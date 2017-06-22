package code.ponfee.commons.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import code.ponfee.commons.util.Networks;

/**
 * web工具类
 * @author fupf
 */
public final class WebUtils {
    private static final List<String> LOCAL_IP = Arrays.asList("127.0.0.1", "0:0:0:0:0:0:0:1");
    private final static Pattern PATTERN_SUFFIX = Pattern.compile("\\S*[?]\\S*");

    /** session trace */
    public static final String SESSION_TRACE_HEADER = "X-Auth-Token";
    public static final String SESSION_TRACE_COOKIE = "auth_token";
    public static final String SESSION_TRACE_PARAME = "authToken";

    /** charset */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 获取请求参数，<b>支付模块参数签名/验签时使用</b>
     * @param request
     * @return
     */
    public static Map<String, String> getReqParams(HttpServletRequest request) {
        Map<String, String[]> requestParams = request.getParameterMap();
        Map<String, String> params = new TreeMap<String, String>();
        for (Entry<String, String[]> entry : requestParams.entrySet()) {
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return params;
    }

    /**
     * 获取客户端ip
     * @param req
     * @return
     */
    public static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        if (ip != null && ip.indexOf(",") > 0) {
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割   
            ip = ip.substring(0, ip.indexOf(","));
        }
        if (LOCAL_IP.contains(ip)) {
            ip = Networks.getSiteIp(); // 如果是本机ip
        }
        return ip;
    }

    /**
     * 判断是否ajax请求
     * @param req
     * @return
     */
    public static boolean isAjax(HttpServletRequest req) {
        return "XMLHttpRequest".equals(req.getHeader("X-Requested-With"));
    }

    /**
     * 响应数据到请求客户端
     * @param resp
     * @param contentType
     * @param text
     * @param charset
     */
    public static void response(HttpServletResponse resp, String contentType, String text, String charset) {
        resp.setContentType(contentType + ";charset=" + charset);
        resp.setCharacterEncoding(charset);
        PrintWriter out = null;
        try {
            out = resp.getWriter();
            out.write(text);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) out.close();
        }
    }

    /**
     * 响应json数据
     * @param resp
     * @param json
     */
    public static void respJson(HttpServletResponse resp, String json) {
        respJson(resp, json, DEFAULT_CHARSET);
    }

    public static void respJson(HttpServletResponse resp, String json, String charset) {
        response(resp, "application/json", json, charset);
    }

    public static void respJsonp(HttpServletResponse response, String callback, String json) {
        respJsonp(response, callback, json, DEFAULT_CHARSET);
    }

    /**
     * 响应jsonp数据
     * @param resp
     * @param callback
     * @param json
     * @param charset
     */
    public static void respJsonp(HttpServletResponse resp, String callback, String json, String charset) {
        respJson(resp, callback + "(" + json + ");", charset);
    }

    /**
     * 允许跨站
     * @param resp
     */
    public static void cors(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Max-Age", "0");
        resp.setHeader("Access-Control-Allow-Headers", "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("XDomainRequestAllowed", "1");
    }

    /**
     * 获取请求地址后缀名
     * @param req
     * @return
     */
    public static String getUrlSuffix(HttpServletRequest req) {
        String url = req.getRequestURI();
        if (url.indexOf(".") < 0) return null;

        String[] pathInfos = url.toString().split("/");
        String endUrl = pathInfos[pathInfos.length - 1];
        if (PATTERN_SUFFIX.matcher(url).find()) {
            String[] spEndUrl = endUrl.split("\\?");
            return spEndUrl[0].split("\\.")[1];
        }
        return endUrl.split("\\.")[1];
    }

    /**
     * get cookie value
     * @param req
     * @param name
     * @return
     */
    public static String getCookie(HttpServletRequest req, String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    /**
     * 获取请求头参数
     * @param request
     * @param name
     * @return
     */
    public static String getHeader(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    /**
     * 设置cookie
     * @param response
     * @param name
     * @param value
     */
    public static void addCookie(HttpServletResponse response, String name, String value) {
        addCookie(response, name, value, "/", 24 * 60 * 60);
    }

    /**
     * 设置cookie
     * @param resp
     * @param name
     * @param value
     * @param path
     * @param maxAge
     */
    public static void addCookie(HttpServletResponse resp, String name, String value, String path, int maxAge) {
        resp.addCookie(createCookie(name, value, path, maxAge));
    }

    /**
     * 创建cookie
     * @param name
     * @param value
     * @param path
     * @param maxAge
     * @return
     */
    public static Cookie createCookie(String name, String value, String path, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    /**
     * 设置响应头
     * @param response
     * @param name
     * @param value
     */
    public static void addHeader(HttpServletResponse response, String name, String value) {
        response.addHeader(name, value);
    }

    /**
     * 获取request
     * @return
     */
    public static HttpServletRequest getRequest() {
        //return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return WebContext.getRequest();
    }

    /**
     * 获取response
     * @return
     */
    public static HttpServletResponse getResponse() {
        return WebContext.getResponse();
    }

    /**
     * 会话跟踪
     */
    public static void setSessionTrace(String token) {
        int maxAge = token == null ? 0 : 24 * 60 * 60;
        HttpServletResponse resp = getResponse();
        //result.setAuthToken(token); // to parame
        WebUtils.addCookie(resp, SESSION_TRACE_COOKIE, token, "/", maxAge); // to cookie
        WebUtils.addHeader(resp, SESSION_TRACE_HEADER, token); // to header
    }

    /**
     * 会话跟踪
     */
    public static String getSessionTrace() {
        HttpServletRequest req = getRequest();

        String authToken = req.getParameter(SESSION_TRACE_PARAME); // from parame
        if (authToken != null) return authToken;

        authToken = WebUtils.getCookie(req, SESSION_TRACE_COOKIE); // from cooike
        if (authToken != null) return authToken;

        return WebUtils.getHeader(req, SESSION_TRACE_HEADER); // from header;
    }

}
