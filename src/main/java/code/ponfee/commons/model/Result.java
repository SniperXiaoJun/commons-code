package code.ponfee.commons.model;

import java.beans.Transient;
import java.io.Serializable;

import code.ponfee.commons.json.Jsons;
import code.ponfee.commons.reflect.Fields;

/**
 * 返回结果数据结构体封装类
 * @param <T>
 * @author fupf
 */
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -2804195259517755050L;
    public static final Result<Void> SUCCESS = new SuccessResult();

    private Integer code; // 状态码
    private String msg; // 返回信息
    private T data; // 结果数据

    // -----------------------constructor method
    public Result() {} // code is null

    public Result(int code, String msg) {
        this(code, msg, null);
    }

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // -----------------------------static method
    public static Result<Void> success() {
        return SUCCESS;
    }

    public static <T> Result<T> success(T data) {
        return success("SUCCESS", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(SUCCESS.getCode(), msg, data);
    }

    public static <T> Result<T> failure(Enum<?> e) {
        int code = (int) Fields.get(e, "code");
        String msg = (String) Fields.get(e, "msg");
        return failure(code, msg, null);
    }

    public static <T> Result<T> failure(ResultCode rc) {
        return failure(rc.getCode(), rc.getMsg(), null);
    }

    public static <T> Result<T> failure(int code, String msg) {
        return failure(code, msg, null);
    }

    public static <T> Result<T> failure(int code, String msg, T data) {
        if (code == SUCCESS.getCode()) {
            throw new IllegalStateException("invalid failure code: " + code);
        }
        return new Result<>(code, msg, data);
    }

    // -----------------------------get/set method
    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setData(T data) {
        this.data = data;
    }

    public @Transient boolean isSuccess() {
        return code == SUCCESS.getCode();
    }

    @Override
    public String toString() {
        return this.toJson();
    }

    public String toJson() {
        return Jsons.NORMAL.stringify(this);
    }

    /**
     * 成功结果
     */
    private static final class SuccessResult extends Result<Void> {
        static final long serialVersionUID = 6740650053476768729L;

        SuccessResult() {
            super(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg());
        }

        @Override
        public void setCode(int code) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setMsg(String msg) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setData(Void _void) {
            throw new UnsupportedOperationException();
        }
    }

}
