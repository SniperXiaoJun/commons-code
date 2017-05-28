package code.ponfee.commons.mail;

import static org.apache.commons.net.smtp.SMTPReply.ACTION_OK;
import static org.apache.commons.net.smtp.SMTPReply.SERVICE_READY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.net.smtp.SMTPClient;
import org.apache.commons.net.smtp.SMTPReply;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

/**
 * 电子邮箱地址验证
 * @author fupf
 */
public class EMailValidator {

    private static final String[] HOSTNAME = { "techmagazine.ws", "internetmoment.com", "domain.com",
        "lifehacker.biz", "corbad.org", "megacheaphosting.com", "verify-email.org", "discursive.com" };
    private static final AtomicInteger COUNTER = new AtomicInteger();

    public static void main(String[] args) {
        for (int i = 0; i < 1; i++)
            System.out.println(verify("fupengfei163@163.com"));
    }

    /**
     * 验证
     * @param email
     * @return
     */
    public static boolean verify(String email) {
        StringBuilder log = new StringBuilder();
        String name = email.split("@")[1];
        SMTPClient client = new SMTPClient();
        //client.setDefaultTimeout(5000);
        //client.setConnectTimeout(5000);
        String hostname = HOSTNAME[COUNTER.getAndIncrement() % HOSTNAME.length];
        try {
            // 查找MX记录
            Lookup lookup = new Lookup(name, Type.MX);
            lookup.run();
            if (lookup.getResult() != Lookup.SUCCESSFUL) {
                log.append("找不到MX记录\n");
                return false;
            }

            // 连接到邮箱服务器
            List<String> hosts = new ArrayList<>();
            for (Record record : lookup.getAnswers()) {
                hosts.add(record.getAdditionalName().toString());
                client.connect(hosts.get(hosts.size() - 1));
                if (!SMTPReply.isPositiveCompletion(client.getReplyCode())) {
                    client.disconnect();
                    continue;
                } else {
                    log.append("MX record about " + name + " exists.\n");
                    log.append("Connection succeeded to " + hosts.get(hosts.size() - 1) + "\n");
                    break;
                }
            }

            if (!client.isConnected()) {
                log.append("Can't Connect These Hosts " + hosts + "\n");
                return false;
            }

            log.append(client.getReplyString() + "\n");
            if (SERVICE_READY != client.getReplyCode()) return false;

            client.login(hostname);
            log.append("> HELO " + hostname + "\n");
            log.append("=" + client.getReplyString() + "\n");
            if (ACTION_OK != client.getReplyCode()) return false;

            client.setSender("check@" + hostname);
            log.append("> MAIL FROM: <check@" + hostname + ">\n");
            log.append("=" + client.getReplyString() + "\n");
            if (ACTION_OK != client.getReplyCode()) return false;

            client.addRecipient(email);
            log.append("> RCPT TO: <" + email + ">\n");
            log.append("=" + client.getReplyString() + "\n");
            if (ACTION_OK != client.getReplyCode()) return false;

            client.logout();
            log.append("> QUIT" + "\n");
            log.append("=" + client.getReplyString() + "\n");
            return true;
        } catch (IOException ignored) {
            ignored.printStackTrace();
            return false;
        } finally {
            //System.out.println(log);
            if (client.isConnected()) try {
                client.disconnect();
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }
    }

    public static boolean verify(String email, int validateTimes) {
        for (int i = 0; i < validateTimes; i++) {
            if (!verify(email)) return false;
        }
        return true;
    }

}
