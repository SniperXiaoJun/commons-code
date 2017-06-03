package code.ponfee.commons.jedis;

import redis.clients.jedis.ShardedJedis;

/**
 * 钩子函数
 * @author fupf
 */
@FunctionalInterface
public interface JedisCall {

    void call(ShardedJedis shardedJedis);

    /**
     * @param ops              JedisClient
     * @param args             参数列表
     * @return
     */
    default void call(JedisClient jedisClient, Object... args) {
        try (ShardedJedis shardedJedis = jedisClient.getShardedJedis()) {
            this.call(shardedJedis);
        } catch (Exception e) {
            jedisClient.exception(e);
        }
    }
}
