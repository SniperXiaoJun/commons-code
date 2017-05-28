package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.exceptions.JedisException;

/**
 * redis hash（哈希表）操作类
 * @author fupf
 */
public class HashOperations extends JedisOperations {
    private static Logger logger = LoggerFactory.getLogger(HashOperations.class);

    HashOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * <pre>
     *  将哈希表 key 中的域 field 的值设为 value 。
     *  如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
     *  如果域 field 已经存在于哈希表中，旧值将被覆盖。
     * </pre>
     * 
     * @param key
     * @param field
     * @param value
     * @param seconds
     * @return 返回值：true设置一个新域；false覆盖一个旧域；
     */
    public boolean hset(final String key, final String field, final String value, final Integer seconds) {
        if (value == null) return false;

        return new JedisHook<Boolean>(this) {
            @Override
            Boolean operate(ShardedJedis shardedJedis) {
                boolean flag = JedisOperations.equals(shardedJedis.hset(key, field, value), 1);
                expire(shardedJedis, key, seconds);
                return flag;
            }

            @Override
            Boolean except(JedisException e) {
                logger.error(buildError(key, field, value, seconds), e);
                return false;
            }
        }.hook();
    }

    public boolean hset(String key, String field, String value) {
        return this.hset(key, field, value, null);
    }

    /**
     * 返回哈希表 key 中给定域 field 的值。
     * @param key
     * @param field
     * @param seconds
     * @return 给定域的值。当给定域不存在或是给定 key 不存在时，返回 nil 。
     */
    public String hget(final String key, final String field, final Integer seconds) {
        return new JedisHook<String>(this) {
            @Override
            String operate(ShardedJedis shardedJedis) {
                String result = shardedJedis.hget(key, field);
                if (result != null) {
                    expire(shardedJedis, key, seconds);
                }
                return result;
            }

            @Override
            String except(JedisException e) {
                logger.error(buildError(key, field, seconds), e);
                return null;
            }
        }.hook();
    }

    public String hget(String key, String field) {
        return this.hget(key, field, null);
    }

    /**
     * <pre>
     * 返回哈希表 key 中，所有的域和值。 
     * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @return 以map形式返回哈希表的域和域的值
     */
    public Map<String, String> hgetAll(final String key, final Integer seconds) {
        return new JedisHook<Map<String, String>>(this) {
            @Override
            Map<String, String> operate(ShardedJedis shardedJedis) {
                Map<String, String> result = shardedJedis.hgetAll(key);
                if (result != null && !result.isEmpty()) {
                    expire(shardedJedis, key, seconds);
                }
                return result;
            }

            @Override
            Map<String, String> except(JedisException e) {
                logger.error(buildError(key, seconds), e);
                return null;
            }
        }.hook();
    }

    public Map<String, String> hgetAll(String key) {
        return this.hgetAll(key, null);
    }

    /**
     * 返回哈希表 key 中所有域的值
     * @param key
     * @param seconds
     * @param isCompress
     * @return 一个包含哈希表中所有值的表
     */
    public List<String> hvals(final String key, final Integer seconds) {
        return new JedisHook<List<String>>(this) {
            @Override
            List<String> operate(ShardedJedis shardedJedis) {
                List<String> result = shardedJedis.hvals(key);
                if (result != null && !result.isEmpty()) {
                    expire(shardedJedis, key, seconds);
                }
                return result;
            }

            @Override
            List<String> except(JedisException e) {
                logger.error(buildError(key, seconds), e);
                return null;
            }
        }.hook();
    }

    public List<String> hvals(String key) {
        return this.hvals(key, null);
    }

