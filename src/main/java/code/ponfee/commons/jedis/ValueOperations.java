package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.math.Numbers;
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
    public boolean set(String key, String value, int seconds) {
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
    public String get(String key, Integer seconds) {
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
    public List<String> gets(String keyWildcard) {
        return hook(shardedJedis -> {
            CompletionService<List<String>> service = new ExecutorCompletionService<>(EXECUTOR);
            int number = 0;
            for (Jedis jedis : shardedJedis.getAllShards()) {
                Set<String> keys = jedis.keys(keyWildcard);
                if (keys == null || keys.isEmpty()) {
                    continue;
                }

                service.submit(new Callable<List<String>>() {
                    @Override
                    public List<String> call() throws Exception {
                        // 相应分片上获取值
                        return jedis.mget(keys.toArray(new String[keys.size()]));
                    }
                });
                number++;
            }
            List<String> result = new ArrayList<>();
            for (; number > 0; number--) {
                try {
                    List<String> list = service.take().get();
                    if (list != null && !list.isEmpty()) {
                        result.addAll(list);
                    }
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
    public boolean setLong(String key, long value, int seconds) {
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

    public Long getLong(String key, Integer seconds) {
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

    public String getSet(String key, String value, int seconds) {
        return hook(shardedJedis -> {
            String oldValue = shardedJedis.getSet(key, value);
            expireForce(shardedJedis, key, seconds);
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
    public boolean setnx(String key, String value, int seconds) {
        return hook(shardedJedis -> {
            Long result = shardedJedis.setnx(key, value);
            if (Numbers.equals(result, 1)) {
                // 设置成功则需要设置失效期
                expireForce(shardedJedis, key, seconds);
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

    public Long incrBy(String key, int step, Integer seconds) {
        return hook(shardedJedis -> {
            Long rtn = shardedJedis.incrBy(key, step);
            expireForce(shardedJedis, key, seconds);
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

    public Double incrByFloat(String key, double step, Integer seconds) {
        return hook(shardedJedis -> {
            Double rtn = shardedJedis.incrByFloat(key, step);
            expireForce(shardedJedis, key, seconds);
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

    public Long decrBy(String key, int step, Integer seconds) {
        return hook(shardedJedis -> {
            Long rtn = shardedJedis.decrBy(key, step);
            expireForce(shardedJedis, key, seconds);
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
    public <T extends Object> boolean setObject(byte[] key, T t,
                                                boolean isCompress, int seconds) {
        if (t == null) {
            return false;
        }

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
    public <T extends Object> T getObject(byte[] key, Class<T> clazz, 
                                          boolean isCompress, Integer seconds) {
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
    public boolean set(byte[] key, byte[] value, boolean isCompress, int seconds) {
        if (value == null || key == null) {
            return false;
        }

        byte[] _value = isCompress ? GzipProcessor.compress(value) : value;

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
    public byte[] get(byte[] key, boolean isCompress, Integer seconds) {
        if (key == null) {
            return null;
        }

        return hook(shardedJedis -> {
            byte[] result = shardedJedis.get(key);
            if (result != null) {
                if (isCompress) {
                    result = GzipProcessor.decompress(result);
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
    public Map<String, String> mget(String... keys) {
        if (keys == null) {
            return null;
        }

        return hook(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (jedisList == null || jedisList.isEmpty()) {
                return null;
            }

            Map<String, String> resultMap;
            if (jedisList.size() < keys.length) { // key数量大于分片数量，则采用mget方式
                resultMap = new ConcurrentHashMap<>();
                CompletionService<List<String>> service = new ExecutorCompletionService<>(EXECUTOR);
                int number = jedisList.size();
                for (Jedis jedis : jedisList) {
                    service.submit(new Callable<List<String>>() {
                        @Override
                        public List<String> call() throws Exception {
                            return jedis.mget(keys);
                        }
                    });
                }
                for (; number > 0; number--) {
                    try {
                        // 所有的 future get 等待
                        List<String> list = service.take().get();
                        if (list == null || list.isEmpty()) {
                            continue;
                        }
                        String s;
                        for (int i = 0; i < keys.length; i++) {
                            s = list.get(i);
                            if (s != null && !resultMap.containsKey(keys[i])) {
                                resultMap.put(keys[i], s);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Jedis mget occur error", e);
                    }
                }
            } else { // 直接获取，不用mget方式
                resultMap = new HashMap<>();
                for (String k : keys) {
                    String v = shardedJedis.get(k);
                    if (v != null) {
                        resultMap.put(k, v);
                    }
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
    public Map<byte[], byte[]> mget(boolean isCompress, byte[]... keys) {
        if (keys == null) {
            return null;
        }

        return hook(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (jedisList == null || jedisList.isEmpty()) {
                return null;
            }

            Map<byte[], byte[]> resultMap;
            if (jedisList.size() < keys.length) { // key数量大于分片数量，则采用mget方式
                resultMap = new ConcurrentHashMap<>();
                CompletionService<List<byte[]>> service = new ExecutorCompletionService<>(EXECUTOR);
                int number = jedisList.size();
                for (Jedis jedis : jedisList) {
                    service.submit(new Callable<List<byte[]>>() {
                        @Override
                        public List<byte[]> call() throws Exception {
                            return jedis.mget((byte[][]) keys);
                        }
                    });
                }
                for (; number > 0; number--) {
                    try {
                        // 获取异步执行的返回数据
                        byte[] v;
                        List<byte[]> list = service.take().get();
                        if (list == null || list.isEmpty()) {
                            continue;
                        }
                        for (int i = 0; i < keys.length; i++) {
                            v = list.get(i);
                            if (v != null && !resultMap.containsKey(keys[i])) {
                                if (isCompress) {
                                    v = GzipProcessor.decompress(v);
                                }
                                resultMap.put(keys[i], v);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Jedis mget occur error", e);
                    }
                }
            } else { // 直接获取，不用mget方式
                resultMap = new HashMap<>();
                byte[] v;
                for (byte[] k : (byte[][]) keys) {
                    v = shardedJedis.get(k);
                    if (v == null) {
                        continue;
                    }
                    if (isCompress) {
                        v = GzipProcessor.decompress(v);
                    }
                    resultMap.put(k, v);
                }
            }
            return resultMap;
        }, null, isCompress, keys);
    }

    public Map<byte[], byte[]> mget(byte[]... keys) {
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
        if (datas == null || datas.isEmpty()) {
            return null;
        }

        HashMap<byte[], T> result = new HashMap<>();
        for (Entry<byte[], byte[]> entry : datas.entrySet()) {
            T t = jedisClient.deserialize(entry.getValue(), clazz, isCompress);
            if (t != null) {
                result.put(entry.getKey(), t);
            }
        }
        return result;
    }

    public <T extends Object> Map<byte[], T> mgetObject(Class<T> clazz, byte[]... keys) {
        return this.mgetObject(clazz, true, keys);
    }
}
