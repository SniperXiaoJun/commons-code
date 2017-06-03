package code.ponfee.commons.jedis;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.util.ObjectUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.Transaction;

/**
 * <pre>
 * class X {
 *   public void m() {
 *     Lock lock = new JedisLock(jedisClient, "lockKey", 5);
 *     lock.lock();  // block until acquire lock or timeout
 *     try {
 *       // ... method body
 *     } finally {
 *       lock.unlock()
 *     }
 *   }
 * }
 * 
 * class Y {
 *   public void m() {
 *     Lock lock = new JedisLock(jedisClient, "lockKey", 5);
 *     if (!lock.tryLock()) return;
 *     try {
 *       // ... method body
 *     } finally {
 *       lock.unlock();
 *     }
 *   }
 * }
 * 
 * class Z {
 *   public void m() {
 *     // auto timeout release lock
 *     if (new JedisLock(jedisClient, "lockKey", 5).tryLock()) {
 *       // ... method body
 *     }
 *   }
 * }
 * </pre>
 * 
 * 基于redis的分布式锁
 * redis transaction的一个应用场景
 * @author fupf
 */
public class JedisLock implements Lock, Serializable {

    private static final long serialVersionUID = -6209919116306827731L;
    private static Logger logger = LoggerFactory.getLogger(JedisLock.class);

    private static final int MAX_TOMEOUT = 86400; // 最大超 时为1天
    private static final int MIN_TOMEOUT = 1; // 最小超 时为1秒
    private static final int MIN_SLEEP_MILLIS = 9; // 最小休眠时间为5毫秒
    private static final String SEPARATOR = ":"; // 分隔符
    private static final transient ThreadLocal<String> LOCK_VALUE = new ThreadLocal<>();

    private final Lock innerLock = new ReentrantLock(); // 内部锁
    private final transient JedisClient jedisClient;
    private final String lockKey;
    private final int timeoutSeconds; // 锁的超时时间，防止死锁
    private final long timeoutNanos;
    private final long sleepMillis;

    public JedisLock(JedisClient jedisClient, String lockKey) {
        this(jedisClient, lockKey, MAX_TOMEOUT);
    }

    public JedisLock(JedisClient jedisClient, String lockKey, int timeoutSeconds) {
        this(jedisClient, lockKey, timeoutSeconds, 9);
    }

    /**
     * 锁对象构造函数
     * @param jedisClient        jedisClient实例
     * @param lockKey            锁键
     * @param timeoutSeconds     锁超时时间（防止死锁）
     * @param sleepMillis        休眠时间（毫秒）
     */
    public JedisLock(JedisClient jedisClient, String lockKey, int timeoutSeconds, int sleepMillis) {
        this.jedisClient = jedisClient;
        this.lockKey = "lock:" + lockKey;
        timeoutSeconds = Math.abs(timeoutSeconds);
        if (timeoutSeconds > MAX_TOMEOUT) {
            timeoutSeconds = MAX_TOMEOUT;
        } else if (timeoutSeconds < MIN_TOMEOUT) {
            timeoutSeconds = MIN_TOMEOUT;
        }
        this.timeoutSeconds = timeoutSeconds;
        this.timeoutNanos = TimeUnit.SECONDS.toNanos(timeoutSeconds);
        this.sleepMillis = sleepMillis < MIN_SLEEP_MILLIS ? MIN_SLEEP_MILLIS : sleepMillis;
    }

    /**
     * 等待锁直到获取
     */
    public @Override void lock() {
        while (true) {
            if (tryLock()) break;
            try {
                Thread.sleep(sleepMillis); // sleep sleepMillis milliseconds
            } catch (InterruptedException e) {
                logger.error("jedis lock interrupted exception", e);
            }
        }
    }

