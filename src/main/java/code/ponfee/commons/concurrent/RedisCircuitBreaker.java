package code.ponfee.commons.concurrent;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.concurrent.AsyncBatchTransmitter;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;
import code.ponfee.commons.util.IdWorker;

/**
 * Redis熔断控制器
 * @author fupf
 */
public class RedisCircuitBreaker implements CircuitBreaker {

    private static final int EXPIRE_SECONDS = (int) TimeUnit.DAYS.toSeconds(30) + 1; // key的失效日期
    private static final String TRACE_KEY_PREFIX = "freq:trace:"; // 频率缓存key前缀
    private static final String THRESHOLD_KEY_PREFIX = "freq:threshold:"; // 次数缓存key前缀

    private final JedisClient jedisClient;
    private final JedisLock lock;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AsyncBatchTransmitter<Trace> transmitter;
    private final int clearBeforeMillis;
    private final Cache<Long> localCache = CacheBuilder.newBuilder().keepaliveInMillis(300000L) // 5 minutes of cache alive
                                                       .autoReleaseInSeconds(300).build(); // 5 minutes to release expire cache

    public RedisCircuitBreaker(JedisClient jedisClient, int clearBeforeHours, int autoClearInSeconds) {
        this.jedisClient = jedisClient;
        this.clearBeforeMillis = (int) TimeUnit.HOURS.toMillis(clearBeforeHours);

        // 定时清除记录(zrem range by score)，jedis:lock:freq:trace:clear
        this.lock = new JedisLock(jedisClient, TRACE_KEY_PREFIX + "clear", autoClearInSeconds / 2);
        this.executor.scheduleAtFixedRate(() -> {
            if (this.lock.tryLock()) { // 不用释放锁，让其自动超时
                long beforeTimeMillis = System.currentTimeMillis() - clearBeforeMillis;
                for (String key : jedisClient.keysOps().keys(TRACE_KEY_PREFIX + "*")) {
                    jedisClient.zsetOps().zremrangeByScore(key, 0, beforeTimeMillis);
                }
            }
        }, autoClearInSeconds, autoClearInSeconds, TimeUnit.SECONDS);

        // 批量记录
        this.transmitter = new AsyncBatchTransmitter<>((traces, isEnd) -> {
            return () -> {
                Map<String, Map<String, Double>> groups = new HashMap<>();
                Map<String, Double> batch;
                for (Trace trace : traces) {
                    batch = groups.get(trace.key);
                    if (batch == null) {
                        batch = new HashMap<>();
                        groups.put(trace.key, batch);
                    }
                    // ObjectUtils.uuid22()
                    batch.put(Long.toString(IdWorker.LOCAL_WORKER.nextId(), Character.MAX_RADIX), 
                              trace.timeMillis);
                }
                for (Entry<String, Map<String, Double>> entry : groups.entrySet()) {
                    // TRACE_KEY_PREFIX + trace.key
                    jedisClient.zsetOps().zadd(TRACE_KEY_PREFIX + entry.getKey(), 
                                               entry.getValue(), EXPIRE_SECONDS);
                }
                groups.clear();
                groups = null;
                traces.clear();
            };
        }, 100, 5000); // 100毫秒间隔，5000条∕次
    }

    /**
     * 校验并记录
     * @param key
     * @return 是否频繁访问：true是；false否；
     */
    public @Override boolean checkpoint(String key) {
        return checkpoint(key, getRequestThreshold(key));
    }

    public @Override boolean checkpoint(String key, long requestThreshold) {
        if (requestThreshold < 0) {
            return true; // 小于0表示无限制
        } else if (requestThreshold == 0) {
            return false; // 禁止访问
        }

        if (requestThreshold < countByLastTime(key, 1, TimeUnit.MINUTES)) {
            return false; // 超过频率
        } else {
            return transmitter.put(new Trace(key, System.currentTimeMillis()));
        }
    }

    public long countByLastTime(String key, int time, TimeUnit unit) {
        long now = System.currentTimeMillis();
        return countByRangeMillis(key, now - unit.toMillis(time), now);
    }

    public long countByRange(String key, Date from, Date to) {
        return countByRangeMillis(key, from.getTime(), to.getTime());
    }

    /**
     * 限制一分钟的访问频率
     * @param key
     * @param threshold
     * @return 是否设置成功：true是；false否；
     */
    public @Override boolean setRequestThreshold(String key, long threshold) {
        boolean flag = jedisClient.valueOps().setLong(THRESHOLD_KEY_PREFIX + key, threshold, EXPIRE_SECONDS);
        if (flag) {
            localCache.getAndRemove(key); // remove from local cache
        }

        return flag;
    }

    /**
     * 获取一分钟的限制频率量
     * @param key
     * @return
     */
    public @Override long getRequestThreshold(String key) {
        Long threshold = localCache.get(key);
        if (threshold == null) {
            threshold = jedisClient.valueOps().getLong(THRESHOLD_KEY_PREFIX + key, EXPIRE_SECONDS);
            if (threshold == null) {
                threshold = -1L; // -1表示无限制
            }
            localCache.set(key, threshold); // put into local cache
        }
        return threshold;
    }

    /**
     * 销毁
     */
    public void destory() {
        localCache.destroy();
        executor.isShutdown();
    }

    /**
     * 查询指定时间段的访问次数
     * @param key
     * @param fromMillis
     * @param toMillis
     * @return  the threshold of this time range
     */
    private long countByRangeMillis(String key, long fromMillis, long toMillis) {
        Preconditions.checkArgument(fromMillis < toMillis, "from time must before to time.");
        return jedisClient.zsetOps().zcount(TRACE_KEY_PREFIX + key, fromMillis, toMillis);
    }

    private static class Trace {
        final String key;
        final double timeMillis;

        public Trace(String key, long timeMillis) {
            this.key = key;
            this.timeMillis = timeMillis;
        }
    }

}
