package code.ponfee.commons.jedis;

import redis.clients.jedis.ShardedJedis;

/**
 * 钩子函数（有返回值时使用）
 * @author fupf
 * @param <T>
 */
@FunctionalInterface
public interface JedisHook<T> {

    T hook(ShardedJedis shardedJedis);

    /**
     * 挂勾
     * @param jedisClient      JedisClient
     * @param occurErrorRtnVal 出现异常时的返回值
     * @param args             参数列表
     * @return
     */
    default T hook(JedisClient jedisClient, T occurErrorRtnVal, Object... args) {
        try (ShardedJedis shardedJedis = jedisClient.getShardedJedis()) {
            return this.hook(shardedJedis);
        } catch (Exception e) {
            jedisClient.exception(e, args);
            return occurErrorRtnVal;
        }
    }
}
