package code.ponfee.commons.model;

import java.io.Serializable;

import code.ponfee.commons.json.Jsons;

/**
 * 公用错误码区间[100 ~ 999]
 * @author fupf
 */
public final class ResultCode implements Serializable {
    private static final long serialVersionUID = -679746150956111045L;

    /** 公用结果码 */
    public static final ResultCode SUCCESS            = create(200, "成功");
    public static final ResultCode ILLEGAL_ARGUMENTS  = create(400, "参数错误");
    public static final ResultCode UNAUTHORIZED       = create(401, "未授权");
    public static final ResultCode FORBIDDEN          = create(403, "拒绝访问");
    public static final ResultCode RESOURCE_NOT_FOUND = create(404, "资源未找到");
    public static final ResultCode REQUEST_TIMEOUT    = create(408, "请求超时");
    public static final ResultCode OCCUR_CONFLICT     = create(409, "出现冲突");
    public static final ResultCode SERVER_ERROR       = create(500, "服务器错误");
    public static final ResultCode SERVER_UNAVAILABLE = create(503, "服务不可用");
    public static final ResultCode SERVER_UNSUPPORTED = create(505, "服务不支持");

    private final Integer code;
    private final String msg;

    /**
     * 加上无参构造，解决反序例化报错问题
     */
    private ResultCode() {
        this(null, null);
    }

    private ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ResultCode create(int code, String msg) {
        return new ResultCode(code, msg);
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static void main(String[] args) {
        String json = Jsons.NORMAL.stringify(create(-999, "msg"));
        System.out.println(json);
        ResultCode result = Jsons.NORMAL.parse(json, ResultCode.class);
        System.out.println(Jsons.NORMAL.stringify(result));
    }
}
