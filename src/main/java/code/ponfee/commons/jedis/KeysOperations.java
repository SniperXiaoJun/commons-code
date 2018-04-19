package code.ponfee.commons.jedis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.util.ObjectUtils;
import redis.clients.jedis.Jedis;

/**
 * redis key（键）操作类
 * @author fupf
 */
public class KeysOperations extends JedisOperations {

    private static Logger logger = LoggerFactory.getLogger(KeysOperations.class);

    KeysOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * 当key不存在时，返回 -2
     * 当key存在但没有设置剩余生存时间时，返回 -1
     * 否则以秒为单位，返回 key的剩余生存时间
     * @param key
     * @return
     */
    public Long ttl(String key) {
        return ((JedisCallback<Long>) sjedis -> sjedis.ttl(key)).call(jedisClient, null, key);
    }

    /**
     * 获取有效期
     * @param key
     * @return
     */
    public Long pttl(String key) {
        return call(sjedis -> sjedis.pttl(key), null, key);
    }

    /**
     * 获取key列表
     * @param keyWildcard
     * @return
     */
    public Set<String> keys(String keyWildcard) {
        return call(shardedJedis -> {
            Set<String> keys = new HashSet<>();
            for (Jedis jedis : shardedJedis.getAllShards()) {
                keys.addAll(jedis.keys(keyWildcard));
            }
            return keys;
        }, null, keyWildcard);
    }

    /**
     * 设置失效时间
     * @param key
     * @param seconds
     * @return 是否设置成功
     */
    public boolean expire(String key, int seconds) {
        return call(shardedJedis -> {
            return JedisOperations.expire(shardedJedis, key, seconds);
        }, false, key, seconds);
    }

    /**
     * 设置失效时间
     * @param key
     * @param milliseconds
     * @return 是否设置成功
     */
    public boolean pexpire(String key, int milliseconds) {
        return call(shardedJedis -> {
            return JedisOperations.pexpire(shardedJedis, key, milliseconds);
        }, false, key, milliseconds);
    }

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return call(shardedJedis -> {
            return shardedJedis.exists(key);
        }, false, key);
    }

    /**
     * 删除
     * @param key
     * @return 被删除 key 的数量
     */
    public Long del(String key) {
        return call(shardedJedis -> {
            return shardedJedis.del(key);
        }, null, key);
    }

    /**
     * 删除
     * @param key
     * @return 被删除 key 的数量
     */
    public Long del(byte[] key) {
        return call(shardedJedis -> {
            return shardedJedis.del(key);
        }, null, (Object) key);
    }

    /**
     * 删除多个key值
     * @param keys
     * @return
     */
    public Long dels(String... keys) {
        return call(shardedJedis -> {
            if (keys == null || keys.length == 0) {
                return 0L;
            }

            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (jedisList == null || jedisList.isEmpty()) {
                return 0L;
            }

            Long delCounts = 0L;
            int number = jedisList.size();
            if (number < keys.length / BATCH_MULTIPLE) { // key数量大于分片数量的BATCH_MULTIPLE倍
                CompletionService<Long> service = new ExecutorCompletionService<>(EXECUTOR);
                for (Jedis jedis : jedisList) {
                    service.submit(() -> {
                        return jedis.del(keys);
                    });
                }
                for (; number > 0; number--) {
                    try {
                        delCounts += ObjectUtils.ifNull(service.take().get(), 0L);
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("Jedis del occur error", e);
                    }
                }
            } else {
                for (String key : keys) {
                    delCounts += ObjectUtils.ifNull(shardedJedis.del(key), 0L);
                }
            }
            return delCounts;
        }, null, (Object[]) keys);
    }

    /**
     * 删除key（匹配通配符）
     * @param keyWildcard
     * @return 被删除 key 的数量
     */
    public long delWithWildcard(String keyWildcard) {
        return call(shardedJedis -> {
            long delCounts = 0L;
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (jedisList == null || jedisList.isEmpty()) {
                return delCounts;
            }

            int number = jedisList.size();
            CompletionService<Long> service = new ExecutorCompletionService<>(EXECUTOR);
            for (Jedis jedis : jedisList) {
                service.submit(() -> {
                    Set<String> keys = jedis.keys(keyWildcard);
                    if (keys == null || keys.isEmpty()) {
                        return 0L;
                    } else {
                        return jedis.del(keys.toArray(new String[keys.size()]));
                    }
                });
            }
            for (; number > 0; number--) {
                try {
                    delCounts += ObjectUtils.ifNull(service.take().get(), 0L);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Jedis del by wildcard occur error", e);
                }
            }
            return delCounts;
        }, 0L, keyWildcard);
    }

    /**
     * 返回 key 所储存的值的类型。
     * @param key
     * @return none (key不存在)；string (字符串)；list (列表)；set (集合)；zset (有序集)；hash (哈希表)；
     */
    public String type(String key) {
        return call(shardedJedis -> {
            return shardedJedis.type(key);
        }, null, key);
    }

}