    /**
     * <pre>
     *  将哈希表 key 中的域 field 的值设为 value 。
     *  如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作。
     *  如果域 field 已经存在于哈希表中，旧值将被覆盖。
     * </pre>
     * 
     * @param key
     * @param field
     * @param t
     * @param isCompress
     * @param seconds
     * @return 返回值：true设置一个新域；false覆盖一个旧域；
     */
    public <T extends Object> boolean hsetObject(final byte[] key, final byte[] field,
        final T t, final boolean isCompress, final Integer seconds) {
        if (t == null) return false;

        return new JedisHook<Boolean>(this) {
            @Override
            Boolean operate(ShardedJedis shardedJedis) {
                byte[] data = jedisClient.serialize(t, isCompress);
                boolean flag = JedisOperations.equals(shardedJedis.hset(key, field, data), 1);
                expire(shardedJedis, key, seconds);
                return flag;
            }

            @Override
            Boolean except(JedisException e) {
                logger.error(buildError(key, field, t, isCompress, seconds), e);
                return false;
            }
        }.hook();
    }

    public <T extends Object> boolean hsetObject(byte[] key, byte[] field, T t, boolean isCompress) {
        return this.hsetObject(key, field, t, isCompress, null);
    }

    public <T extends Object> boolean hsetObject(byte[] key, byte[] field, T t, Integer seconds) {
        return this.hsetObject(key, field, t, true, seconds);
    }

    public <T extends Object> boolean hsetObject(byte[] key, byte[] field, T t) {
        return this.hsetObject(key, field, t, true, null);
    }

    /**
     * 获取对象
     * @param key
     * @param field
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return
     */
    public <T extends Object> T hgetObject(final byte[] key, final byte[] field,
        final Class<T> clazz, final boolean isCompress, final Integer seconds) {
        return new JedisHook<T>(this) {
            @Override
            T operate(ShardedJedis shardedJedis) {
                byte[] data = shardedJedis.hget(key, field);
                T t = jedisClient.deserialize(data, clazz, isCompress);
                if (t != null) {
                    expire(shardedJedis, key, seconds);
                }
                return t;
            }

            @Override
            T except(JedisException e) {
                logger.error(buildError(key, field, clazz, isCompress, seconds), e);
                return null;
            }
        }.hook();
    }

    public <T extends Object> T hgetObject(byte[] key, byte[] field, Class<T> clazz,
        boolean isCompress) {
        return this.hgetObject(key, field, clazz, isCompress, null);
    }

    public <T extends Object> T hgetObject(byte[] key, byte[] field, Class<T> clazz, Integer seconds) {
        return this.hgetObject(key, field, clazz, true, seconds);
    }

    public <T extends Object> T hgetObject(byte[] key, byte[] field, Class<T> clazz) {
        return this.hgetObject(key, field, clazz, true, null);
    }

    /**
     * <pre>
     * 返回哈希表 key 中，所有的域和值。 
     * 在返回值里，紧跟每个域名(field name)之后是域的值(value)，所以返回值的长度是哈希表大小的两倍。
     * </pre>
     * 
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return 以map形式返回哈希表的域和域的值
     */
    public <T extends Object> Map<byte[], T> hgetAllObject(final byte[] key,
        final Class<T> clazz, final boolean isCompress, final Integer seconds) {
        return new JedisHook<Map<byte[], T>>(this) {
            @Override
            Map<byte[], T> operate(ShardedJedis shardedJedis) {
                Map<byte[], byte[]> datas = shardedJedis.hgetAll(key);
                Map<byte[], T> result = new HashMap<>();
                if (datas != null && !datas.isEmpty()) {
                    for (Entry<byte[], byte[]> entry : datas.entrySet()) {
                        T t = jedisClient.deserialize(entry.getValue(), clazz, isCompress);
                        result.put(entry.getKey(), t);
                    }
                    expire(shardedJedis, key, seconds);
                }
                return result;
            }

            @Override
            Map<byte[], T> except(JedisException e) {
                logger.error(buildError(key, clazz, isCompress, seconds), e);
                return null;
            }
        }.hook();
    }

    public <T extends Object> Map<byte[], T> hgetAllObject(byte[] key, Class<T> clazz,
        boolean isCompress) {
        return this.hgetAllObject(key, clazz, isCompress, null);
    }

    public <T extends Object> Map<byte[], T> hgetAllObject(byte[] key, Class<T> clazz, Integer seconds) {
        return this.hgetAllObject(key, clazz, true, seconds);
    }

