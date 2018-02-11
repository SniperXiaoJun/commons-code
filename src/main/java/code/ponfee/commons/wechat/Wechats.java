package code.ponfee.commons.wechat;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import code.ponfee.commons.http.Http;
import code.ponfee.commons.http.HttpParams;
import code.ponfee.commons.io.Files;
import code.ponfee.commons.jce.hash.HashUtils;
import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 微信工具类
 * https://www.cnblogs.com/txw1958/p/weixin76-user-info.html
 * @author fupf
 */
public class Wechats {

    // -------------------------构建微信授权地址------------------------- //
    public static final String buildAuthorizeUrl(String appid, String redirect, String state) {
        return buildAuthorizeUrl(appid, Files.UTF_8, redirect, state);
    }

    public static final String buildAuthorizeUrl(String appid, String charset, 
                                                 String redirect, String state) {
        return buildAuthorizeUrl(appid, charset, redirect, state, "snsapi_base");
    }

    /**
     * 构建授权地址
     * @param appid
     * @param state
     * @param scope snsapi_base不弹出授权页面，直接跳转，只能获取用户openid
     *              snsapi_userinfo弹出授权页面，可通过openid拿到昵称、性别、所在地。
     *              并且，即使在未关注的情况下，只要用户授权，也能获取其信息
     * @param redirect
     * @return
     */
    public static final String buildAuthorizeUrl(String appid, String charset, 
                                                 String redirect, String state, String scope) {
        String url = "https://open.weixin.qq.com/connect/oauth2/authorize";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("appid", appid);
        params.put("redirect_uri", redirect);
        params.put("response_type", "code");
        params.put("scope", scope);
        params.put("state", state);
        return HttpParams.buildUrlPath(url, charset, params) + "#wechat_redirect";
    }

    // -------------------------通过授权地址的回调参数code换取网页授权access_token和openId------------------------- //
    /**
     * <pre>
     * scope=snsapi_userinfo
     *  {
     *    "access_token":"OezXcEiiBSKSxW0eow",
     *    "expires_in":7200,
     *    "refresh_token":"OezXcqDQy52232WDXB3Msuzq1A",
     *    "openid":"oLVPpjqs9BhvzwPj5A-vTYAX3GLc",
     *    "scope":"snsapi_userinfo,"
     *  }
     *  
     * scope=snsapi_base
     *  {
     *    "access_token": "OezXcEiiBSKSxW0eoylIeAsR0GmYd1awCffdHgb4fhS_KKf2CotGj2cBNUKQQvj-oJ9VmO-0Z-_izfnSAX_s0aqDsYkW4s8W5dLZ4iyNj5Y6vey3dgDtFki5C8r6D0E6mSVxxtb8BjLMhb-mCyT_Yg",
     *    "expires_in": 7200,
     *    "refresh_token": "OezXcEiiBSKSxW0eoylIeAsR0GmYd1awCffdHgb4fhS_KKf2CotGj2cBNUKQQvj-oJ9VmO-0Z-_izfnSAX_s0aqDsYkW4s8W5dLZ4iyNj5YBkF0ZUH1Ew8Iqea6x_itq13sYDqP1D7ieaDy9u2AHHw",
     *    "openid": "oLVPpjqs9BhvzwPj5A-vTYAX3GLc",
     *    "scope": "snsapi_base"
     *  }
     * </pre>
     * 
     * 获取微信openID及授权access_token
     * @param appid
     * @param secret
     * @param code   {@link #buildAuthorizeUrl(String, String, String, String, String)}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getOAuth2(String appid, String secret, String code) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("appid", appid);
        params.put("secret", secret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        Map<String, Object> result = Http.post("https://api.weixin.qq.com/sns/oauth2/access_token")
                                         .data(params).request(Map.class);

        checkError(result);
        return result;
    }

    /**
     * <pre>
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
     * </pre>
     * 
     * 通过OAuth2.0方式弹出授权页面获得用户基本信息（因scope=snsapi_userinfo会弹出授权页面）
     * @see Wechats#buildAuthorizeUrl(String, String, String, String, String) set scope=snsapi_userinfo
     * @param token  {@link #getOAuth(String, String, String)}
     * @param openid {@link #getOAuth(String, String, String)}
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getUserInfoByOAuth2(String token, String openid) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("access_token", token);
        params.put("openid", openid);
        params.put("lang", "zh_CN"); // this param can be unset
        Map<String, Object> result = Http.post("https://api.weixin.qq.com/sns/userinfo")
                                         .data(params).request(Map.class);

        checkError(result);
        return result;
    }

    // -------------------------获取全局access_token------------------------- //
    /**
     * get global access token
     * @param appid
     * @param secret
     * @return {access_token=token, expires_in=7200}
     */
    @SuppressWarnings("unchecked")
    public static String getAccessToken(String appid, String secret) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("grant_type", "client_credential");
        params.put("appid", appid);
        params.put("secret", secret);
        Map<String, Object> result = Http.post("https://api.weixin.qq.com/cgi-bin/token")
                                         .data(params).request(Map.class);

