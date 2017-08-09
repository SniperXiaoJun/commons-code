package code.ponfee.commons.jedis;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import code.ponfee.commons.concurrent.NamedThreadFactory;
import code.ponfee.commons.util.Numbers;
import redis.clients.jedis.Jedis;
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

    static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, 20, 60, TimeUnit.SECONDS, // 最大100个线程
                                                                   new SynchronousQueue<>(), // 同步队列，超过数量让调用线程处理
                                                                   new NamedThreadFactory("redis-mget-furture", true));
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(EXECUTOR::shutdown));
    }

    final JedisClient jedisClient;

    JedisOperations(JedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }

    /**
     * 调用勾子函数：有返回值
     * @param hook             勾子对象
     * @param occurErrorRtnVal 出现异常时的返回值
     * @param args             参数
     * @return
     */
    final <T> T hook(JedisHook<T> hook, T occurErrorRtnVal, Object... args) {
        return hook.hook(jedisClient, occurErrorRtnVal, args);
    }

    /**
     * 调用勾子函数：无返回值
     * @param call 勾子对象
     * @param args 参数列表
     */
    final void call(JedisCall call, Object... args) {
        call.call(jedisClient, args);
    }

    /**
     * 获取分片的Jedis
     * @param shardedJedis
     * @param key
     * @return 具体哈希片的Jedis
     */
    final Jedis getShard(ShardedJedis shardedJedis, String key) {
        return shardedJedis.getShard(key);
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
     * 设置过期时间，若seconds为null则不做处理
     * @param shardedJedis
     * @param key
     * @param seconds
     * @return
     */
    static boolean expire(ShardedJedis shardedJedis, String key, Integer seconds) {
        if (seconds == null) return false;
        return Numbers.equals(shardedJedis.expire(key, getActualExpire(seconds)), 1);
    }

    static boolean expire(ShardedJedis shardedJedis, byte[] key, Integer seconds) {
        if (seconds == null) return false;
        return Numbers.equals(shardedJedis.expire(key, getActualExpire(seconds)), 1);
    }

    /**
     * 设置过期时间，若milliseconds为null则不做处理
     * @param shardedJedis
     * @param key
     * @param milliseconds
     * @return
     */
    static boolean pexpire(ShardedJedis shardedJedis, String key, Integer milliseconds) {
        if (milliseconds == null) return false;
        return expire(shardedJedis, key, (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds));
    }

    static boolean pexpire(ShardedJedis shardedJedis, byte[] key, Integer milliseconds) {
        if (milliseconds == null) return false;
        return expire(shardedJedis, key, (int) TimeUnit.MILLISECONDS.toSeconds(milliseconds));
    }

    /**
     * 设置过期时间，若seconds为null且无失效期限则设置默认失效时间
     * @param shardedJedis
     * @param key
     * @param seconds
     * @return
     */
    static boolean expireForce(ShardedJedis shardedJedis, String key, Integer seconds) {
        if (seconds != null) {
            return expire(shardedJedis, key, seconds);
        } else {
            expireDefaultIfInfinite(shardedJedis, key);
            return false;
        }
    }

    static boolean expireForce(ShardedJedis shardedJedis, byte[] key, Integer seconds) {
        if (seconds != null) {
            return expire(shardedJedis, key, seconds);
        } else {
            expireDefaultIfInfinite(shardedJedis, key);
            return false;
        }
    }

    /**
     * 设置过期时间，若milliseconds为null且无失效期限则设置默认失效时间
     * @param shardedJedis
     * @param key
     * @param milliseconds
     * @return
     */
    static boolean pexpireForce(ShardedJedis shardedJedis, String key, Integer milliseconds) {
        if (milliseconds != null) {
            return pexpire(shardedJedis, key, milliseconds);
        }
        expireDefaultIfInfinite(shardedJedis, key);
        return false;
    }

    static boolean pexpireForce(ShardedJedis shardedJedis, byte[] key, Integer milliseconds) {
        if (milliseconds != null) {
            return pexpire(shardedJedis, key, milliseconds);
        }
        expireDefaultIfInfinite(shardedJedis, key);
        return false;
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

}
