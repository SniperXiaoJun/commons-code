package code.ponfee.commons.exception;

/**
 * 异常基类
 * @author fupf
 */
public class BasicException extends RuntimeException {
    private static final long serialVersionUID = -5678901285130119481L;

    /** 错误编码 */
    private Integer code;

    /**
     * 默认构造函数
     */
    public BasicException() {
        super();
    }

    /**
     * 默认构造函数
     */
    public BasicException(Integer code) {
        super();
        this.code = code;
    }

    /**
     * @param message 错误消息
     */
    public BasicException(String message) {
        super(message);
    }

    /**
     * @param cause 异常
     */
    public BasicException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BasicException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param code 错误编码
     * @param message 错误消息
     */
    public BasicException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * @param code 错误编码
     * @param cause 异常原因
     */
    public BasicException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    /**
     * @param code 错误编码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BasicException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @param code                错误编码
     * @param message             detail msg
     * @param cause               the cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public BasicException(Integer code, String message, Throwable cause, 
                          boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
    
    /**
     * 取得错误编码
     * @return
     */
    public Integer getCode() {
        return code;
    }

}