        checkError(result);
        return (String) result.get("access_token");
    }

    // -------------------------－－－－－－－通过全局access token获取用户信息－－－－－------------------------- //
    /**
     * <pre>
     *  1、用户关注以及回复消息的时候，均可以获得用户的OpenID
     *   <xml>
     *     <ToUserName><![CDATA[gh_b629c48b653e]]></ToUserName>
     *     <FromUserName><![CDATA[ollB4jv7LA3tydjviJp5V9qTU_kA]]></FromUserName>
     *     <CreateTime>1372307736</CreateTime>
     *     <MsgType><![CDATA[event]]></MsgType>
     *     <Event><![CDATA[subscribe]]></Event>
     *     <EventKey><![CDATA[]]></EventKey>
     *   </xml>
     *  其中的FromUserName就是OpenID
     * 
     *  2、返回数据格式
     *  {
     *    "subscribe": 1,
     *    "openid": "oLVPpjqs2BhvzwPj5A-vTYAX4GLc",
     *    "nickname": "刺猬宝宝",
     *    "sex": 1,
     *    "language": "zh_CN",
     *    "city": "深圳",
     *    "province": "广东",
     *    "country": "中国",
     *    "headimgurl": "http://wx.qlogo.cn/mmopen/JcDicrZBlREhnNXZRudod9PmibRkIs5K2f1tUQ7lFjC63pYHaXGxNDgMzjGDEuvzYZbFOqtUXaxSdoZG6iane5ko9H30krIbzGv/0",
     *    "subscribe_time": 1386160805
     *  }
     * </pre>
     * 
     * 通过全局Access Token获取用户基本信息
     * @param token  {@link #getAccessToken(String, String)}
     * @param openid
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getUserInfoByGlobal(String token, String openid) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("access_token", token);
        params.put("openid", openid);
        Map<String, Object> result = Http.post("https://api.weixin.qq.com/cgi-bin/user/info")
                                         .data(params).request(Map.class);

        checkError(result);
        return result;
    }

    // -------------------------获取api_ticket------------------------- //
    /**
     * 获取jsapi ticket
     * @param accessToken
     * @return
     */
    public static String getJsapiTicket(String accessToken) {
        return getTicket("jsapi", accessToken);
    }

    /**
     * 获取ticket
     * @param type        wx_card 卡券；jsapi js接口票据；
     * @param accessToken 通过调用getAccessToken()获取
     * @return {errcode=0, errmsg=ok, ticket=ticket, expires_in=7200}
     */
    @SuppressWarnings("unchecked")
    public static String getTicket(String type, String accessToken) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("access_token", accessToken);
        params.put("type", type);
        Map<String, Object> result = Http.post("https://api.weixin.qq.com/cgi-bin/ticket/getticket")
                                         .data(params).request(Map.class);

        checkError(result);
        return (String) result.get("ticket");
    }

    /**
     * 分享链接到朋友圈
     * @param jsapiTicket
     * @param appid
     * @param url
     * @return
     */
    public static Map<String, String> shareUrl(String jsapiTicket, String appid, String url) {
        Map<String, String> map = new HashMap<>();
        map.put("jsapi_ticket", jsapiTicket);
        map.put("noncestr", ObjectUtils.uuid22());
        map.put("timestamp", Long.toString(System.currentTimeMillis() / 1000));
        map.put("url", url.indexOf("#") == -1 ? url : url.substring(0, url.indexOf("#")));

        // generate sigin data
        map.put("signature", HashUtils.sha1Hex(HttpParams.buildSigning(map)));
        map.put("appid", appid);
        return map;
    }

    // ----------------------------private methods---------------------------
    private static void checkError(Map<String, ?> result) {
        Object errcode = result.get("errcode");
        if (errcode != null && !"0".equals(errcode.toString())) {
            throw new RuntimeException(Jsons.NORMAL.stringify(result));
        }
    }

    public static void main(String[] args) {
        //System.out.println(getOAuth("fsda", "fdasf", "fdasf"));
        //System.out.println(getUserInfo("fsda", "fdasf"));
        //System.out.println(getJsapiTicket("fsda"));
        System.out.println(shareUrl("jsapiTicket", "appid", "url"));
    }
}
