package code.ponfee.commons.limit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;

/**
 * The request limiter based ConcurrentHashMap
 * 
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public final class ConcurrentMapRequestLimiter extends RequestLimiter {

    private static final ConcurrentHashMap<String, CacheValue<?>> CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMapRequestLimiter INSTANCE = new ConcurrentMapRequestLimiter();
    private static final ScheduledExecutorService EXECUTOR = new ScheduledThreadPoolExecutor(1, new DiscardPolicy());
    private static final Lock LOCK = new ReentrantLock(); // 定时清理加锁
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(EXECUTOR::shutdown));
        EXECUTOR.scheduleAtFixedRate(() -> {
            if (!LOCK.tryLock()) {
                return;
            }
            try {
                long now = System.currentTimeMillis();
                CACHE.entrySet().removeIf(x -> x.getValue().isExpire(now));
            } finally {
                LOCK.unlock();
            }
        }, 60, 120, TimeUnit.SECONDS);
    }

    private ConcurrentMapRequestLimiter() {}

    public static ConcurrentMapRequestLimiter singleton() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------request limit
    @Override public ConcurrentMapRequestLimiter limitFrequency(String key, int period, String message)
        throws RequestLimitException {
        checkLimit(CHECK_FREQ_KEY + key, period, 1, message);
        return this;
    }

    @Override public ConcurrentMapRequestLimiter limitThreshold(String key, int period, 
                                                              int limit, String message) 
        throws RequestLimitException {
        checkLimit(CHECK_THRE_KEY + key, period, limit, message);
        return this;
    }

    // ---------------------------------------------------------------------cache sms code
    @Override public void cacheCode(String key, String code, int ttl) {
        add(CACHE_CODE_KEY + key, code, ttl);
        remove(CHECK_CODE_KEY + key);
    }

    @Override public ConcurrentMapRequestLimiter checkCode(String key, String code, int limit)
        throws RequestLimitException {
        if (StringUtils.isEmpty(code)) {
            throw new RequestLimitException("验证码不能为空！");
        }

        String cacheKey = CACHE_CODE_KEY + key;

        // 1、判断验证码是否已失效
        CacheValue<String> actual = get(cacheKey);
        if (actual == null || actual.get() == null) {
            throw new RequestLimitException("验证码失效，请重新获取！");
        }

        String checkKey = CHECK_CODE_KEY + key;

        // 2、检查是否验证超过限定次数
        CacheValue<?> times = incrementAndGet(checkKey, actual.expireTimeMillis);
        if (times.count() > limit) {
            remove(cacheKey, checkKey); // 超过验证次数，删除缓存中的验证码
            throw new RequestLimitException("验证错误次数过多，请重新获取！");
        }

        // 3、检查验证码是否匹配
        if (!actual.get().equals(code)) {
            throw new RequestLimitException("验证码错误！");
        }

        // 验证成功，删除缓存key
        remove(cacheKey, checkKey);
        return this;
    }

    // ---------------------------------------------------------------------cache captcha
    @Override public void cacheCaptcha(String key, String captcha, int expire) {
        add(CACHE_CAPTCHA_KEY + key, captcha, expire);
    }

    @Override public boolean checkCaptcha(String key, String captcha, boolean caseSensitive) {
        CacheValue<String> value = getAndRemove(CACHE_CAPTCHA_KEY + key);

        if (value == null || value.get() == null) {
            return false;
        }

        return caseSensitive 
               ? value.get().equals(captcha)
               : value.get().equalsIgnoreCase(captcha);
    }

    // ---------------------------------------------------------------------action
    @Override public void traceAction(String key, int period) {
        incrementAndGet(INCR_ACTION_KEY + key, expire(period));
    }

    @Override public long countAction(String key) {
        CacheValue<Void> cache = get(COUNT_ACTION_KEY + key);
        return cache == null ? 0 : cache.count();
    }

    @Override public void resetAction(String key) {
        remove(COUNT_ACTION_KEY + key);
    }

    // ---------------------------------------------------------------------private methods
    private void checkLimit(String key, int ttl, int limit, String message)
        throws RequestLimitException {
        CacheValue<?> cache = incrementAndGet(key, expire(ttl));
        if (cache.count() > limit) {
            throw new RequestLimitException(message);
        }
    }

    private CacheValue<?> incrementAndGet(String key, long expireTimeMillis) {
        CacheValue<?> cache = (CacheValue<?>) CACHE.get(key);
        if (cache == null || cache.isExpire()) {
            synchronized (ConcurrentMapRequestLimiter.class) {
                cache = (CacheValue<?>) CACHE.get(key);
                if (cache == null || cache.isExpire()) { // 失效则重置
                    cache = new CacheValue<>(null, expireTimeMillis);
                    CACHE.put(key, cache);
                    return cache;
                }
            }
        }
        cache.increment();
        return cache;
    }

    private void remove(String... keys) {
        for (String key : keys) {
            CACHE.remove(key);
        }
    }

    private <T> CacheValue<T> getAndRemove(String key) {
        CacheValue<T> cache = (CacheValue<T>) CACHE.remove(key);
        return cache == null || cache.isExpire() ? null : cache;
    }

    private <T> void add(String key, T value, int ttl) {
        CACHE.put(key, new CacheValue<>(value, expire(ttl)));
    }

    private <T> CacheValue<T> get(String key) {
        CacheValue<T> cache = (CacheValue<T>) CACHE.get(key);
        if (cache == null) {
            return null;
        } else if (cache.isExpire()) {
            CACHE.remove(key);
            return null;
        } else {
            return cache;
        }
    }

}
