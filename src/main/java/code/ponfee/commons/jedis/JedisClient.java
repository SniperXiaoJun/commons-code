package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import code.ponfee.commons.serial.FstSerializer;
import code.ponfee.commons.serial.Serializer;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

/**
 * jedis客户端
 * @author fupf
 */
public class JedisClient implements DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(JedisClient.class);

    private final static String SEPARATOR = ";";
    private final static int DEFAULT_TIMEOUT_MILLIS = 2000; // default 2000 millis timeout

    private Pool<ShardedJedis> shardedJedisPool;
    private Serializer serializer;
    private KeysOperations keysOps;
    private ValueOperations valueOps;
    private HashOperations hashOps;
    private ListOperations listOps;
    private SetOpertions setOps;
    private ZSetOperations zsetOps;

    // -----------------------------------ShardedJedisPool（分片模式）-----------------------------------
    public JedisClient(final GenericObjectPoolConfig poolCfg, String hosts) {
        this(poolCfg, hosts, DEFAULT_TIMEOUT_MILLIS, null);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, String hosts, int timeout) {
        this(poolCfg, hosts, timeout, null);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, String hosts, Serializer serializer) {
        this(poolCfg, hosts, DEFAULT_TIMEOUT_MILLIS, serializer);
    }

    /**
     * <pre>
     *  ShardedJedis注入格式：
     *   host1:port1;host2:port2;host3:port3
     *   name1:host1:port1;name2:host2:port2;name3:host3:port3
     *   name1:host1:port1:password1;name2:host2:port2:password2;name3:host3:port3:password3
     * </pre>
     * @param poolCfg
     * @param hosts
     * @param timeout
     * @param serializer
     */
    public JedisClient(final GenericObjectPoolConfig poolCfg, String hosts, int timeout, Serializer serializer) {
        List<JedisShardInfo> infos = new ArrayList<>();
        for (String str : hosts.split(SEPARATOR)) {
            if (isBlank(str)) continue;
            String name, host, port, password = null;
            String[] array = str.split(":");
            if (array.length == 2) {
                host = array[0].trim();
                port = array[1].trim();
                name = host + ":" + port;
            } else if (array.length == 3 || array.length == 4) {
                name = array[0].trim();
                host = array[1].trim();
                port = array[2].trim();
                if (array.length == 4) {
                    password = array[3].trim();
                }
            } else {
                throw new IllegalArgumentException("invalid hosts config[" + hosts + "]");
            }
            JedisShardInfo info = new JedisShardInfo(host, Integer.parseInt(port), timeout, name);
            if (!isBlank(password)) {
                info.setPassword(password);
            }
            infos.add(info);
        }
        if (infos.isEmpty()) {
            throw new IllegalArgumentException("invalid hosts config[" + hosts + "]");
        }

        init(new ShardedJedisPool(poolCfg, infos), serializer);
    }

    // -----------------------------------ShardedJedisSentinelPool（哨兵+分片）-----------------------------------
    public JedisClient(final GenericObjectPoolConfig poolCfg, String masters, String sentinels) {
        this(poolCfg, masters, sentinels, DEFAULT_TIMEOUT_MILLIS, null, null);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, String masters, String sentinels, int timeout) {
        this(poolCfg, masters, sentinels, timeout, null, null);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, String masters, String sentinels, Serializer serializer) {
        this(poolCfg, masters, sentinels, DEFAULT_TIMEOUT_MILLIS, null, serializer);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, String masters, String sentinels, String password) {
        this(poolCfg, masters, sentinels, DEFAULT_TIMEOUT_MILLIS, password, null);
    }

    /**
     * 
     * @param poolCfg
     * @param masters    哨兵mastername名称，多个以“;”分隔，如：sen_redis_master1:sen_redis_master2
     * @param sentinels  哨兵服务器ip及端口，多个以“;”分隔，如：127.0.0.1:16379;127.0.0.1:16380;
     * @param timeout    超时时间
     * @param password   密码
     * @param serializer 序列化对象
     */
    public JedisClient(final GenericObjectPoolConfig poolCfg, String masters,
        String sentinels, int timeout, String password, Serializer serializer) {
        List<String> master = Arrays.asList(masters.split(SEPARATOR));
        Set<String> sentinel = new HashSet<>(Arrays.asList(sentinels.split(SEPARATOR)));

        init(new ShardedJedisSentinelPool(master, sentinel, poolCfg, timeout, password), serializer);
    }

    private void init(Pool<ShardedJedis> shardedJedisPool, Serializer serializer) {
        this.shardedJedisPool = shardedJedisPool;
        this.serializer = serializer != null ? serializer : new FstSerializer();
        this.keysOps = new KeysOperations(this);
        this.valueOps = new ValueOperations(this);
        this.hashOps = new HashOperations(this);
        this.listOps = new ListOperations(this);
        this.setOps = new SetOpertions(this);
        this.zsetOps = new ZSetOperations(this);
    }

    public KeysOperations keysOps() {
        return this.keysOps;
    }

    public ValueOperations valueOps() {
        return this.valueOps;
    }

    public HashOperations hashOps() {
        return this.hashOps;
    }

    public ListOperations listOps() {
        return this.listOps;
    }

    public SetOpertions setOps() {
        return this.setOps;
    }

    public ZSetOperations zsetOps() {
        return this.zsetOps;
    }

    ShardedJedis getShardedJedis() throws JedisException {
        return this.shardedJedisPool.getResource();
    }

    void closeShardedJedis(ShardedJedis shardedJedis) {
        if (shardedJedis != null) try {
            shardedJedis.close();
        } catch (/*JedisException*/Throwable e) {
            logger.error("redis close occur error", e);
        }
    }

    public <T extends Object> byte[] serialize(T t, boolean isCompress) {
        return serializer.serialize(t, isCompress);
    }

    public <T extends Object> byte[] serialize(T t) {
        return this.serialize(t, true);
    }

    public <T extends Object> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        return serializer.deserialize(data, clazz, isCompress);
    }

    public final <T extends Object> T deserialize(byte[] data, Class<T> clazz) {
        return this.deserialize(data, clazz, true);
    }

    public @Override void destroy() {
        if (shardedJedisPool != null && !shardedJedisPool.isClosed()) {
            shardedJedisPool.close();
        }
        this.shardedJedisPool = null;
        this.serializer = null;
        this.keysOps = null;
        this.valueOps = null;
        this.hashOps = null;
        this.listOps = null;
        this.zsetOps = null;
        this.setOps = null;
    }

    private static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
