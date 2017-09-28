package code.ponfee.commons.jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

    private final static String SEPARATOR = ";";
    private final static int DEFAULT_TIMEOUT_MILLIS = 2000; // default 2000 millis timeout
    private static final int MAX_BYTE_LEN = 40; // max bytes length
    private static final int MAX_LEN = 50; // max str length
    private static Logger logger = LoggerFactory.getLogger(JedisClient.class);

    private Pool<ShardedJedis> shardedJedisPool;
    private Serializer serializer;
    private KeysOperations keysOps;
    private ValueOperations valueOps;
    private HashOperations hashOps;
    private ListOperations listOps;
    private SetOpertions setOps;
    private ZSetOperations zsetOps;
    private ScriptOperations scriptOps;
    private MQOperations mqOps;

    // -----------------------------------ShardedJedisPool（分片模式）-----------------------------------
    public JedisClient(final GenericObjectPoolConfig poolCfg, String hosts) {
        this(poolCfg, hosts, DEFAULT_TIMEOUT_MILLIS, null);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, 
                       String hosts, int timeout) {
        this(poolCfg, hosts, timeout, null);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, 
                       String hosts, Serializer serializer) {
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
    public JedisClient(final GenericObjectPoolConfig poolCfg, String hosts, 
                       int timeout, Serializer serializer) {
        List<JedisShardInfo> infos = new ArrayList<>();
        for (String str : hosts.split(SEPARATOR)) {
            if (StringUtils.isBlank(str)) continue;

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
            if (StringUtils.isNotBlank(password)) {
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
    public JedisClient(final GenericObjectPoolConfig poolCfg, 
                       String masters, String sentinels) {
        this(poolCfg, masters, sentinels, DEFAULT_TIMEOUT_MILLIS, null, null);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, String masters, 
                       String sentinels, int timeout) {
        this(poolCfg, masters, sentinels, timeout, null, null);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, String masters, 
                       String sentinels, Serializer serializer) {
        this(poolCfg, masters, sentinels, DEFAULT_TIMEOUT_MILLIS, null, serializer);
    }

    public JedisClient(final GenericObjectPoolConfig poolCfg, String masters, 
                       String sentinels, String password) {
        this(poolCfg, masters, sentinels, DEFAULT_TIMEOUT_MILLIS, password, null);
    }

    /**
     * @param poolCfg    连接池
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

    /**
     * init sharded jedis
     * @param shardedJedisPool
     * @param serializer
     */
    private void init(Pool<ShardedJedis> shardedJedisPool, Serializer serializer) {
        this.serializer = (serializer != null) 
                          ? serializer 
                          : new FstSerializer();

        this.shardedJedisPool = shardedJedisPool;
        this.keysOps = new KeysOperations(this);
        this.valueOps = new ValueOperations(this);
        this.hashOps = new HashOperations(this);
        this.listOps = new ListOperations(this);
        this.setOps = new SetOpertions(this);
        this.zsetOps = new ZSetOperations(this);
        this.scriptOps = new ScriptOperations(this);
        this.mqOps = new MQOperations(this);
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

    public ScriptOperations scriptOps() {
        return this.scriptOps;
    }

    public MQOperations mqOps() {
        return this.mqOps;
    }

    @Override
    public void destroy() {
        if (shardedJedisPool != null && !shardedJedisPool.isClosed()) {
            shardedJedisPool.close();
            shardedJedisPool = null;
        }
    }

    ShardedJedis getShardedJedis() throws JedisException {
        return this.shardedJedisPool.getResource();
    }

    /**
     * 调用勾子函数：有返回值
     * @param hook              勾子对象
     * @param occurErrorRtnVal  出现异常时的返回值
     * @param args              参数
     * @return
     */
    public final <T> T hook(JedisHook<T> hook, T occurErrorRtnVal, Object... args) {
        return hook.hook(this, occurErrorRtnVal, args);
    }

    /**
     * 调用勾子函数：无返回值
     * @param call 调用勾子函数
     * @param args 参数列表
     */
    public final void call(JedisCall call, Object... args) {
        call.call(this, args);
    }

    final void exception(Exception e, Object... args) {
        //StackTraceElement[] st = Thread.currentThread().getStackTrace();
        //builder.append(st[p].getClassName()).append(".").append(st[p].getMethodName()).append("(");
        StringBuilder builder = new StringBuilder("redis operation occur error, args(");
        String arg;
        for (int n = args.length, i = 0; i < n; i++) {
            if (args[i] == null) {
                arg = "null";
            } else if (i == 0 && (args[i] instanceof byte[] || args[i] instanceof Byte[])) {
                byte[] bytes;
                if (args[i] instanceof Byte[]) {
                    Byte[] b = (Byte[]) args[i];
                    bytes = new byte[b.length > MAX_BYTE_LEN ? MAX_BYTE_LEN : b.length];
                    for (int j = 0; j < bytes.length; j++) {
                        bytes[i] = b[i];
                    }
                } else {
                    bytes = (byte[]) args[i];
                }
                arg = toString(bytes); // redis key base64编码
            } else {
                arg = args[i].toString();
            }

            if (arg.length() > MAX_LEN) {
                arg = arg.substring(0, MAX_LEN - 3) + "...";
            }
            builder.append("`").append(arg).append("`");
            if (i != n - 1) {
                builder.append(", ");
            }
        }
        logger.error(builder.append(")").toString(), e);
    }

    private String toString(byte[] bytes) {
        if (bytes.length > MAX_BYTE_LEN) {
            bytes = ArrayUtils.subarray(bytes, 0, MAX_BYTE_LEN);
        }
        return "b64:" + Base64.getEncoder().encodeToString(bytes);
    }

    /*void closeShardedJedis(ShardedJedis shardedJedis) {
        if (shardedJedis != null) try {
            shardedJedis.close();
            //shardedJedis.disconnect();
        } catch (Throwable e) {
            logger.error("redis close occur error", e);
        }
    }
    
    Jedis getJedis(String key) {
        return this.getShardedJedis().getShard(key);
    }
    
    void closeJedis(Jedis jedis) {
        jedis.close();
        //jedis.disconnect();
    }*/

    final <T> byte[] serialize(T t, boolean isCompress) {
        return serializer.serialize(t, isCompress);
    }

    final <T> byte[] serialize(T t) {
        return this.serialize(t, true);
    }

    final <T> T deserialize(byte[] data, Class<T> clazz, boolean isCompress) {
        return serializer.deserialize(data, clazz, isCompress);
    }

    final <T> T deserialize(byte[] data, Class<T> clazz) {
        return this.deserialize(data, clazz, true);
    }

}
