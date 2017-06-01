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
     * @param ops              JedisOperation
     * @param occurErrorRtnVal 出现异常时的返回值
     * @param args             参数列表
     * @return
     */
    default T hook(JedisOperations ops, T occurErrorRtnVal, Object... args) {
        try (ShardedJedis shardedJedis = ops.jedisClient.getShardedJedis()) {
            return this.operate(shardedJedis);
        } catch (Exception e) {
            ops.exception(e, args);
            return occurErrorRtnVal;
        }
    }
}
