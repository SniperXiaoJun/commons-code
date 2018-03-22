package code.ponfee.commons.model;

import java.io.Serializable;

import code.ponfee.commons.json.Jsons;

/**
 * 公用错误码区间[000 ~ 999]
 * @author fupf
 */
public final class ResultCode implements Serializable {
    private static final long serialVersionUID = -679746150956111045L;

    /** 公用结果码 */
    public static final ResultCode SUCCESS            = create0(200, "成功");
    public static final ResultCode ILLEGAL_ARGS       = create0(400, "参数错误");
    public static final ResultCode UNAUTHORIZED       = create0(401, "未授权");
    public static final ResultCode FORBIDDEN          = create0(403, "拒绝访问");
    public static final ResultCode RESOURCE_UNFOUND   = create0(404, "资源未找到");
    public static final ResultCode REQUEST_TIMEOUT    = create0(408, "请求超时");
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
