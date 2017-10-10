package code.ponfee.commons.log;

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
import code.ponfee.commons.concurrent.AsyncBatchConsumer;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;
import code.ponfee.commons.util.IdWorker;

/**
 * Redis访问频率控制器
 * @author fupf
 */
public class RedisFrequencyLimiter implements FrequencyLimiter {

    private static final int EXPIRE_SECONDS = (int) TimeUnit.DAYS.toSeconds(30) + 1; // key的失效日期
    private static final String TRACE_KEY_PREFIX = "freq:trace:"; // 频率缓存key前缀
    private static final String QTY_KEY_PREFIX = "freq:qty:"; // 次数缓存key前缀

    private final JedisClient jedisClient;
    private final JedisLock lock;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AsyncBatchConsumer<Trace> consumer;
    private final int clearBeforeMillis;
    private final Cache<Long> localCache = CacheBuilder.newBuilder().keepaliveInMillis(300000L) // 5 minutes of cache alive
                                                       .autoReleaseInSeconds(300).build(); // 5 minutes to release expire cache

    public RedisFrequencyLimiter(JedisClient jedisClient, int clearBeforeHours, int autoClearInSeconds) {
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
        this.consumer = new AsyncBatchConsumer<>((list, isEnd) -> {
            return () -> {
                Map<String, Map<String, Double>> map = new HashMap<>();
                Map<String, Double> batch;
                for (Trace trace : list) {
                    batch = map.get(trace.key);
                    if (batch == null) {
                        batch = new HashMap<>();
                        map.put(trace.key, batch);
                    }
                    // ObjectUtils.uuid22()
                    batch.put(Long.toString(IdWorker.LOCAL_WORKER.nextId(), Character.MAX_RADIX), 
                              trace.timeMillis);
                }
                for (Entry<String, Map<String, Double>> entry : map.entrySet()) {
                    // TRACE_KEY_PREFIX + trace.key
                    jedisClient.zsetOps().zadd(TRACE_KEY_PREFIX + entry.getKey(), 
                                               entry.getValue(), EXPIRE_SECONDS);
                }
                map.clear();
                map = null;
                list.clear();
            };
        }, 100, 2000); // 100毫秒间隔，2000条∕次
    }

    /**
     * 校验并记录
     * @param key
     * @return 是否频繁访问：true是；false否；
     */
    public @Override boolean checkAndTrace(String key) {
        long limitQtyInMinutes = getLimitsInMinute(key);
        if (limitQtyInMinutes < 0) {
            return true; // 小于0表示无限制
        } else if (limitQtyInMinutes == 0) {
            return false; // 禁止访问
        }

        if (limitQtyInMinutes < countByLastTime(key, 1, TimeUnit.MINUTES)) {
            return false; // 超过频率
        } else {
            return consumer.add(new Trace(key, System.currentTimeMillis()));
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
     * @param qty
     * @return 是否设置成功：true是；false否；
     */
    public @Override boolean setLimitsInMinute(String key, long qty) {
        boolean flag = jedisClient.valueOps().setLong(QTY_KEY_PREFIX + key, qty, EXPIRE_SECONDS);
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
    public @Override long getLimitsInMinute(String key) {
        Long qty = localCache.get(key);
        if (qty == null) {
            qty = jedisClient.valueOps().getLong(QTY_KEY_PREFIX + key, EXPIRE_SECONDS);
            if (qty == null) {
                qty = -1L; // -1表示无限制
            }
            localCache.set(key, qty); // put into local cache
        }
        return qty;
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
     * @return  the qty of this time range
     */
    private long countByRangeMillis(String key, long fromMillis, long toMillis) {
        Preconditions.checkState(fromMillis < toMillis, "from time must be less than to time.");
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