    public <T extends Object> Map<byte[], T> hgetAllObject(byte[] key, Class<T> clazz) {
        return this.hgetAllObject(key, clazz, true, null);
    }

    /**
     * 返回哈希表 key 中所有域的值
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return 一个包含哈希表中所有值的表
     */
    public <T extends Object> List<T> hvalsObject(final byte[] key,
        final Class<T> clazz, final boolean isCompress, final Integer seconds) {
        return new JedisHook<List<T>>(this) {
            @Override
            List<T> operate(ShardedJedis shardedJedis) {
                List<T> list = new ArrayList<T>();
                for (byte[] data : shardedJedis.hvals(key)) {
                    T t = jedisClient.deserialize(data, clazz, isCompress);
                    if (t != null) list.add(t);
                }
                expire(shardedJedis, key, seconds);
                return list;
            }

            @Override
            List<T> except(JedisException e) {
                logger.error(buildError(key, clazz, isCompress, seconds), e);
                return null;
            }
        }.hook();
    }

    public <T extends Object> List<T> hvalsObject(byte[] key, Class<T> clazz, boolean isCompress) {
        return this.hvalsObject(key, clazz, isCompress, null);
    }

    public <T extends Object> List<T> hvalsObject(byte[] key, Class<T> clazz, Integer seconds) {
        return this.hvalsObject(key, clazz, true, seconds);
    }

    public <T extends Object> List<T> hvalsObject(byte[] key, Class<T> clazz) {
        return this.hvalsObject(key, clazz, true, null);

    }

    /**
     * <pre>
     *  同时将多个 field-value (域-值)对设置到哈希表 key 中。
     *  此命令会覆盖哈希表中已存在的域。
     *  如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
     * </pre>
     * 
     * @param key
     * @param map
     * @param isCompress
     * @param seconds
     * @return 返回值：true成功；false失败；
     */
    public <T extends Object> boolean hmsetObjects(final byte[] key, final Map<byte[], T> map,
        final boolean isCompress, final Integer seconds) {
        if (map == null || map.isEmpty()) return false;
        return new JedisHook<Boolean>(this) {
            @Override
            Boolean operate(ShardedJedis shardedJedis) {
                Map<byte[], byte[]> data = new HashMap<byte[], byte[]>();
                for (Entry<byte[], T> entry : map.entrySet()) {
                    data.put(entry.getKey(), jedisClient.serialize(entry.getValue(), isCompress));
                }

                String rtn = shardedJedis.hmset(key, data);
                expire(shardedJedis, key, seconds);
                return SUCCESS_MSG.equalsIgnoreCase(rtn);
            }

            @Override
            Boolean except(JedisException e) {
                logger.error(buildError(key, map, isCompress, seconds), e);
                return false;
            }
        }.hook();
    }

    public <T extends Object> boolean hmsetObjects(byte[] key, Map<byte[], T> map, boolean isCompress) {
        return this.hmsetObjects(key, map, isCompress, null);
    }

    public <T extends Object> boolean hmsetObjects(byte[] key, Map<byte[], T> map, Integer seconds) {
        return this.hmsetObjects(key, map, true, seconds);
    }

    public <T extends Object> boolean hmsetObjects(byte[] key, Map<byte[], T> map) {
        return this.hmsetObjects(key, map, true, null);
    }

    /**
     * <pre>
     *  返回哈希表 key 中，一个或多个给定域的值。
     *  如果给定的域不存在于哈希表，那么返回一个 nil 值。
     *  因为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。
     * </pre>
     * 
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @param fields
     * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     */
    public <T extends Object> List<T> hmgetObjects(final byte[] key, final Class<T> clazz,
        final boolean isCompress, final Integer seconds, final byte[]... fields) {
        if (key == null || fields == null) return null;
        return new JedisHook<List<T>>(this) {
            @Override
            List<T> operate(ShardedJedis shardedJedis) {
                List<byte[]> datas = shardedJedis.hmget(key, fields);
                if (datas == null || datas.isEmpty()) return null;

                List<T> list = new ArrayList<>();
                for (byte[] data : datas) {
                    T t = jedisClient.deserialize(data, clazz, isCompress);
                    if (t != null) list.add(t);
                }
                expire(shardedJedis, key, seconds);
                return list;
            }

            @Override
            List<T> except(JedisException e) {
                logger.error(buildError(key, clazz, isCompress, seconds, fields), e);
                return null;
            }
        }.hook();
    }

