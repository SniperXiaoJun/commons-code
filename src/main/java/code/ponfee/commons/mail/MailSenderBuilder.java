package code.ponfee.commons.mail;

/**
 * 邮件发送构建类
 * @author fupf
 */
public final class MailSenderBuilder {

    private String user;
    private String password;
    private String smtpHost;
    private String nickname;

    private boolean authRequire = true;
    private Integer connTimeout; // 建立连接超时时间
    private Integer readTimeout; // 邮件发送读写超时时间
    private int validateTimes; // 邮箱验证次数
    private String charset; // 字符编码
    private int retryTimes; // 重试次数
    private MailSentFailedLogger sentFailedLogger;

    private MailSenderBuilder() {}

    public static MailSenderBuilder newBuilder(String user, String password) {
        return newBuilder(user, password, null);
    }

    public static MailSenderBuilder newBuilder(String user, String password, String smtpHost) {
        MailSenderBuilder builder = new MailSenderBuilder();
        builder.user = user;
        builder.password = password;
        builder.smtpHost = smtpHost;
        return builder;
    }

    public MailSenderBuilder nickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public MailSenderBuilder connTimeout(Integer connTimeout) {
        this.connTimeout = connTimeout;
        return this;
    }

    public MailSenderBuilder readTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public MailSenderBuilder charset(String charset) {
        this.charset = charset;
        return this;
    }

    public MailSenderBuilder retryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    public MailSenderBuilder validateTimes(int validateTimes) {
        this.validateTimes = validateTimes;
        return this;
    }

    public MailSenderBuilder authRequire(boolean authRequire) {
        this.authRequire = authRequire;
        return this;
    }

    public MailSenderBuilder sentFailedLogger(MailSentFailedLogger sentFailedLogger) {
        this.sentFailedLogger = sentFailedLogger;
        return this;
    }

    public MailSender build() {
        MailSender sender = new MailSender(user, password, smtpHost, authRequire, connTimeout, readTimeout);
        sender.setNickname(nickname);
        sender.setCharset(charset);
        sender.setRetryTimes(retryTimes);
        sender.setValidateTimes(validateTimes);
        sender.setSentFailedLogger(sentFailedLogger);
        return sender;
    }

}
