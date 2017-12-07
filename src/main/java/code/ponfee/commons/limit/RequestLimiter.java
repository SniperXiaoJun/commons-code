package code.ponfee.commons.limit;

import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.jce.hash.HmacUtils;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.util.Bytes;

/**
 * 请求限制器
 * @author Ponfee
 */
public class RequestLimiter {

    private static final String CHECK_FREQ_KEY = "req:limit:freq:";
    private static final String CHECK_THRE_KEY = "req:limit:thre:";

    private static final String CACHE_CAPTCHA_KEY = "req:cache:captcha:";
    private static final String CHECK_CAPTCHA_KEY = "req:check:captcha:";

    private static final String INCR_ACTION_KEY = "req:incr:action:";
    private static final String COUNT_ACTION_KEY = "req:count:action:";

    private final JedisClient client;

    public RequestLimiter(JedisClient client) {
        this.client = client;
    }

    public RequestLimiter limitFrequency(String key, int period)
        throws RequestLimitException {
        return this.limitFrequency(key, period, "请求频繁，请" + period + "秒后再试！");
    }

    /**
     * 访问频率限制：一个周期内最多允许访问1次<p>
     * 比如短信60秒内只能发送一次
     * @param key
     * @param period
     * @throws RequestLimitException 
     */
    public RequestLimiter limitFrequency(String key, int period, String message)
        throws RequestLimitException {
        checkLimit(CHECK_FREQ_KEY + key, period, 1, message);
        return this;
    }

    public RequestLimiter limitThreshold(String key, int period, int limit)
        throws RequestLimitException {
        return limitThreshold(CHECK_THRE_KEY + key, period, limit, "请求超限，请" + period + "秒后再试！");
    }

    /**
     * 访问次数限制：一个周期内最多允许访问limit次
     * @param key
     * @param period
     * @param limit
     * @throws RequestLimitException 
     */
    public RequestLimiter limitThreshold(String key, int period, int limit, String message)
        throws RequestLimitException {
        checkLimit(CHECK_THRE_KEY + key, period, limit, message);
        return this;
    }

    /**
     * 缓存验证码
     * @param key
     * @param captcha
     * @param ttl     缓存有效时间
     * @return
     */
    public void cacheCaptcha(String key, String captcha, int ttl) {
        client.valueOps().set(CACHE_CAPTCHA_KEY + key, captcha, ttl);
        client.keysOps().del(CHECK_CAPTCHA_KEY + key);
    }

    /**
     * 校验验证码
     * @param key
     * @param captcha
     * @param limit    限制验证失败的最多次数
     * @return
     * @throws RequestLimitException
     */
    public RequestLimiter checkCaptcha(String key, String captcha, int limit)
        throws RequestLimitException {
        String cacheKey = CACHE_CAPTCHA_KEY + key;

        // 1、判断验证码是否已失效
        String actual = client.valueOps().get(cacheKey);
        if (actual == null) {
            throw new RequestLimitException("验证码失效，请重新获取！");
        }

        String checkKey = CHECK_CAPTCHA_KEY + key;

        // 2、检查是否验证超过限定次数
        long times = client.valueOps().incrBy(checkKey);
        if (times == 1) {
            int ttl = client.keysOps().ttl(cacheKey).intValue() + 1;
            client.keysOps().expire(checkKey, ttl); // 第一次验证，设置缓存失效期
        } else if (times > limit) {
            client.keysOps().del(cacheKey); // 超过验证次数，删除缓存中的验证码
            throw new RequestLimitException("验证错误次数过多，请重新获取！");
        }

        // 3、检查验证码是否匹配
        if (!actual.equals(captcha)) {
            throw new RequestLimitException("验证码错误！");
        }

        client.keysOps().del(cacheKey);
        return this;
    }

    /**
     * 计数周期内的行为<p>
     * 用于登录失败达到一定次数后锁定账户等场景<p>
     * @param key
     * @param period
     */
    public void incrAction(String key, int period) {
        key = INCR_ACTION_KEY + key;
        long times = client.valueOps().incrBy(key);
        if (times == 1) {
            client.keysOps().expire(key, period); // 第一次缓存，设置失效时间
        }
    }

    /**
     * 统计周期内的行为量<p>
     * 用于登录失败达到一定次数后锁定账户等场景<p>
     * @param key
     * @return
     */
    public long countAction(String key) {
        return client.valueOps().getLong(COUNT_ACTION_KEY + key);
    }

    /**
     * 生成nonce str校验码（返回到用户端）
     * @param captcha
     * @param salt
     * @return
     */
    public static String buildNonce(String captcha, String salt) {
        // new Random(long seed).nextLong() 第一个nextLong值是固定的
        return HmacUtils.sha1Hex(Bytes.fromLong(new Random(captcha.hashCode()).nextLong()), salt.getBytes());
    }

    /**
     * 校验noce str
     * @param nonce
     * @param captcha
     * @param text
     * @return
     */
    public static boolean verifyNonce(String nonce, String captcha, String salt) {
        return StringUtils.isNotEmpty(nonce) && nonce.equals(buildNonce(captcha, salt));
    }

    // -------------------------------------private methods----------------------------------
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
