package code.ponfee.commons.model;

import java.io.Serializable;

import code.ponfee.commons.json.Jsons;

/**
 * Http response code: {@link java.net.HttpURLConnection#HTTP_OK}
 *  100 => "HTTP/1.1 100 Continue",
 *  101 => "HTTP/1.1 101 Switching Protocols",
 *  200 => "HTTP/1.1 200 OK",
 *  201 => "HTTP/1.1 201 Created",
 *  202 => "HTTP/1.1 202 Accepted",
 *  203 => "HTTP/1.1 203 Non-Authoritative Information",
 *  204 => "HTTP/1.1 204 No Content",
 *  205 => "HTTP/1.1 205 Reset Content",
 *  206 => "HTTP/1.1 206 Partial Content",
 *  300 => "HTTP/1.1 300 Multiple Choices",
 *  301 => "HTTP/1.1 301 Moved Permanently",
 *  302 => "HTTP/1.1 302 Found",
 *  303 => "HTTP/1.1 303 See Other",
 *  304 => "HTTP/1.1 304 Not Modified",
 *  305 => "HTTP/1.1 305 Use Proxy",
 *  307 => "HTTP/1.1 307 Temporary Redirect",
 *  400 => "HTTP/1.1 400 Bad Request",
 *  401 => "HTTP/1.1 401 Unauthorized",
 *  402 => "HTTP/1.1 402 Payment Required",
 *  403 => "HTTP/1.1 403 Forbidden",
 *  404 => "HTTP/1.1 404 Not Found",
 *  405 => "HTTP/1.1 405 Method Not Allowed",
 *  406 => "HTTP/1.1 406 Not Acceptable",
 *  407 => "HTTP/1.1 407 Proxy Authentication Required",
 *  408 => "HTTP/1.1 408 Request Time-out",
 *  409 => "HTTP/1.1 409 Conflict",
 *  410 => "HTTP/1.1 410 Gone",
 *  411 => "HTTP/1.1 411 Length Required",
 *  412 => "HTTP/1.1 412 Precondition Failed",
 *  413 => "HTTP/1.1 413 Request Entity Too Large",
 *  414 => "HTTP/1.1 414 Request-URI Too Large",
 *  415 => "HTTP/1.1 415 Unsupported Media Type",
 *  416 => "HTTP/1.1 416 Requested range not satisfiable",
 *  417 => "HTTP/1.1 417 Expectation Failed",
 *  500 => "HTTP/1.1 500 Internal Server Error",
 *  501 => "HTTP/1.1 501 Not Implemented",
 *  502 => "HTTP/1.1 502 Bad Gateway",
 *  503 => "HTTP/1.1 503 Service Unavailable",
 *  504 => "HTTP/1.1 504 Gateway Time-out"
 *
 * https://baike.baidu.com/item/HTTP%E7%8A%B6%E6%80%81%E7%A0%81/5053660?fr=aladdin
 *
 * 公用错误码区间[000 ~ 999]
 *
 * @author fupf
 */
public final class ResultCode implements Serializable {
    private static final long serialVersionUID = -679746150956111045L;

    /** 公用结果码 */
    public static final ResultCode OK                 = create0(200, "OK");
    public static final ResultCode CREATED            = create0(201, "已创建");
    public static final ResultCode NO_CONTENT         = create0(204, "无内容");
    public static final ResultCode REST_CONTENT       = create0(205, "请重置");

    public static final ResultCode REDIRECT           = create0(301, "重定向");

    public static final ResultCode BAD_REQUEST        = create0(400, "请求错误");
    public static final ResultCode UNAUTHORIZED       = create0(401, "未授权");
    public static final ResultCode FORBIDDEN          = create0(403, "拒绝访问");
    public static final ResultCode NOT_FOUND          = create0(404, "资源未找到");
    public static final ResultCode CLIENT_TIMEOUT     = create0(408, "请求超时");
    public static final ResultCode OPS_CONFLICT       = create0(409, "操作冲突");

    public static final ResultCode SERVER_ERROR       = create0(500, "服务器错误");
    public static final ResultCode SERVER_UNAVAILABLE = create0(503, "服务不可用");
    public static final ResultCode SERVER_UNSUPPORT   = create0(505, "服务不支持");

    private final Integer code;
    private final String msg;

    /**
     * 加上无参构造，解决反序例化报错问题
     */
    private ResultCode() {
        this(null, null); // code is null
    }

    private ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * inner create, only call in this class
     * use in assign the commons code 
     * @param code
     * @param msg
     * @return
     */
    private static ResultCode create0(int code, String msg) {
        return new ResultCode(code, msg);
    }

    /**
     * others place cannot set the code in commons code range[000 ~ 999]
     * @param code
     * @param msg
     * @return
     */
    public static ResultCode create(int code, String msg) {
        if (code >= 0 && code < 1000) {
            throw new IllegalArgumentException("the code must not between 0 and 999.");
        }
        return new ResultCode(code, msg);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static void main(String[] args) {
        String json = Jsons.toJson(create(-999, "msg"));
        System.out.println(json);
        ResultCode result = Jsons.fromJson(json, ResultCode.class);
        System.out.println(Jsons.toJson(result));
    }
}
