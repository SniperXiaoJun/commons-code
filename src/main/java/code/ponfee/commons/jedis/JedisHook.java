package code.ponfee.commons.jedis;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.exceptions.JedisException;

/**
 * 钩子函数
 * @author fupf
 * @param <T>
 */
abstract class JedisHook<T> {
    JedisOperations ops;

    JedisHook(JedisOperations ops) {
        this.ops = ops;
    }

    abstract T operate(ShardedJedis shardedJedis);

    abstract T except(JedisException e);

    final T hook() {
        try (ShardedJedis shardedJedis = ops.jedisClient.getShardedJedis()) {
            return (T) this.operate(shardedJedis);
        } catch (JedisException e) {
            return this.except(e);
        }
    }
}
