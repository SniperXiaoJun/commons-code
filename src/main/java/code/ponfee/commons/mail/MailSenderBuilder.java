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

    private Integer connTimeout;
    private Integer readTimeout;
    private String charset;
    private int retryTimes;
    private int validateTimes;
    private boolean authRequire = true;
    private MailSentFailedLogger sentFailHandler;

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

    public MailSenderBuilder sentFailHandler(MailSentFailedLogger sentFailHandler) {
        this.sentFailHandler = sentFailHandler;
        return this;
    }

    public MailSender build() {
        MailSender sender = new MailSender(user, password, smtpHost, authRequire, connTimeout, readTimeout);
        sender.nickname(nickname);
        sender.charset(charset);
        sender.retryTimes(retryTimes);
        sender.validateTimes(validateTimes);
        sender.sentFailHandler(sentFailHandler);
        return sender;
    }

    public static void main(String[] args) throws Exception {
        MailSenderBuilder builder = MailSenderBuilder.newBuilder("fupengfei163@163.com", "");
        builder.nickname("张三").connTimeout(5000).readTimeout(5000).charset("UTF-8").retryTimes(2).validateTimes(0).authRequire(false);
        MailSender sender = builder.build();
        MailEnvelope evp = MailEnvelope.newMimeInstance("fupengfei163@163.com", "图片", "你好，看附件：<hr/><img src=\"cid:contentid123\" />");
        evp.setCc(new String[]{"395191357@qq.com", "fdsaewfdfa@fdsa.com"});
        evp.setBcc(new String[]{"fdsaewfdfa@test.com"});
        //evp.addContentImage("contentid123", "d:/QQ图片20170320235130.png");
        //evp.addAttachment("名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长.xlsx", "d:/abc.xlsx");
        //evp.addAttachment("百度名字有点长魂牵梦萦脍少朝秦暮楚脍塔顶地额外负担要暮云春树工奇巧魂牵梦萦地魂牵梦萦城.txt", "d:/baidu.html");
        //evp.addAttachment("QQ图片20170320235130字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名字有点长名.png", IOUtils.toByteArray(new FileInputStream("D:\\QQ图片20170320235130.png")));

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            ThreadPoolMailSender.send(sender, evp, true);
        }
        //sender.send(evp);
        System.out.println("end: " + (System.currentTimeMillis() - start));
    }
}