    public <T extends Object> List<T> hmgetObjects(byte[] key, Class<T> clazz, boolean isCompress,
        byte[]... fields) {
        return this.hmgetObjects(key, clazz, isCompress, null, fields);
    }

    public <T extends Object> List<T> hmgetObjects(byte[] key, Class<T> clazz, Integer seconds,
        byte[]... fields) {
        return this.hmgetObjects(key, clazz, true, seconds, fields);
    }

    public <T extends Object> List<T> hmgetObjects(byte[] key, Class<T> clazz, byte[]... fields) {
        return this.hmgetObjects(key, clazz, true, null, fields);
    }

    /**
     * <pre>
     *  为哈希表 key 中的域 field 的值加上增量 increment，增量也可以为负数，相当于对给定域进行减法操作。
     *  如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令。
     *  如果域 field 不存在，那么在执行命令前，域的值被初始化为 0
     * </pre>
     * 
     * @param key
     * @param field
     * @param value
     * @param seconds
     * @return 执行 HINCRBY 命令之后，哈希表 key 中域 field 的值
     */
    public Long hincrBy(final String key, final String field,
        final int value, final Integer seconds) {
        return new JedisHook<Long>(this) {
            @Override
            Long operate(ShardedJedis shardedJedis) {
                Long rtn = shardedJedis.hincrBy(key, field, value);
                expire(shardedJedis, key, seconds);
                return rtn;
            }

            @Override
            Long except(JedisException e) {
                logger.error(buildError(key, field, value, seconds), e);
                return null;
            }
        }.hook();
    }

    public Long hincrBy(String key, String field, int value) {
        return hincrBy(key, field, value, null);
    }

    /**
     * <pre>
     *  同时将多个 field-value (域-值)对设置到哈希表 key 中。
     *  此命令会覆盖哈希表中已存在的域。
     *  如果 key 不存在，一个空哈希表被创建并执行 HMSET 操作。
     * </pre>
     * 
     * @param key
     * @param map
     * @param seconds
     * @return 返回值：true成功；false失败；
     */
    public boolean hmset(final String key, final Map<String, String> map, final Integer seconds) {
        if (map == null || map.isEmpty()) return false;

        return new JedisHook<Boolean>(this) {
            @Override
            Boolean operate(ShardedJedis shardedJedis) {
                String rtn = shardedJedis.hmset(key, map);
                expire(shardedJedis, key, seconds);
                return SUCCESS_MSG.equalsIgnoreCase(rtn);
            }

            @Override
            Boolean except(JedisException e) {
                logger.error(buildError(key, map, seconds), e);
                return false;
            }
        }.hook();
    }

    public boolean hmset(String key, Map<String, String> map) {
        return this.hmset(key, map, null);
    }

    /**
     * <pre>
     *  返回哈希表 key 中，一个或多个给定域的值。
     *  如果给定的域不存在于哈希表，那么返回一个 nil 值。
     *  为不存在的 key 被当作一个空哈希表来处理，所以对一个不存在的 key 进行 HMGET 操作将返回一个只带有 nil 值的表。
     * </pre>
     * 
     * @param key
     * @param seconds
     * @param fields
     * @return 一个包含多个给定域的关联值的表，表值的排列顺序和给定域参数的请求顺序一样。
     */
    public List<String> hmget(final String key, final Integer seconds, final String... fields) {
        return new JedisHook<List<String>>(this) {
            @Override
            List<String> operate(ShardedJedis shardedJedis) {
                List<String> list = shardedJedis.hmget(key, fields);
                expire(shardedJedis, key, seconds);
                return list;
            }

            @Override
            List<String> except(JedisException e) {
                logger.error(buildError(key, seconds, fields), e);
                return null;
            }
        }.hook();
    }

