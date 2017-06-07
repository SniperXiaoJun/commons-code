package code.ponfee.commons.jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import redis.clients.jedis.ShardedJedis;

/**
 * jedis操作抽象类
 * @author fupf
 */
abstract class JedisOperations {

    private static final int MIN_EXPIRE_SECONDS = 1; // min 1 seconds
    static final int DEFAULT_EXPIRE_SECONDS = 86400; // default 1 days
    private static final int MAX_EXPIRE_SECONDS = 30 * DEFAULT_EXPIRE_SECONDS; // max 30 days
    static final String SUCCESS_MSG = "OK"; // 返回成功信息
    static final int FUTURE_TIMEOUT = 1500; // future task timeout milliseconds

    static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, 100, 60, TimeUnit.SECONDS, // 最大100个线程
                                                                   new SynchronousQueue<>(), // 同步队列，超过数量让调用线程处理
                                                                   new NamedThreadFactory("redis-mget-furture", true));
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> EXECUTOR.shutdown()));
    }

    final JedisClient jedisClient;

    JedisOperations(JedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }

    /**
     * 调用勾子函数：有返回值
     * @param hook              勾子对象
     * @param occurErrorRtnVal  出现异常时的返回值
     * @param args              参数
     * @return
     */
    final <T> T hook(JedisHook<T> hook, T occurErrorRtnVal, Object... args) {
        return jedisClient.hook(hook, occurErrorRtnVal, args);
    }

    /**
     * 调用勾子函数：无返回值
     * @param call 调用勾子函数
     * @param args 参数列表
     */
    final void call(JedisCall call, Object... args) {
        jedisClient.call(call, args);
    }

    static int getActualExpire(int seconds) {
        if (seconds > MAX_EXPIRE_SECONDS) {
            seconds = MAX_EXPIRE_SECONDS;
        } else if (seconds < MIN_EXPIRE_SECONDS) {
            seconds = MIN_EXPIRE_SECONDS;
        }
        return seconds;
    }

    /**
     * 设置过期时间
     * @param seconds
     * @return
     */
    static boolean expire(ShardedJedis shardedJedis, String key, Integer seconds) {
        if (seconds == null) {
            expireDefaultIfInfinite(shardedJedis, key);
            return false;
        }
        return equals(shardedJedis.expire(key, getActualExpire(seconds)), 1);
    }

    static boolean expire(ShardedJedis shardedJedis, byte[] key, Integer seconds) {
        if (seconds == null) {
            expireDefaultIfInfinite(shardedJedis, key);
            return false;
        }
        return equals(shardedJedis.expire(key, getActualExpire(seconds)), 1);
    }

    /**
     * 设置过期时间
     * @param milliseconds
     * @return
     */
    static boolean pexpire(ShardedJedis shardedJedis, String key, Integer milliseconds) {
        if (milliseconds == null) {
            expireDefaultIfInfinite(shardedJedis, key);
            return false;
        }
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        return expire(shardedJedis, key, seconds);
    }

    static boolean equals(Number a, Number b) {
        return (a == b) || (a != null && b != null && a.longValue() == b.longValue());
    }

    /**
     * 防止内存泄露：如果无失效期，则设置默认失效时间
     * @param shardedJedis
     * @param key
     */
    private static void expireDefaultIfInfinite(ShardedJedis shardedJedis, String key) {
        if (shardedJedis.ttl(key) == -1) {
            shardedJedis.expire(key, DEFAULT_EXPIRE_SECONDS);
        }
    }

    private static void expireDefaultIfInfinite(ShardedJedis shardedJedis, byte[] key) {
        if (shardedJedis.ttl(key) == -1) {
            shardedJedis.expire(key, DEFAULT_EXPIRE_SECONDS);
        }
    }

    /**
     * 线程工厂
     * @author fupf
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

        private final AtomicInteger mThreadNum = new AtomicInteger(1);
        private final String mPrefix;
        private final boolean mDaemo;
        private final ThreadGroup mGroup;

        @SuppressWarnings("unused")
        NamedThreadFactory() {
            this("pool-" + POOL_SEQ.getAndIncrement(), false);
        }

        @SuppressWarnings("unused")
        NamedThreadFactory(String prefix) {
            this(prefix, false);
        }

        NamedThreadFactory(String prefix, boolean daemo) {
            mPrefix = prefix + "-thread-";
            mDaemo = daemo;
            SecurityManager s = System.getSecurityManager();
            mGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable runnable) {
            String name = mPrefix + mThreadNum.getAndIncrement();
            Thread ret = new Thread(mGroup, runnable, name, 0);
            ret.setDaemon(mDaemo);
            return ret;
        }

        @SuppressWarnings("unused")
        public ThreadGroup getThreadGroup() {
            return mGroup;
        }
    }

}
