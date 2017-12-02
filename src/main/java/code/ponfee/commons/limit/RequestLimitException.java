package code.ponfee.commons.limit;

/**
 * 请求超限异常
 * @author Ponfee
 */
public class RequestLimitException extends Exception {

    private static final long serialVersionUID = 2493768018114069549L;

    /**
     * @param message 错误消息
     */
    public RequestLimitException(String message) {
        super(message);
    }

}
