package code.ponfee.commons.log;

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
 * 访问频率控制器
 * @author fupf
 */
public class FrequencyLimiter {

    private static final int EXPIRE_SECONDS = (int) TimeUnit.DAYS.toSeconds(30); // key的失效日期
    private static final String TRACE_KEY_PREFIX = "freq:trace:"; // 频率缓存key前缀
    private static final String QTY_KEY_PREFIX = "freq:qty:"; // 次数缓存key前缀

    private final JedisClient jedisClient;
    private final JedisLock lock;
    private final ScheduledExecutorService executor;
    private final AsyncBatchConsumer<Trace> consumer;
    private final int clearBeforeMillis;
    private final IdWorker idWorker = IdWorker.localIpWorker(); 
    private final Cache<Long> localCache = CacheBuilder.newBuilder().keepaliveInMillis(60000L)
                                                       .autoReleaseInSeconds(60).build();

    public FrequencyLimiter(JedisClient jedisClient, int clearBeforeHours, int autoClearInSeconds) {
        this.jedisClient = jedisClient;
        this.lock = new JedisLock(jedisClient, TRACE_KEY_PREFIX + "clear", autoClearInSeconds);
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.clearBeforeMillis = (int) TimeUnit.HOURS.toMillis(clearBeforeHours);

        // 定时清除记录（zrem range by score）
        this.executor.scheduleAtFixedRate(() -> {
            if (!lock.tryLock()) return;

            try {
                long now = System.currentTimeMillis();
                for (String key : jedisClient.keysOps().keys(TRACE_KEY_PREFIX + "*")) {
                    jedisClient.zsetOps().zremrangeByScore(key, 0, now - clearBeforeMillis);
                }
            } finally {
                lock.unlock();
            }
        }, autoClearInSeconds, autoClearInSeconds, TimeUnit.SECONDS);

        // 批量记录
        this.consumer = new AsyncBatchConsumer<>((list, isEnd) -> {
            return () -> {
                Map<String, Map<String, Double>> map = new HashMap<>();
                for (Trace trace : list) {
                    Map<String, Double> batch = map.get(trace.key);
                    if (batch == null) {
                        batch = new HashMap<>();
                        map.put(trace.key, batch);
                    }
                    // ObjectUtils.uuid(16)
                    batch.put(Long.toString(idWorker.nextId()), (double) trace.timeMillis);
                }
                for (Entry<String, Map<String, Double>> entry : map.entrySet()) {
                    jedisClient.zsetOps().zadd(TRACE_KEY_PREFIX + entry.getKey(), entry.getValue());
                }
                map.clear();
                list.clear();
            };
        }, 200, 1000); // 200毫秒间隔，1000量数/批次
    }

    /**
     * 校验并记录
     * @param key
     * @return 是否频繁访问：true是；false否；
     */
    public boolean checkAndTrace(String key) {
        long limitQtyInMinutes = getLimitQtyInMinutes(key);
        if (limitQtyInMinutes < 0) {
            return true; // 小于0表示无限制
        } else if (limitQtyInMinutes == 0) {
            return false; // 禁止访问
        }

        long currentQtyLastMinutes = countByLastMinutes(key, 1);
        if (currentQtyLastMinutes > limitQtyInMinutes) {
            return false; // 超过频率
        } else {
            return consumer.add(new Trace(key, System.currentTimeMillis()));
        }
    }

    public long countByLastMinutes(String key, int minutes) {
        return countByLastSeconds(key, minutes * 60);
    }

    public long countByLastSeconds(String key, int seconds) {
        long now = System.currentTimeMillis();
        long fromMillis = now - TimeUnit.SECONDS.toMillis(seconds);
        return countByRangeMillis(key, fromMillis, now);
    }

    public long countByBeforeRangeHours(String key, int beforeFromHours, int beforeToHours) {
        return countByBeforeRangeSeconds(key, beforeFromHours * 60 * 60, beforeToHours * 60 * 60);
    }

    public long countByBeforeRangeMinutes(String key, int beforeFromMinutes, int beforeToMinutes) {
        return countByBeforeRangeSeconds(key, beforeFromMinutes * 60, beforeToMinutes * 60);
    }

    public long countByBeforeRangeSeconds(String key, int beforeFromSeconds, int beforeToSeconds) {
        Preconditions.checkState(beforeFromSeconds > beforeToSeconds, 
                                 "before from must be greater than before to.");

        long now = System.currentTimeMillis();
        long fromMillis = now - TimeUnit.SECONDS.toMillis(beforeFromSeconds);
        long toMillis = now - TimeUnit.SECONDS.toMillis(beforeToSeconds);
        return countByRangeMillis(key, fromMillis, toMillis);
    }

    /**
     * 设置一分钟的访问频率
     * @param key
     * @param qty
     * @return
     */
    public boolean setLimitQtyInMinutes(String key, long qty) {
        boolean flag = jedisClient.valueOps().setLong(QTY_KEY_PREFIX + key, 
                                                      qty, EXPIRE_SECONDS);
        if (flag) {
            localCache.getAndRemove(key); // remove this key
        }

        return flag;
    }

    /**
     * 获取一分钟的访问频率
     * @param key
     * @return
     */
    public long getLimitQtyInMinutes(String key) {
        Long qty = localCache.get(key);
        if (qty == null) {
            qty = jedisClient.valueOps().getLong(QTY_KEY_PREFIX + key, EXPIRE_SECONDS);
            if (qty == null) {
                qty = -1L;
            }
            localCache.set(key, qty);
        }
        return qty;
    }

    private long countByRangeMillis(String key, long fromMillis, long toMillis) {
        return jedisClient.zsetOps().zcount(TRACE_KEY_PREFIX + key, fromMillis, toMillis);
    }

    private static class Trace {
        final String key;
        final long timeMillis;

        public Trace(String key, long timeMillis) {
            this.key = key;
            this.timeMillis = timeMillis;
        }
    }

    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        System.out.println(System.nanoTime());
        System.out.println(TRACE_KEY_PREFIX.substring(0, TRACE_KEY_PREFIX.length() - 1));
    }
}
