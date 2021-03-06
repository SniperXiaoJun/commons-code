package code.ponfee.commons.limit;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.util.ObjectUtils;

/**
 * The request limiter based redis
 * 
 * @author Ponfee
 */
public class RedisRequestLimiter extends RequestLimiter{

    private final JedisClient client;

    private RedisRequestLimiter(JedisClient client) {
        this.client = client;
    }

    public static RedisRequestLimiter create(JedisClient client) {
        return new RedisRequestLimiter(client);
    }


    @Override public RedisRequestLimiter limitFrequency(String key, int period, String message)
        throws RequestLimitException {
        checkLimit(CHECK_FREQ_KEY + key, period, 1, message);
        return this;
    }

    @Override public RedisRequestLimiter limitThreshold(String key, int period, 
                                                        int limit, String message) 
        throws RequestLimitException {
        checkLimit(CHECK_THRE_KEY + key, period, limit, message);
        return this;
    }

    @Override public void cacheCode(String key, String code, int ttl) {
        client.valueOps().set(CACHE_CODE_KEY + key, code, ttl);
        client.keysOps().del(CHECK_CODE_KEY + key);
    }

    @Override public RedisRequestLimiter checkCode(String key, String code, int limit)
        throws RequestLimitException {
        if (StringUtils.isEmpty(code)) {
            throw new RequestLimitException("验证码不能为空！");
        }

        String cacheKey = CACHE_CODE_KEY + key;

        // 1、判断验证码是否已失效
        String actual = client.valueOps().get(cacheKey);
        if (actual == null) {
            throw new RequestLimitException("验证码失效，请重新获取！");
        }

        String checkKey = CHECK_CODE_KEY + key;

        // 2、检查是否验证超过限定次数
        long times = client.valueOps().incrBy(checkKey);
        if (times == 1) {
            int ttl = client.keysOps().ttl(cacheKey).intValue() + 1; // calc check key ttl
            client.keysOps().expire(checkKey, ttl); // 第一次验证，设置验证标识数据的缓存失效期
        } else if (times > limit) {
            client.keysOps().mdel(cacheKey, checkKey); // 超过验证次数，删除缓存中的验证码
            throw new RequestLimitException("验证错误次数过多，请重新获取！");
        }

        // 3、检查验证码是否匹配
        if (!actual.equals(code)) {
            throw new RequestLimitException("验证码错误！");
        }

        // 验证成功，删除缓存key
        client.keysOps().mdel(cacheKey, checkKey);
        return this;
    }

    @Override public void cacheCaptcha(String key, String captcha, int expire) {
        client.valueOps().set(CACHE_CAPTCHA_KEY + key, captcha, expire);
    }

    @Override public boolean checkCaptcha(String key, String captcha, boolean caseSensitive) {
        String value = client.valueOps().getAndDel(CACHE_CAPTCHA_KEY + key);

        if (value == null) {
            return false;
        }

        if (caseSensitive) {
            return value.equals(captcha);
        } else {
            return value.equalsIgnoreCase(captcha);
        }
    }

    @Override public void traceAction(String key, int period) {
        key = TRACE_ACTION_KEY + key;
        long times = client.valueOps().incrBy(key);
        if (times == 1) {
            client.keysOps().expire(key, period); // 第一次缓存，设置失效时间
        }
    }

    @Override public long countAction(String key) {
        return ObjectUtils.orElse(client.valueOps().getLong(TRACE_ACTION_KEY + key), 0L);
    }

    @Override public void resetAction(String key) {
        client.keysOps().del(TRACE_ACTION_KEY + key);
    }

    // -----------------------------------------------------------------------private methods
    private void checkLimit(String key, int ttl, int limit, String message)
        throws RequestLimitException {
        long times = client.valueOps().incrBy(key);
        if (times == 1) {
            client.keysOps().expire(key, ttl); // 第一次缓存，则设置失效时间
        }
        if (times > limit) {
            throw new RequestLimitException(message);
        }
    }

}
