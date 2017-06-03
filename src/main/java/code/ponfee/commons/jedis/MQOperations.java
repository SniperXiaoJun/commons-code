package code.ponfee.commons.jedis;

import redis.clients.jedis.JedisPubSub;

/**
 * redis message queue
 * @author fupf
 */
public class MQOperations extends JedisOperations {

    MQOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * 将信息 message 发送到指定的频道 channel
     * @param channel
     * @param message
     * @return 接收到信息message的订阅者数量
     */
    public Long publish(String channel, String message) {
        return call(shardedJedis -> {
            return shardedJedis.getShard(channel).publish(channel, message);
        }, null, channel, message);
    }

    /**
     * 订阅给定的一个或多个频道的信息
     * @param jedisPubSub
     * @param channel
     */
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        for (String channel : channels) {
            jedisClient.getJedis(channel).subscribe(jedisPubSub, channel);
            //jedisClient.getJedis(channel).psubscribe(jedisPubSub, patterns);
        }
    }
}
