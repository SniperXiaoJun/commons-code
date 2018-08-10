package code.ponfee.commons.mail;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class MailTest {

    public static void main(String[] args) throws IOException {
        MailSenderBuilder builder = MailSenderBuilder.newBuilder("fupengfei163@163.com", "");
        builder.nickname("张三").connTimeout(5000).readTimeout(5000).charset("UTF-8")
               .retryTimes(2).validateTimes(0).authRequire(false);
        MailSender sender = builder.build();
        MailEnvelope evp = MailEnvelope.newMimeInstance("395191357@qq.com", "图片", "<img src=\"cid:contentid123\" />");
        evp.setCc(new String[] { "fupengfei.china@gmail.com", "unkonwn@fdsa.com" });
        evp.setBcc(new String[] { "fupengfei163@163.com", "abcdefdsfdsaf@abc.com" });
        evp.addContentImage("contentid123", "d:/20150820140846195.jpg");
        evp.addAttachment("超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试.xlsx", "d:/346576876987432534.xlsx");
        evp.addAttachment("超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试.html", "d:/百度一下，你就知道.html");
        evp.addAttachment("超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试超长中文名测试.pdf", IOUtils.toByteArray(new FileInputStream("D:\\LearnElasticSearch.pdf")));

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            ThreadPoolMailSender.send(sender, evp, true);
        }
        //sender.send(evp);
        System.out.println("end: " + (System.currentTimeMillis() - start));
    }
}