    public List<String> hmget(String key, String... fields) {
        return this.hmget(key, null, fields);
    }

    /**
     * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略
     * @param key
     * @param seconds
     * @param fields
     * @return 被成功移除的域的数量，不包括被忽略的域
     */
    public Long hdel(final String key, final Integer seconds, final String... fields) {
        return new JedisHook<Long>(this) {
            @Override
            Long operate(ShardedJedis shardedJedis) {
                Long rtn = shardedJedis.hdel(key, fields);
                expire(shardedJedis, key, seconds);
                return rtn;
            }

            @Override
            Long except(JedisException e) {
                logger.error(buildError(key, seconds, fields), e);
                return null;
            }
        }.hook();
    }

    public Long hdel(String key, String... fields) {
        return this.hdel(key, null, fields);
    }

    /**
     * 返回哈希表 key 中域的数量。
     * @param key
     * @param seconds
     * @return 哈希表中域的数量，当 key 不存在时，返回 0 。
     */
    public Long hlen(final String key, final Integer seconds) {
        return new JedisHook<Long>(this) {
            @Override
            Long operate(ShardedJedis shardedJedis) {
                Long rtn = shardedJedis.hlen(key);
                if (rtn != null && rtn != 0) {
                    // key存在时才设置失效时间
                    expire(shardedJedis, key, seconds);
                }
                return rtn;
            }

            @Override
            Long except(JedisException e) {
                logger.error(buildError(key, seconds), e);
                return null;
            }
        }.hook();
    }

    public Long hlen(String key) {
        return this.hlen(key, null);
    }

    /**
     * 查看哈希表 key 中，给定域 field 是否存在。
     * @param key
     * @param field
     * @param seconds
     * @return 返回值：true哈希表含有给定域；false哈希表不含有给定域（或key）不存在；
     */
    public boolean hexists(final String key, final String field, final Integer seconds) {
        return new JedisHook<Boolean>(this) {
            @Override
            Boolean operate(ShardedJedis shardedJedis) {
                boolean result = shardedJedis.hexists(key, field);
                expire(shardedJedis, key, seconds);
                return result;
            }

            @Override
            Boolean except(JedisException e) {
                logger.error(buildError(key, field, seconds), e);
                return false;
            }
        }.hook();
    }

    /**
     * 返回哈希表 key 中的所有域。
     * @param key
     * @param seconds
     * @return 一个包含哈希表中所有域的表。当 key 不存在时，返回一个空表。
     */
    public Set<String> hkeys(final String key, final Integer seconds) {
        return new JedisHook<Set<String>>(this) {
            @Override
            Set<String> operate(ShardedJedis shardedJedis) {
                Set<String> keys = shardedJedis.hkeys(key);
                if (keys != null && !keys.isEmpty()) {
                    // 存在时才设置失效时间
                    expire(shardedJedis, key, seconds);
                }
                return keys;
            }

            @Override
            Set<String> except(JedisException e) {
                logger.error(buildError(key, seconds), e);
                return null;
            }
        }.hook();
    }

    /**
     * 批量获取
     * @param queryParams
     * @return
     */
    public List<Object> hmget(final Map<String, String[]> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) return null;

        return new JedisHook<List<Object>>(this) {
            @Override
            List<Object> operate(ShardedJedis shardedJedis) {
                ShardedJedisPipeline pipeline = shardedJedis.pipelined();
                //Map<String, Response<List<String>>> result = new HashMap<>();
                for (Entry<String, String[]> entry : queryParams.entrySet()) {
                    /*Response<List<String>> resp = pipeline.hmget(entry.getKey(), entry.getValue());
                    result.put(entry.getKey(), resp);*/
                    pipeline.hmget(entry.getKey(), entry.getValue());
                }
                return pipeline.syncAndReturnAll();
            }

            @Override
            List<Object> except(JedisException e) {
                logger.error(buildError(queryParams), e);
                return null;
            }
        }.hook();
    }

}
