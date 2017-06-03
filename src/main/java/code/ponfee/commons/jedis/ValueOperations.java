package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.serial.Serializer;
import redis.clients.jedis.Jedis;

/**
 * redis string（字符串）操作类
 * @author fupf
 */
public class ValueOperations extends JedisOperations {
    private static Logger logger = LoggerFactory.getLogger(ValueOperations.class);

    ValueOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * SET 在设置操作成功完成时，才返回 OK 。
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, String value) {
        return set(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 将值value关联到key，并将key的生存时间设为seconds(以秒为单位)。
     * 如果 key 已经存在， SETEX 命令将覆写旧值。
     * SET('key','value')+EXPIRE('key','seconds')
     * @param key
     * @param value
     * @param seconds
     * @return 是否设置成功
     */
    public boolean set(final String key, final String value, final int seconds) {
        return hook(shardedJedis -> {
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), value);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value, seconds);
    }

    public String get(String key) {
        return get(key, null);

    }

    /***
     * 获取值
     * @param key
     * @param seconds
     * @return
     */
    public String get(final String key, final Integer seconds) {
        return hook(shardedJedis -> {
            String value = shardedJedis.get(key);
            if (value != null) {
                // 存在则设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return value;
        }, null, key, seconds);
    }

    /**
     * 通配符获取值
     * @param keyWildcard
     * @return
     */
    public List<String> gets(final String keyWildcard) {
        return hook(shardedJedis -> {
            List<Future<List<String>>> futureList = new ArrayList<>();
            for (final Jedis jedis : shardedJedis.getAllShards()) {
                final Set<String> keys = jedis.keys(keyWildcard);
                if (keys == null || keys.isEmpty()) continue;
                futureList.add(EXECUTOR.submit(new Callable<List<String>>() {
                    @Override
                    public List<String> call() throws Exception {
                        // 相应分片上获取值
                        return jedis.mget(keys.toArray(new String[keys.size()]));
                    }
                }));
            }
            List<String> result = new ArrayList<>();
            for (Future<List<String>> future : futureList) {
                try {
                    List<String> list = future.get(FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
                    if (list == null || list.isEmpty()) continue;
                    result.addAll(list);
                } catch (TimeoutException e) {
                    logger.error("Jedis mget timeout", e);
                } catch (Exception e) {
                    logger.error("Jedis mget occur error", e);
                }
            }
            return result;
        }, null, keyWildcard);
    }

    /**
     * SET 在设置操作成功完成时，才返回 OK 。
     * @param key
     * @param value
     * @return 是否设置成功
     */
    public boolean setLong(String key, long value) {
        return setLong(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * SET 在设置操作成功完成时，才返回 OK 。
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public boolean setLong(final String key, final long value, final int seconds) {
        return hook(shardedJedis -> {
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), String.valueOf(value));
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value, seconds);
    }

    /**
     * 获取long值
     * @param key
     * @return
     */
    public Long getLong(String key) {
        return getLong(key, null);
    }

    public Long getLong(final String key, final Integer seconds) {
        return hook(shardedJedis -> {
            Long number = null;
            String value = shardedJedis.get(key);
            if (value != null) {
                // 存在则设置失效时间
                number = Long.parseLong(value);
                expire(shardedJedis, key, seconds);
            }
            return number;
        }, null, key, seconds);
    }

    /**
     * 设置新值，返回旧值
     * @param key
     * @param value
     * @return
     */
    public String getSet(String key, String value) {
        return getSet(key, value, DEFAULT_EXPIRE_SECONDS);
    }

    public String getSet(final String key, final String value, final int seconds) {
        return hook(shardedJedis -> {
            String oldValue = shardedJedis.getSet(key, value);
            expire(shardedJedis, key, seconds);
            return oldValue;
        }, null, key, value, seconds);
    }

    /**
     * <pre>
     *  将 key的值设为value，当且仅当key不存在；若给定的 key已经存在，则 SETNX不做任何动作。
     *  返回：1成功；0失败；
     * </pre>
     * 
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public boolean setnx(final String key, final String value, final int seconds) {
        return hook(shardedJedis -> {
            Long result = shardedJedis.setnx(key, value);
            if (JedisOperations.equals(result, 1)) {
                // 设置成功则需要设置失效期
                expire(shardedJedis, key, seconds);
                return true;
            } else {
                return false;
            }
        }, false, key, value, seconds);
    }

    /**
     * 将 key中储存的数字值增一，如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
     * @param key
     * @param step
     * @param seconds
     * @return 执行 INCR 命令之后 key 的值
     */
    public Long incrBy(String key) {
        return incrBy(key, 1);
    }

    public Long incrBy(String key, int step) {
        return incrBy(key, step, null);
    }

    public Long incrBy(final String key, final int step, final Integer seconds) {
        return hook(shardedJedis -> {
            Long rtn = shardedJedis.incrBy(key, step);
            expire(shardedJedis, key, seconds);
            return rtn;
        }, null, key, step, seconds);
    }

    /**
     * 为 key 中所储存的值加上浮点数增量 increment，如果 key 不存在，那么 INCRBYFLOAT 会先将 key 的值设为 0 ，再执行加法操作。
     * @param key
     * @param step
     * @return
     */
    public Double incrByFloat(String key, double step) {
        return incrByFloat(key, step, null);
    }

    public Double incrByFloat(final String key, final double step, final Integer seconds) {
        return hook(shardedJedis -> {
            Double rtn = shardedJedis.incrByFloat(key, step);
            expire(shardedJedis, key, seconds);
            return rtn;
        }, null, key, step, seconds);
    }

    /**
     * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECRBY 操作。
     * @param key
     * @return
     */
    public Long decrBy(String key) {
        return decrBy(key, 1);
    }

    public Long decrBy(String key, int step) {
        return decrBy(key, step, null);
    }

    public Long decrBy(final String key, final int step, final Integer seconds) {
        return hook(shardedJedis -> {
            Long rtn = shardedJedis.decrBy(key, step);
            expire(shardedJedis, key, seconds);
            return rtn;
        }, null, key, step, seconds);
    }

    /**
     * 对象序例化并缓存
     * @param key
     * @param t
     * @param isCompress
     * @param seconds
     * @return
     */
    public <T extends Object> boolean setObject(final byte[] key, final T t,
        final boolean isCompress, final int seconds) {
        if (t == null) return false;

        return hook(shardedJedis -> {
            byte[] data = jedisClient.serialize(t, isCompress);
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), data);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, t, isCompress, seconds);
    }

    public <T extends Object> boolean setObject(byte[] key, T t, boolean isCompress) {
        return setObject(key, t, isCompress, DEFAULT_EXPIRE_SECONDS);
    }

    public <T extends Object> boolean setObject(byte[] key, T t, int seconds) {
        return setObject(key, t, true, seconds);
    }

    public <T extends Object> boolean setObject(byte[] key, T t) {
        return setObject(key, t, true, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 获取缓存数据并反序例化为对象
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return
     */
    public <T extends Object> T getObject(final byte[] key,
        final Class<T> clazz, final boolean isCompress, final Integer seconds) {
        return hook(shardedJedis -> {
            T t = jedisClient.deserialize(shardedJedis.get(key), clazz, isCompress);
            if (t != null) {
                // 存在则设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return t;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T extends Object> T getObject(byte[] key, Class<T> clazz, boolean isCompress) {
        return getObject(key, clazz, isCompress, null);
    }

    public <T extends Object> T getObject(byte[] key, Class<T> clazz, Integer seconds) {
        return getObject(key, clazz, true, seconds);
    }

    public <T extends Object> T getObject(byte[] key, Class<T> clazz) {
        return getObject(key, clazz, true, null);
    }

    /**
     * 缓存流数据
     * @param key
     * @param value
     * @param isCompress
     * @param seconds
     * @return
     */
    public boolean set(final byte[] key, final byte[] value,
        final boolean isCompress, final int seconds) {
        if (value == null || key == null) return false;

        final byte[] _value;
        if (!isCompress) _value = value;
        else _value = Serializer.compress(value);

        return hook(shardedJedis -> {
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), _value);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, _value, isCompress, seconds);
    }

    public boolean set(String key, byte[] value, int seconds) {
        return this.set(key.getBytes(), value, true, seconds);
    }

    /**
     * 获取流数据
     * @param key
     * @param isCompress
     * @param seconds
     * @return
     */
    public byte[] get(final byte[] key, final boolean isCompress, final Integer seconds) {
        if (key == null) return null;

        return hook(shardedJedis -> {
            byte[] result = shardedJedis.get(key);
            if (result != null) {
                if (isCompress) {
                    result = Serializer.decompress(result);
                }
                expire(shardedJedis, key, seconds);
            }
            return result;
        }, null, key, isCompress, seconds);
    }

    public byte[] get(byte[] key, boolean isCompress) {
        return this.get(key, isCompress, null);
    }

    public byte[] get(byte[] key) {
        return this.get(key, true, null);
    }

    /**
     * 批量获取值
     * @param keys
     * @return
     */
    public Map<String, String> mget(final String... keys) {
        if (keys == null) return null;

        return hook(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (jedisList == null || jedisList.isEmpty()) return null;

            Map<String, String> resultMap;
            if (jedisList.size() < keys.length) { // key数量大于分片数量，则采用mget方式
                resultMap = new ConcurrentHashMap<>();
                List<Future<List<String>>> futureList = new ArrayList<>();
                for (final Jedis jedis : jedisList) {
                    futureList.add(EXECUTOR.submit(new Callable<List<String>>() {
                        @Override
                        public List<String> call() throws Exception {
                            return jedis.mget(keys);
                        }
                    }));
                }
                for (Future<List<String>> future : futureList) {
                    try {
                        // 所有的 future get 等待
                        List<String> list = future.get(FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
                        if (list == null || list.isEmpty()) continue;
                        String s;
                        for (int i = 0; i < keys.length; i++) {
                            s = list.get(i);
                            if (s != null && !resultMap.containsKey(keys[i])) {
                                resultMap.put(keys[i], s);
                            }
                        }
                    } catch (TimeoutException e) {
                        logger.error("Jedis mget timeout", e);
                    } catch (Exception e) {
                        logger.error("Jedis mget occur error", e);
                    }
                }
            } else { // 直接获取，不用mget方式
                resultMap = new HashMap<>();
                for (String k : keys) {
                    String v = shardedJedis.get(k);
                    if (v != null) resultMap.put(k, v);
                }
            }
            return resultMap;
        }, null, String.valueOf(keys));
    }

    /**
     * 批量获取值
     * @param isCompress
     * @param keys
     * @return
     */
    public Map<byte[], byte[]> mget(final boolean isCompress, final byte[]... keys) {
        if (keys == null) return null;

        return hook(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (jedisList == null || jedisList.isEmpty()) return null;

            Map<byte[], byte[]> resultMap;
            if (jedisList.size() < keys.length) { // key数量大于分片数量，则采用mget方式
                resultMap = new ConcurrentHashMap<>();
                List<Future<List<byte[]>>> futureList = new ArrayList<>();
                for (final Jedis jedis : jedisList) {
                    futureList.add(EXECUTOR.submit(new Callable<List<byte[]>>() {
                        @Override
                        public List<byte[]> call() throws Exception {
                            return jedis.mget((byte[][]) keys);
                        }
                    }));
                }
                for (Future<List<byte[]>> future : futureList) {
                    try {
                        // 获取异步执行的返回数据
                        byte[] v;
                        List<byte[]> list = future.get(FUTURE_TIMEOUT, TimeUnit.MILLISECONDS);
                        if (list == null || list.isEmpty()) continue;
                        for (int i = 0; i < keys.length; i++) {
                            v = list.get(i);
                            if (v != null && !resultMap.containsKey(keys[i])) {
                                if (isCompress) {
                                    v = Serializer.decompress(v);
                                }
                                resultMap.put(keys[i], v);
                            }
                        }
                    } catch (TimeoutException e) {
                        logger.error("Jedis mget timeout", e);
                    } catch (Exception e) {
                        logger.error("Jedis mget occur error", e);
                    }
                }
            } else { // 直接获取，不用mget方式
                resultMap = new HashMap<>();
                byte[] v;
                for (byte[] k : (byte[][]) keys) {
                    v = shardedJedis.get(k);
                    if (v == null) continue;
                    if (isCompress) {
                        v = Serializer.decompress(v);
                    }
                    resultMap.put(k, v);
                }
            }
            return resultMap;
        }, null, isCompress, keys);
    }

    public Map<byte[], byte[]> mget(final byte[]... keys) {
        return this.mget(true, keys);
    }

    /**
     * 批量获取
     * @param clazz
     * @param keys
     * @return
     */
    public <T extends Object> Map<byte[], T> mgetObject(Class<T> clazz, boolean isCompress, byte[]... keys) {
        Map<byte[], byte[]> datas = this.mget(false, keys);
        if (datas == null || datas.isEmpty()) return null;

        HashMap<byte[], T> result = new HashMap<>();
        for (Entry<byte[], byte[]> entry : datas.entrySet()) {
            T t = jedisClient.deserialize(entry.getValue(), clazz, isCompress);
            if (t != null) result.put(entry.getKey(), t);
        }
        return result;
    }

    public <T extends Object> Map<byte[], T> mgetObject(Class<T> clazz, byte[]... keys) {
        return this.mgetObject(clazz, true, keys);
    }
}
