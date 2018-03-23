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

    public static void main(String[] args) {
        MailSenderBuilder builder = MailSenderBuilder.newBuilder("fupengfei163@163.com", "");
        builder.nickname("张三").connTimeout(5000).readTimeout(5000).charset("UTF-8")
                               .retryTimes(2).validateTimes(0).authRequire(false);
        MailSender sender = builder.build();
        MailEnvelope evp = MailEnvelope.newMimeInstance("fupengfei163@163.com", "图片", "<img src=\"cid:contentid123\" />");
        evp.setCc(new String[] { "395191357@qq.com", "unkonwn@fdsa.com" });
        evp.setBcc(new String[] { "unkonwn@test.com" });
        //evp.addContentImage("contentid123", "d:/QQ图片20170320235130.png");
        //evp.addAttachment("超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试.xlsx", "d:/abc.xlsx");
        //evp.addAttachment("超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试.txt", "d:/baidu.html");
        //evp.addAttachment("超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试.png", IOUtils.toByteArray(new FileInputStream("D:\\图片.png")));

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            ThreadPoolMailSender.send(sender, evp, true);
        }
        //sender.send(evp);
        System.out.println("end: " + (System.currentTimeMillis() - start));
    }
}
