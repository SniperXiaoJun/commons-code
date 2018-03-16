package code.ponfee.commons.limit;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.jce.digest.HmacUtils;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.util.Bytes;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 请求限制器
 * @author Ponfee
 */
public class RequestLimiter {

    private static final byte[] SLAT_PREFIX = "{;a*9)p<?T".getBytes();

    /** limit operation key */
    private static final String CHECK_FREQ_KEY = "req:lmt:fre:";
    private static final String CHECK_THRE_KEY = "req:lmt:thr:";

    /** validation code verify key */
    private static final String CACHE_CODE_KEY = "req:cah:code:";
    private static final String CHECK_CODE_KEY = "req:chk:code:";

    /** image captcha code verify key */
    private static final String CACHE_CAPTCHA_KEY = "req:cah:cap:";

    /** count operation action key */
    private static final String INCR_ACTION_KEY = "req:inc:act:";
    private static final String COUNT_ACTION_KEY = "req:cnt:act:";

    private final JedisClient client;

    private RequestLimiter(JedisClient client) {
        this.client = client;
    }

    public static RequestLimiter create(JedisClient client) {
        return new RequestLimiter(client);
    }

    // ---------------------------------用于请求限制-------------------------------
    public RequestLimiter limitFrequency(String key, int period)
        throws RequestLimitException {
        return limitFrequency(key, period, "请求频繁，请" + format(period) + "后再试！");
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
        return limitThreshold(CHECK_THRE_KEY + key, period, limit, 
                              "请求超限，请" + format(period) + "后再试！");
    }

    /**
     * 访问次数限制：一个周期内最多允许访问limit次
     * 比如一个手机号一天只能发10次
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

    // ------------------------------用于验证码校验（如手机验证码）----------------------------------
    /**
     * cache for the server generate validation code
     * @param key   the cache key
     * @param code  the validation code of server generate
     * @param ttl   the expire time
     * @return
     */
    public void cacheCode(String key, String code, int ttl) {
        client.valueOps().set(CACHE_CODE_KEY + key, code, ttl);
        client.keysOps().del(CHECK_CODE_KEY + key);
    }

    /**
     * check the validation code of user input is equals server cache
     * @param key   the cache key
     * @param code  the validation code of user input
     * @param limit the maximum fail input times
     * @return chain program
     * @throws RequestLimitException
     */
    public RequestLimiter checkCode(String key, String code, int limit)
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
            client.keysOps().dels(cacheKey, checkKey); // 超过验证次数，删除缓存中的验证码
            throw new RequestLimitException("验证错误次数过多，请重新获取！");
        }

        // 3、检查验证码是否匹配
        if (!actual.equals(code)) {
            throw new RequestLimitException("验证码错误！");
        }

        // 验证成功，删除缓存key
        client.keysOps().dels(cacheKey, checkKey);
        return this;
    }

    // ------------------------------用于缓存图片验证码----------------------------------
    /**
     * cache captcha of server generate
     * @param key
     * @param captcha the image cptcha code of server generate
     * @param expire  缓存有效时间
     * @return
     */
    public void cacheCaptcha(String key, String captcha, int expire) {
        client.valueOps().set(CACHE_CAPTCHA_KEY + key, captcha, expire);
    }

    public boolean checkCaptcha(String key, String captcha) {
        return checkCaptcha(key, captcha, false); // ignore case sensitive
    }

    /**
     * check captcha of user input
     * @param key  the cache key
     * @param captcha  the captcha
     * @param caseSensitive  is case sensitive
     * @return true|flase
     */
    public boolean checkCaptcha(String key, String captcha, boolean caseSensitive) {
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

    // ----------------------------------行为计数（用于登录失败限制）--------------------------------------
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
        return ObjectUtils.ifNull(client.valueOps().getLong(COUNT_ACTION_KEY + key), 0L);
    }

    /**
     * 重置行为
     * @param key
     */
    public void resetAction(String key) {
        client.keysOps().del(COUNT_ACTION_KEY + key);
    }

    // -------------------------------用于验证码校验---------------------------------
    /**
     * 生成nonce校验码（返回到用户端）
     * @param code
     * @param salt
     * @return
     */
    public static String buildNonce(String code, String salt) {
        long first = new Random(code.hashCode()).nextLong(); // 第一个nextLong值是固定的
        byte[] slatBytes = ArrayUtils.addAll(SLAT_PREFIX, salt.getBytes());
        return HmacUtils.md5Hex(Bytes.fromLong(first), slatBytes);
    }

    /**
     * 校验nonce
     * @param nonce
     * @param code
     * @param salt
     * @return
     */
    public static boolean verifyNonce(String nonce, String code, String salt) {
        return StringUtils.isNotEmpty(nonce) && nonce.equals(buildNonce(code, salt));
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

    /**
     * 时间格式化，5/6 rate
     * @param seconds
     * @return
     */
    public static String format(int seconds) {
        int days = seconds / 86400;
        if (days > 365) { // 年
            return (days / 365 + ((days % 365) / 30 + 10) / 12) + "年";
        }
        if (days > 30) { // 月
            return (days / 30 + (days % 30 + 25) / 30) + "个月";
        }

        seconds %= 86400;
        int hours = seconds / 3600;
        if (days > 0) { // 日
            return (days + (hours + 20) / 24) + "天";
        }

        seconds %= 3600;
        int minutes = seconds / 60;
        if (hours > 0) { // 时
            return (hours + (minutes + 50) / 60) + "小时";
        }

        seconds %= 60;
        if (minutes > 0) { // 分
            return (minutes + (seconds + 50) / 60) + "分钟";
        }

        return seconds + "秒"; // 秒
    }

}
