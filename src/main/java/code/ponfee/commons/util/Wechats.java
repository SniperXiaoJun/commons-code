package code.ponfee.commons.util;

import java.util.LinkedHashMap;
import java.util.Map;

import code.ponfee.commons.http.Http;
import code.ponfee.commons.http.HttpParams;
import code.ponfee.commons.json.Jsons;

/**
 * 微信工具类
 * @author fupf
 */
public class Wechats {

    // -------------------------构建微信授权地址------------------------- //
    public static final String buildAuthorizeUrl(String appid, String redirect, String state) {
        return buildAuthorizeUrl(appid, "UTF-8", redirect, state);
    }

    public static final String buildAuthorizeUrl(String appid, String charset, String redirect, String state) {
        return buildAuthorizeUrl(appid, charset, redirect, state, "snsapi_base");
    }

    /**
     * 构建授权地址
     * @param appid
     * @param state
     * @param scope snsapi_base不弹出授权页面，直接跳转，只能获取用户openid
     *              snsapi_userinfo弹出授权页面，可通过openid拿到昵称、性别、所在地。并且，即使在未关注的情况下，只要用户授权，也能获取其信息
     * @param redirect
     * @return
     */
    public static final String buildAuthorizeUrl(String appid, String charset, String redirect, String state, String scope) {
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("appid", appid);
        params.put("redirect_uri", redirect);
        params.put("response_type", "code");
        params.put("scope", scope);
        params.put("state", state);
        return HttpParams.buildUrlPath(url, charset, params) + "#wechat_redirect";
    }

    // -------------------------通过code换取网页授权access_token------------------------- //
    /**
     * <pre>
     *  {
     *    "access_token":"OezXcEiiBSKSxW0eow",
     *    "expires_in":7200,
     *    "refresh_token":"OezXcqDQy52232WDXB3Msuzq1A",
     *    "openid":"oLVPpjqs9BhvzwPj5A-vTYAX3GLc",
     *    "scope":"snsapi_userinfo,"
     *  }
     * </pre>
     * 
     * 获取微信openID
     * @param appid
     * @param secret
     * @param code
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getOAuth(String appid, String secret, String code) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("appid", appid);
        params.put("secret", secret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        Map<String, String> result = Http.post("https://api.weixin.qq.com/sns/oauth2/access_token").params(params).request(Map.class);
        if (result.containsKey("errcode")) {
            throw new RuntimeException(Jsons.NORMAL.stringify(result));
        }
        return result;
    }

    // -------------------------拉取用户信息(需scope为 snsapi_userinfo)------------------------- //
    /**
     * <pre>
     *  https://api.weixin.qq.com/sns/userinfo
     *  {
     *    "openid":"oLVPpjqs9BhvzwPj5A-vTYAX3GLc",
     *    "nickname":"方倍",
     *    "sex":1,
     *    "language":"zh_CN",
     *    "city":"Shenzhen",
     *    "province":"Guangdong",
     *    "country":"CN",
     *    "headimgurl":"http://wx.qlogo.cn/mmopen/utpBBg18/0",
     *    "privilege":[]
     *  }
     *  
     *  https://api.weixin.qq.com/cgi-bin/user/info  需要使用全局的access_token（getUserInfo，目前版本已不能使用了）
     *  {
     *    "subscribe": 1,
     *    "openid": "osdhfjkdsfh78sdjkljljkkj",
     *    "nickname": "小帅帅丶",
     *    "sex": 1,
     *    "language": "zh_CN",
     *    "city": "北京",
     *    "province": "北京",
     *    "country": "中国",
     *    "headimgurl": "http://wx.qlogo.cn/mmopen/Kkv3HV30gbEZmoo1rTrP4UjRRqzsibUjT9JClPJy3gzo0NkEqzQ9yTSJzErnsRqoLIct5NdLJgcDMicTEBiaibzLn34JLwficVvl6/0",
     *    "subscribe_time": 1389684286
     *  }
     * </pre>
     * 
     * 获取用户信息
     * @param token
     * @param openid
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getUserInfo(String token, String openid) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("access_token", token);
        params.put("openid", openid);
        params.put("lang", "zh_CN");
        Map<String, String> result = Http.post("https://api.weixin.qq.com/sns/userinfo").params(params).request(Map.class);
        if (result.containsKey("errcode")) {
            throw new RuntimeException(Jsons.NORMAL.stringify(result));
        }
        return result;
    }

    // -------------------------获取access_token------------------------- //
    /**
     * get access token
     * @param appid
     * @param secret
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String getAccessToken(String appid, String secret) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "client_credential");
        params.put("appid", appid);
        params.put("secret", secret);
        Map<String, Object> result = Http.post("https://api.weixin.qq.com/cgi-bin/token").params(params).request(Map.class);
        return getString(result, "access_token");
    }

    // -------------------------获取api_ticket------------------------- //
    /**
     * 获取jsapi ticket
     * @param token
     * @return
     */
    public static String getJsapiTicket(String token) {
        return getTicket("jsapi", token);
    }

    /**
     * 获取ticket
     * @param type wx_card 卡券；jsapi js接口票据
     * @param token
     * @return
     */
    @SuppressWarnings("unchecked")
    private static String getTicket(String type, String token) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("access_token", token);
        params.put("type", type);
        Map<String, Object> result = Http.post("https://api.weixin.qq.com/cgi-bin/ticket/getticket").params(params).request(Map.class);
        return getString(result, "ticket");
    }

    private static String getString(Map<String, Object> result, String name) {
        Object errcode = result.get("errcode");
        if (errcode == null || "0".equals(errcode.toString())) {
            return (String) result.get(name); // 成功状态
        } else {
            throw new RuntimeException(Jsons.NORMAL.stringify(result));
        }
    }

    public static void main(String[] args) {
        //System.out.println(getOAuth("fsda", "fdasf", "fdasf"));
        System.out.println(getUserInfo("fsda", "fdasf"));
        //System.out.println(getJsapiTicket("fsda"));
    }
}
