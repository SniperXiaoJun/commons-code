package code.ponfee.commons.jedis;

import java.util.List;

import redis.clients.jedis.Jedis;

/**
 * redis lua script
 * @author fupf
 */
public class ScriptOperations extends JedisOperations {

    ScriptOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * 执行script
     * @param script
     * @return
     */
    public Object eval(String script, List<String> keys, List<String> args) {
        return call(shardedJedis -> {
            return shardedJedis.getShard(script).eval(script, keys, args);
        }, null, script, keys, args);
    }

    /**
     * 将脚本 script 添加到脚本缓存中，但并不立即执行这个脚本。
     * @param script
     * @return 给定 script 的 SHA1 校验和
     */
    public String scriptLoad(String script) {
        return call(shardedJedis -> {
            return shardedJedis.getShard(script).scriptLoad(script);
        }, null, script);
    }

    /**
     * 根据给定的 sha1 校验码，对缓存在服务器中的脚本进行求值
     * @param sha1
     * @param keys
     * @param args
     * @return
     */
    public Object evalsha(String script, String sha1, List<String> keys, List<String> args) {
        return call(shardedJedis -> {
            return shardedJedis.getShard(script).evalsha(sha1, keys, args);
        }, null, script, sha1, keys, args);
    }

    /**
     * 获取lua脚本执行后的值
     * @param script
     * @param key
     * @param seconds
     * @return
     */
    public Object get(String script, String key, Integer seconds) {
        return call(shardedJedis -> {
            Jedis jedis = shardedJedis.getShard(script);
            Object value = jedis.get(key);
            if (seconds != null) {
                jedis.expire(key, seconds);
            }
            return value;
        }, null, script, key, seconds);
    }
}
