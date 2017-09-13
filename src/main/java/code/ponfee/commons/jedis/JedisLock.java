package code.ponfee.commons.jedis;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.util.Numbers;
import code.ponfee.commons.util.ObjectUtils;
import redis.clients.jedis.Jedis;
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
 *     Lock lock = new JedisLock(jedisClient, "lockKey", 5);
 *     // auto timeout release lock
 *     if (!lock.tryLock(100, TimeUnit.MILLISECONDS)) return;
 *     try {
 *       // ... method body
 *     } finally {
 *       lock.unlock();
 *     }
 *   }
 * }
 * </pre>
 * 
 * 基于redis的分布式锁
 * 使用redis transaction功能实现
 * @author fupf
 */
public class JedisLock implements Lock, java.io.Serializable {

    private static final long serialVersionUID = -6209919116306827731L;
    private static Logger logger = LoggerFactory.getLogger(JedisLock.class);

    private static final int MAX_TOMEOUT_SECONDS = 86400; // 最大超 时为1天
    private static final int MIN_TOMEOUT_SECONDS = 1; // 最小超 时为1秒
    private static final int MIN_SLEEP_MILLIS = 9; // 最小休眠时间为5毫秒
    private static final String SEPARATOR = ":"; // 分隔符
    private static final transient ThreadLocal<String> LOCK_VALUE = new ThreadLocal<>();

    private final Lock innerLock = new ReentrantLock(); // 内部锁
    private final transient JedisClient jedisClient;
    private final String lockKey;
    private final int timeoutSeconds; // 锁的超时时间，防止死锁
    private final long timeoutMillis;
    private final long sleepMillis;

    public JedisLock(JedisClient jedisClient, String lockKey) {
        this(jedisClient, lockKey, MAX_TOMEOUT_SECONDS);
    }

    public JedisLock(JedisClient jedisClient, String lockKey, int timeoutSeconds) {
        this(jedisClient, lockKey, timeoutSeconds, 9);
    }

    /**
     * 锁对象构造函数
     * @param jedisClient        jedisClient实例
     * @param lockKey            待加锁的键
     * @param timeoutSeconds     锁超时时间（防止死锁）
     * @param sleepMillis        休眠时间（毫秒）
     */
    public JedisLock(JedisClient jedisClient, String lockKey, int timeoutSeconds, int sleepMillis) {
        this.jedisClient = jedisClient;
        this.lockKey = "jedis:lock:" + lockKey;
        timeoutSeconds = Math.abs(timeoutSeconds);
        if (timeoutSeconds > MAX_TOMEOUT_SECONDS) {
            timeoutSeconds = MAX_TOMEOUT_SECONDS;
        } else if (timeoutSeconds < MIN_TOMEOUT_SECONDS) {
            timeoutSeconds = MIN_TOMEOUT_SECONDS;
        }
        this.timeoutSeconds = timeoutSeconds;
        this.timeoutMillis = TimeUnit.SECONDS.toMillis(timeoutSeconds);
        this.sleepMillis = Numbers.bounds(sleepMillis, MIN_SLEEP_MILLIS, (int) timeoutMillis);
    }

    /**
     * 等待锁直到获取
     */
    public @Override void lock() {
        for (;;) {
            if (tryLock()) break;
            try {
                TimeUnit.MILLISECONDS.sleep(sleepMillis);
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
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        }
    }

    /**
     * 尝试获取锁，成功返回true，失败返回false
     */
    public @Override boolean tryLock() {
        innerLock.lock();
        try {
            return jedisClient.hook(shardedJedis -> {
                Jedis jedis = shardedJedis.getShard(lockKey);

                // 仅当lockKey不存在才能设置成功并返回1，否则setnx不做任何动作返回0
                Long result = jedis.setnx(lockKey, buildValue());
                if (result != null && result.intValue() == 1) {
                    jedis.expire(lockKey, timeoutSeconds); // 成功则需要设置失效期
                    return true;
                }

                jedis.watch(lockKey); // 监视lockKey
                String value = jedis.get(lockKey); // 获取当前锁值
                if (value == null) {
                    jedis.unwatch();
                    return tryLock(); // 锁被释放，重新获取
                } else if (System.currentTimeMillis() <= parseValue(value)) {
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
            }, false);
        } finally {
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
            if (tryLock()) return true;
            if (System.nanoTime() - startTime > timeout) return false; // 等待超时则返回
            TimeUnit.MILLISECONDS.sleep(sleepMillis);
        }
    }

    /**
     * 释放锁
     */
    public @Override void unlock() {
        innerLock.lock();
        try {
            jedisClient.call(shardedJedis -> {
                // 根据分片获取jedis
                Jedis jedis = shardedJedis.getShard(lockKey);
                jedis.watch(lockKey);
                String value = LOCK_VALUE.get(); // 获取当前线程保存的锁值
                if (value == null || !value.equals(jedis.get(lockKey))) {
                    // 当前线程未获取过锁或锁已被其它线程获取
                    jedis.unwatch();
                } else {
                    // 当前线程持有锁，需要释放锁
                    Transaction tx = jedis.multi();
                    tx.del(lockKey);
                    tx.exec();
                }
            });
        } finally {
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
     * 获取锁值
     * @return
     */
    private String buildValue() {
        String value = new StringBuilder(ObjectUtils.uuid22()).append(SEPARATOR)
                 .append(System.currentTimeMillis() + timeoutMillis).toString();
        LOCK_VALUE.set(value);
        return value;
    }
}
