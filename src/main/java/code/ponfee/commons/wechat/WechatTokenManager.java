package code.ponfee.commons.wechat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;

/**
 * wechat token 管理类
 * @author Ponfee
 */
public class WechatTokenManager {

    private static final Map<String, Wechat> WECHAT_CONFIGS = new HashMap<String, Wechat>() {
        private static final long serialVersionUID = 4891406751053897149L;
        {
            put(new Wechat("wx37faaaf77fbbdc36", "secret"));
            put(new Wechat("appid", "secret"));
        }

        private void put(Wechat wechat) {
            super.put(wechat.appid, wechat);
        }
    };

    private static final Map<String, JedisLock> JEDIS_LOCKS = new ConcurrentHashMap<>(); // 锁
    private static final int REFRESH_PERIOD_TIME = (int) Math.ceil(86400.0D / 2000); // 一天只能获取2000次上限
    private static final int TOKEN_EXPIRE_TIME = 7200 - 100; // token 7100秒的缓存有效期（token有效期为7200秒）

    private static Logger logger = LoggerFactory.getLogger(WechatTokenManager.class);

    private final JedisClient jedisClient;

    public WechatTokenManager(JedisClient jedisClient) {
        this.jedisClient = jedisClient;

        // 定时刷新
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (Wechat wechat : WECHAT_CONFIGS.values()) {
                try {
                    refreshAndGetToken(wechat);
                } catch (FrequentlyRefreshException e) {
                    logger.error(e.getMessage());
                }
            }
        }, 0, TOKEN_EXPIRE_TIME, TimeUnit.SECONDS);
    }

    /**
     * 获取accessToken
     * @param appid
     * @return token
     */
    public final String getAccessToken(String appid) {
        return getWechatToken(appid, Type.ACCESS_TOKEN);
    }

    /**
     * 获取jsapiTicket
     * @param appid
     * @return ticket
     */
    public final String getJsapiTicket(String appid) {
        return getWechatToken(appid, Type.JSAPI_TICKET);
    }

    /**
     * 手动刷新accessToken
     * @param appid
     * @return
     */
    public String refreshAccessToken(String appid) throws FrequentlyRefreshException {
        Wechat wechat = WECHAT_CONFIGS.get(appid);
        if (wechat == null) {
            throw new IllegalArgumentException("invalid wechat appid: " + appid);
        }

        return refreshAndGetToken(wechat).get(WECHAT_CONFIGS.get(appid).accessTokenKey);
    }

    /**
     * 手动刷新jsapiTicket
     * @param appid
     * @return
     */
    public String refreshJsapiTicket(String appid) throws FrequentlyRefreshException {
        Wechat wechat = WECHAT_CONFIGS.get(appid);
        if (wechat == null) {
            throw new IllegalArgumentException("invalid wechat appid: " + appid);
        }

        return refreshAndGetToken(wechat).get(WECHAT_CONFIGS.get(appid).jsapiTicketKey);
    }

    /**
     * 缓存openId：主要是解决获取openid时若网络慢会同时出现多次请求，
     * 导致错误：{"errcode":40029,"errmsg":"invalid code, hints: [ req_id: raY0187ns82 ]"}
     * 当调用{@link Wechats#getOAuth2(String, String, String)}时，如果返回此错误则从缓存获取
     * 如果获取成功则缓存到此缓存
     * @param code
     * @param openid
     */
    private static final String WECHAT_OPENID_CACHE = "wechat:openid:cache:";
    public void cacheOpenidByCode(String code, String openid) {
        jedisClient.valueOps().set(WECHAT_OPENID_CACHE + code, openid, 30);
    }

    /**
     * 加载openId
     * @param code
     * @return
     */
    public String loadOpenidByCode(String code) {
        return jedisClient.valueOps().get(WECHAT_OPENID_CACHE + code);
    }

    // -----------------------------------private methods--------------------------------- //
    /**
     * 获取token
     * @param appid
     * @param type
     * @return
     */
    private final String getWechatToken(String appid, Type type) {
        Wechat wechat = WECHAT_CONFIGS.get(appid);
        if (wechat == null) {
            throw new IllegalArgumentException("invalid wechat appid: " + appid);
        }

        String cacheKey = wechat.getCacheKey(type);
        String token = null;
        int iteration = 10;
        do {
            token = jedisClient.valueOps().get(cacheKey);
            if (token == null) {
                try {
                    token = refreshAndGetToken(wechat).get(cacheKey);
                } catch (FrequentlyRefreshException ignored) {
                    // do-non
                }
                if (token == null) {
                    try {
                        Thread.sleep(99);
                    } catch (InterruptedException e) {
                        logger.error("get wechat token thread sleep interrupted", e);
                    }
                }
            }
        } while (token == null && --iteration > 0);

        return token;
    }

    /**
     * 主动刷新token（已限制频率）
     * @param appid
     * @return
     * @throws FrequentlyRefreshException
     */
    private Map<String, String> refreshAndGetToken(Wechat wechat) 
        throws FrequentlyRefreshException {
        // 限制刷新频率，自动超时释放锁
        if (getLock(wechat.lockRefreshKey, REFRESH_PERIOD_TIME).tryLock()) {
            // 请求微信接口获取
            String accessToken = Wechats.getAccessToken(wechat.appid, wechat.secret);
            String jsapiTicket = Wechats.getJsapiTicket(accessToken);
            logger.info("－－－ refresh wechat token appid->{} －－－", wechat.appid);

            // 异步缓存
            new Thread(() -> {
                jedisClient.valueOps().set(wechat.accessTokenKey, accessToken, TOKEN_EXPIRE_TIME);
                jedisClient.valueOps().set(wechat.jsapiTicketKey, jsapiTicket, TOKEN_EXPIRE_TIME);
            }).start();

            return ImmutableMap.of(wechat.accessTokenKey, accessToken, 
                                   wechat.jsapiTicketKey, jsapiTicket);
        } else {
            throw new FrequentlyRefreshException("微信令牌频繁刷新，请稍后再试！");
        }
    }

    /**
     * 获取分布式锁
     * @param lockKey
     * @param timeout
     * @return
     */
    private JedisLock getLock(String lockKey, int timeout) {
        JedisLock lock = JEDIS_LOCKS.get(lockKey);
        if (lock == null) {
            synchronized (WechatTokenManager.class) {
                lock = JEDIS_LOCKS.get(lockKey);
                if (lock == null) {
                    lock = new JedisLock(jedisClient, lockKey, timeout);
                    JEDIS_LOCKS.put(lockKey, lock);
                }
            }
        }
        return lock;
    }

    /**
     * 配置
     */
    private static final class Wechat {
        private final String appid;
        private final String secret;
        private final String accessTokenKey;
        private final String jsapiTicketKey;
        private final String lockRefreshKey;

        Wechat(String appid, String secret) {
            this.appid = appid;
            this.secret = secret;
            this.accessTokenKey = "wechat:access:token:" + appid;
            this.jsapiTicketKey = "wechat:jsapi:ticket:" + appid;
            this.lockRefreshKey = "wechat:token:refresh:" + appid;
        }

        String getCacheKey(Type type) {
            switch (type) {
                case ACCESS_TOKEN:
                    return accessTokenKey;
                case JSAPI_TICKET:
                    return jsapiTicketKey;
                default:
                    throw new IllegalArgumentException("invalid type " + type);
            }
        }
    }

    /**
     * token类型
     */
    private static enum Type {
        ACCESS_TOKEN, JSAPI_TICKET;
    }

}
