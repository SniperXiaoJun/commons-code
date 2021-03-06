package code.ponfee.commons.jedis;

import code.ponfee.commons.io.GzipProcessor;
import code.ponfee.commons.math.Numbers;
import code.ponfee.commons.util.ObjectUtils;
import org.apache.commons.collections4.CollectionUtils;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * redis string（字符串）操作类
 * @author fupf
 */
public class ValueOperations extends JedisOperations {

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
        return call(shardedJedis -> {
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
        return call(shardedJedis -> {
            String value = shardedJedis.get(key);
            if (value != null) {
                // 存在则设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return value;
        }, null, key, seconds);
    }

    /**
     * 设置并删除
     * @param key
     * @return
     */
    public String getAndDel(String key) {
        return call(shardedJedis -> {
            String value = shardedJedis.get(key);
            if (value != null) {
                shardedJedis.del(key);
            }
            return value;
        }, null, key);
    }

    /**
     * 通配符获取值
     * @param keyWildcard
     * @return
     */
    public Set<String> getWithWildcard(String keyWildcard) {
        return call(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (CollectionUtils.isEmpty(jedisList)) {
                return null;
            }
            List<CompletableFuture<List<String>>> list = jedisList.stream().map(
                jedis -> CompletableFuture.supplyAsync(
                    () -> jedis.keys(keyWildcard), EXECUTOR
                ).thenCompose(
                    keys -> CompletableFuture.supplyAsync(
                        () -> CollectionUtils.isEmpty(keys) ? null : 
                            jedis.mget(keys.toArray(new String[keys.size()])), 
                        EXECUTOR
                    )
                )
            ).collect(Collectors.toList());
            return list.stream()
                       .map(CompletableFuture::join)
                       .filter(CollectionUtils::isNotEmpty)
                       .collect(HashSet::new, HashSet::addAll, HashSet::addAll);
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
        return call(shardedJedis -> {
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
        return call(shardedJedis -> {
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
        return call(shardedJedis -> {
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
        return call(shardedJedis -> {
            boolean flag = Numbers.equals(shardedJedis.setnx(key, value), 1);
            if (flag) {
                expireForce(shardedJedis, key, seconds); // 设置成功则需要设置失效期
            }
            return flag;
        }, false, key, value, seconds);
    }

    /**
     * 将 key中储存的数字值增一，如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
     * @param key
     * @return 执行 INCR 命令之后 key 的值
     */
    public Long incrBy(String key) {
        return incrBy(key, 1, null);
    }

    public Long incrBy(String key, int step) {
        return incrBy(key, step, null);
    }

    public Long incrBy(String key, int step, Integer seconds) {
        return call(shardedJedis -> {
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
        return call(shardedJedis -> {
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
        return decrBy(key, 1, null);
    }

    public Long decrBy(String key, int step) {
        return decrBy(key, step, null);
    }

    public Long decrBy(String key, int step, Integer seconds) {
        return call(shardedJedis -> {
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
    public boolean setObject(byte[] key, Object t,
                             boolean isCompress, int seconds) {
        if (t == null) {
            return false;
        }

        return call(shardedJedis -> {
            byte[] data = jedisClient.serialize(t, isCompress);
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), data);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, t, isCompress, seconds);
    }

    public boolean setObject(byte[] key, Object t, boolean isCompress) {
        return setObject(key, t, isCompress, DEFAULT_EXPIRE_SECONDS);
    }

    public boolean setObject(byte[] key, Object t, int seconds) {
        return setObject(key, t, false, seconds);
    }

    public boolean setObject(byte[] key, Object t) {
        return setObject(key, t, false, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 对象序例化并缓存
     * @param key
     * @param t
     * @param isCompress
     * @param seconds
     * @return
     */
    public boolean setObject(String key, Object t,
                             boolean isCompress, int seconds) {
        return setObject(key.getBytes(StandardCharsets.UTF_8), t, isCompress, seconds);
    }

    public boolean setObject(String key, Object t, boolean isCompress) {
        return setObject(key, t, isCompress, DEFAULT_EXPIRE_SECONDS);
    }

    public boolean setObject(String key, Object t, int seconds) {
        return setObject(key, t, false, seconds);
    }

    public boolean setObject(String key, Object t) {
        return setObject(key, t, false, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 获取缓存数据并反序例化为对象
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return
     */
    public <T> T getObject(byte[] key, Class<T> clazz, 
                           boolean isCompress, Integer seconds) {
        return call(shardedJedis -> {
            T t = jedisClient.deserialize(shardedJedis.get(key), clazz, isCompress);
            if (t != null) {
                // 存在则设置失效时间
                expire(shardedJedis, key, seconds);
            }
            return t;
        }, null, key, clazz, isCompress, seconds);
    }

    public <T> T getObject(byte[] key, Class<T> clazz, boolean isCompress) {
        return getObject(key, clazz, isCompress, null);
    }

    public <T> T getObject(byte[] key, Class<T> clazz, Integer seconds) {
        return getObject(key, clazz, false, seconds);
    }

    public <T> T getObject(byte[] key, Class<T> clazz) {
        return getObject(key, clazz, false, null);
    }

    /**
     * 获取缓存数据并反序例化为对象
     * 
     * @param key
     * @param clazz
     * @param isCompress
     * @param seconds
     * @return
     */
    public <T> T getObject(String key, Class<T> clazz, 
                           boolean isCompress, Integer seconds) {
        return getObject(key.getBytes(StandardCharsets.UTF_8), clazz, isCompress, seconds);
    }

    public <T> T getObject(String key, Class<T> clazz, boolean isCompress) {
        return getObject(key, clazz, isCompress, null);
    }

    public <T> T getObject(String key, Class<T> clazz, Integer seconds) {
        return getObject(key, clazz, false, seconds);
    }

    public <T> T getObject(String key, Class<T> clazz) {
        return getObject(key, clazz, false, null);
    }

    public boolean set(byte[] key, byte[] value, int seconds) {
        if (value == null || key == null) {
            return false;
        }
        return call(shardedJedis -> {
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), value);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value, seconds);
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

        byte[] value0 = isCompress ? GzipProcessor.compress(value) : value;

        return call(shardedJedis -> {
            String rtn = shardedJedis.setex(key, getActualExpire(seconds), value0);
            return SUCCESS_MSG.equalsIgnoreCase(rtn);
        }, false, key, value0, isCompress, seconds);
    }

    public boolean set(String key, byte[] value, int seconds) {
        return this.set(key.getBytes(), value, false, seconds);
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

        return call(shardedJedis -> {
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
        return this.get(key, false, null);
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

        return call(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (CollectionUtils.isEmpty(jedisList)) {
                return null;
            }

            if (jedisList.size() < keys.length / BATCH_MULTIPLE) { // key数量大于分片数量倍数，则采用mget方式
                Map<String, String> resultMap = new ConcurrentHashMap<>();
                List<CompletableFuture<Void>> list = jedisList.stream().map(
                  jedis -> CompletableFuture.supplyAsync(() -> jedis.mget(keys), EXECUTOR)
                ).map(future -> future.thenAccept(values -> { // 同步
                    String value;
                    for (int i = 0; i < keys.length; i++) {
                        if ((value = values.get(i)) != null) {
                            resultMap.putIfAbsent(keys[i], value);
                        }
                    }
                })).collect(Collectors.toList());
                list.forEach(CompletableFuture::join);
                return resultMap;
            } else { // 直接获取，不用mget方式
                Map<String, String> result = new HashMap<>();
                for (String key : keys) {
                    String value = shardedJedis.get(key);
                    if (value != null) {
                        result.putIfAbsent(key, value);
                    }
                }
                return result;
            }
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

        return call(shardedJedis -> {
            Collection<Jedis> jedisList = shardedJedis.getAllShards();
            if (CollectionUtils.isEmpty(jedisList)) {
                return null;
            }

            if (jedisList.size() < keys.length / BATCH_MULTIPLE) { // key数量大于分片数量倍数，则采用mget方式
                Map<byte[], byte[]> resultMap = new ConcurrentHashMap<>();
                List<CompletableFuture<Void>> list = jedisList.stream().map(
                  jedis -> CompletableFuture.supplyAsync(() -> jedis.mget(keys), EXECUTOR)
                ).map(future -> future.thenAccept(values -> { // 同步
                    for (int i = 0; i < keys.length; i++) {
                        byte[] value;
                        if ((value = values.get(i)) != null) {
                            resultMap.putIfAbsent(keys[i], value);
                        }
                    }
                })).collect(Collectors.toList());

                //CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()])).join();
                list.forEach(CompletableFuture::join);
                return resultMap;
            } else { // 直接获取，不用mget方式
                /*return Stream.of(keys).collect(Collectors.toMap(
                     Function.identity(), 
                     k -> CompletableFuture.supplyAsync(() -> shardedJedis.get(k), EXECUTOR)
                 )).entrySet().stream().collect(
                     Collectors.toMap(Entry::getKey, e -> e.getValue().join())
                 );*/
                Map<byte[], byte[]> result = new HashMap<>();
                for (byte[] key : keys) {
                    byte[] value = shardedJedis.get(key);
                    if (value != null) {
                        result.putIfAbsent(key, value);
                    }
                }
                return result;
            }
        }, null, isCompress, keys);
    }

    public Map<byte[], byte[]> mget(byte[]... keys) {
        return this.mget(false, keys);
    }

    /**
     * 批量获取
     * @param clazz
     * @param keys
     * @return
     */
    public <T> Map<byte[], T> mgetObject(Class<T> clazz, boolean isCompress, byte[]... keys) {
        Map<byte[], byte[]> datas = this.mget(false, keys);
        if (datas == null || datas.isEmpty()) {
            return null;
        }
        return datas.entrySet().stream().filter(
            e -> ObjectUtils.isNotNull(e.getValue())
        ).collect(Collectors.toMap(
            Entry::getKey, 
            e -> jedisClient.deserialize(e.getValue(), clazz, isCompress)
        ));
    }

    public <T> Map<byte[], T> mgetObject(Class<T> clazz, byte[]... keys) {
        return this.mgetObject(clazz, false, keys);
    }
}