    /**
     * 等待锁直到获取成功或抛出InterruptedException异常
     */
    public @Override void lockInterruptibly() throws InterruptedException {
        for (;;) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (tryLock()) break;
            Thread.sleep(sleepMillis); // sleep sleepMillis milliseconds
        }
    }

    /**
     * 尝试获取锁，成功返回true，失败返回false
     */
    public @Override boolean tryLock() {
        innerLock.lock();
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = jedisClient.getShardedJedis();
            if (this.setnx(shardedJedis)) return true; // 抢占锁成功

            Jedis jedis = shardedJedis.getShard(lockKey);
            jedis.watch(lockKey); // 监视lockKey
            String value = jedis.get(lockKey); // 获取当前锁值
            if (value == null) {
                jedis.unwatch();
                return tryLock(); // 锁被释放，重新获取
            } else if (System.nanoTime() <= parseValue(value)) {
                jedis.unwatch();
                return false; // 锁未超时
            } else {
                // 锁已超时，争抢锁（事务控制）
                Transaction tx = jedis.multi();
                tx.getSet(lockKey, buildValue());
                tx.expire(lockKey, JedisOperations.getActualExpire(timeoutSeconds));
                List<Object> exec = tx.exec(); // exec执行完后被监控的键会自动unwatch
                return exec != null && !exec.isEmpty() && value.equals(exec.get(0));
            }
        } finally {
            jedisClient.closeShardedJedis(shardedJedis);
            innerLock.unlock();
        }
    }

    /**
     * 尝试获取锁，成功返回true，失败返回false
     * 线程中断则抛出interrupted异常
     */
    public @Override boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        timeout = unit.toNanos(timeout);
        long startTime = System.nanoTime();
        for (;;) {
            if (Thread.interrupted()) throw new InterruptedException();
            if (System.nanoTime() - startTime > timeout) return false; // 等待超时则返回
            if (tryLock()) return true;
            Thread.sleep(sleepMillis); // sleep sleepMillis milliseconds
        }
    }

    /**
     * 释放锁
     */
    public @Override void unlock() {
        innerLock.lock();
        ShardedJedis shardedJedis = null;
        try {
            shardedJedis = jedisClient.getShardedJedis();

            // 根据分片获取jedis
            Jedis jedis = shardedJedis.getShard(lockKey);
            jedis.watch(lockKey);
            String value = LOCK_VALUE.get(); // 获取当前线程保存的锁值
            if (value == null || !value.equals(jedis.get(lockKey))) {
                // 当前线程未获取过锁或锁已被其它线程获取，则跳过不处理
                jedis.unwatch();
                return;
            }

            // 当前线程持有锁，需要释放锁
            Transaction tx = jedis.multi();
            tx.del(lockKey);
            tx.exec();
        } finally {
            jedisClient.closeShardedJedis(shardedJedis);
            innerLock.unlock();
        }
    }

    public @Override Condition newCondition() {
        throw new UnsupportedOperationException();
    }

    /**
     * <pre> 
     *  {@code
     *     class X {
     *       Lock lock = new JedisLock(jedisClient, "lockKey", 5);
     *       // ...
     *       public void m() {
     *         assert !lock.isHeldByCurrentThread();
     *         lock.lock();
     *         try {
     *             // ... method body
     *         } finally {
     *             lock.unlock();
     *         }
     *       }
     *     }
     *  }
     * </pre>
     * 当前线程是否持有锁
     * @return
     */
    public boolean isHeldByCurrentThread() {
        String value = LOCK_VALUE.get();
        return value != null && value.equals(jedisClient.valueOps().get(lockKey));
    }

    /**
     * 是否已锁（任何线程）
     * @return
     */
    public boolean isLocked() {
        return jedisClient.valueOps().get(lockKey) != null;
    }

    /**
     * 解析值数据
     * @param value
     * @return
     */
    private long parseValue(String value) {
        return Long.parseLong(value.split(SEPARATOR)[1]);
    }

    /**
     * 将 key 的值设为 value ，当且仅当 key 不存在。
     * 若给定的 key 已经存在，则 SETNX 不做任何动作。
     * @param shardedJedis
     * @return  设置是否成功
     */
    private boolean setnx(ShardedJedis shardedJedis) {
        Long result = shardedJedis.setnx(lockKey, buildValue());

        // 设置成功，返回 1。设置失败，返回 0 。
        if (result == null || result.intValue() != 1) return false;

        // 设置成功则需要设置失效期
        JedisOperations.expire(shardedJedis, lockKey, timeoutSeconds);
        return true;
    }

    /**
     * 获取锁值
     * @return
     */
    private String buildValue() {
        String value = new StringBuilder(ObjectUtils.uuid32()).append(SEPARATOR)
                           .append(System.nanoTime() + timeoutNanos).toString();
        LOCK_VALUE.set(value);
        return value;
    }
}
