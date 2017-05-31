package code.ponfee.commons.jedis;

import redis.clients.jedis.ShardedJedis;

/**
 * 钩子函数
 * @author fupf
 * @param <T>
 */
@FunctionalInterface
interface JedisHook<T> {

    T operate(ShardedJedis shardedJedis);

    /**
     * @param ops         JedisOperations 实例
     * @param defaultVal  异常时的返回值
     * @param args        参数列表
     * @return
     */
    default T hook(JedisOperations ops, T defaultVal, Object... args) {
        try (ShardedJedis shardedJedis = ops.jedisClient.getShardedJedis()) {
            return (T) this.operate(shardedJedis);
        } catch (Exception e) {
            ops.exception(e, args);
            return defaultVal;
        }
    }
}
