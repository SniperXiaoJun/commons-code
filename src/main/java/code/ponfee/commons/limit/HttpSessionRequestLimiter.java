package code.ponfee.commons.limit;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

/**
 * 请求限制器
 * @author Ponfee
 */
@SuppressWarnings("unchecked")
public class HttpSessionRequestLimiter extends RequestLimiter {

    private final HttpSession session;

    private HttpSessionRequestLimiter(HttpSession session) {
        this.session = session;
    }

    public static HttpSessionRequestLimiter create(HttpSession session) {
        return new HttpSessionRequestLimiter(session);
    }

    @Override public HttpSessionRequestLimiter limitFrequency(String key, int period, String message)
        throws RequestLimitException {
        //checkLimit(CHECK_FREQ_KEY + key, period, 1, message);
        //return this;
        throw new UnsupportedOperationException();
    }

    @Override public HttpSessionRequestLimiter limitThreshold(String key, int period, 
                                                              int limit, String message) 
        throws RequestLimitException {
        //checkLimit(CHECK_THRE_KEY + key, period, limit, message);
        //return this;
        throw new UnsupportedOperationException();
    }

    @Override public void cacheCode(String key, String code, int ttl) {
        add(CACHE_CODE_KEY + key, code, ttl);
        remove(CHECK_CODE_KEY + key);
    }

    @Override public HttpSessionRequestLimiter checkCode(String key, String code, int limit)
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

    @Override public void cacheCaptcha(String key, String captcha, int expire) {
        add(CACHE_CAPTCHA_KEY + key, captcha, expire);
    }

    @Override public boolean checkCaptcha(String key, String captcha, boolean caseSensitive) {
        CacheValue<String> value = getAndRemove(CACHE_CAPTCHA_KEY + key);

        if (value == null || value.get() == null) {
            return false;
        }

        if (caseSensitive) {
            return value.get().equals(captcha);
        } else {
            return value.get().equalsIgnoreCase(captcha);
        }
    }

    @Deprecated
    @Override public void traceAction(String key, int period) {
        incrementAndGet(INCR_ACTION_KEY + key, expire(period));
    }

    @Deprecated
    @Override public long countAction(String key) {
        CacheValue<Void> cache = get(COUNT_ACTION_KEY + key);
        return cache == null ? 0 : cache.count();
    }

    @Deprecated
    @Override public void resetAction(String key) {
        remove(COUNT_ACTION_KEY + key);
    }

    // ---------------------------------------------------------------------private methods
    /*private void checkLimit(String key, int ttl, int limit, String message)
        throws RequestLimitException {
        CacheValue<?> cache = incrementAndGet(key, expire(ttl));
        if (cache.count() > limit) {
            throw new RequestLimitException(message);
        }
    }*/

    private CacheValue<?> incrementAndGet(String key, long expireTimeMillis) {
        synchronized (session) {
            CacheValue<?> cache = (CacheValue<?>) session.getAttribute(key);
            if (cache == null || cache.isExpire()) {
                cache = new CacheValue<>(null, expireTimeMillis);
                session.setAttribute(key, cache);
            } else {
                cache.increment();
            }
            return cache;
        }
    }

    private void remove(String... keys) {
        for (String key : keys) {
            session.removeAttribute(key);
        }
    }

    private <T> CacheValue<T> getAndRemove(String key) {
        CacheValue<T> cache = (CacheValue<T>) session.getAttribute(key);
        if (cache == null) {
            return null;
        } else {
            session.removeAttribute(key);
            return cache.isExpire() ? null : cache;
        }
    }

    private <T> void add(String key, T value, int ttl) {
        session.setAttribute(key, new CacheValue<>(value, expire(ttl)));
    }

    private <T> CacheValue<T> get(String key) {
        CacheValue<T> cache = (CacheValue<T>) session.getAttribute(key);
        if (cache == null) {
            return null;
        } else if (cache.isExpire()) {
            session.removeAttribute(key);
            return null;
        } else {
            return cache;
        }
    }

    private static long expire(int ttl) {
        return System.currentTimeMillis() + ttl * 1000;
    }

    private static class CacheValue<T> {
        private final T value;
        private final long expireTimeMillis;
        private final AtomicInteger count;

        public CacheValue(T value, long expireTimeMillis) {
            this.value = value;
            this.expireTimeMillis = expireTimeMillis;
            this.count = new AtomicInteger(1);
        }

        private int increment() {
            return count.getAndIncrement();
        }

        private int count() {
            return count.get();
        }

        private T get() {
            return value;
        }

        private boolean isExpire() {
            return expireTimeMillis < System.currentTimeMillis();
        }
    }
}
