package code.ponfee.commons.mail;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 邮件发送多线程池
 * @author fupf
 */
public class ThreadPoolMailSender {

    private static Logger logger = LoggerFactory.getLogger(ThreadPoolMailSender.class);
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, 10, 300, TimeUnit.SECONDS, 
                                                                           new SynchronousQueue<Runnable>(), 
                                                                           new ThreadPoolExecutor.CallerRunsPolicy());

    public static boolean send(MailSender mailSender, MailEnvelope envlop) {
        return send(mailSender, envlop, true);
    }

    public static boolean send(MailSender mailSender, MailEnvelope envlop, boolean async) {
        return send(mailSender, Arrays.asList(envlop), async);
    }

    public static boolean send(MailSender mailSender, List<MailEnvelope> envlops) {
        return send(mailSender, envlops, true);
    }

    /**
     * 批量发送
     * @param mailSender  发送器
     * @param envlops     邮件内容集合
     * @param async       true异步；false同步；
     * @return
     */
    public static boolean send(MailSender mailSender, List<MailEnvelope> envlops, boolean async) {
        boolean flag = true;
        if (async) { // 异步发送
            for (MailEnvelope envlop : envlops) {
                EXECUTOR.submit(new Sender(mailSender, envlop));
            }
        } else { // 同步发送
            CompletionService<Boolean> service = new ExecutorCompletionService<>(EXECUTOR);
            for (MailEnvelope envlop : envlops) {
                service.submit(new Sender(mailSender, envlop));
            }
            for (int number = envlops.size(); number > 0; number--) {
                try {
                    //future.isDone(); // 是否完成
                    if (!service.take().get()) {
                        flag = false;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("thread send mail error", e);
                    flag = false;
                }
            }
        }

        return flag;
    }

    /**
     * 异步发送
     */
    private static final class Sender implements Callable<Boolean> {
        MailSender mailSender;
        MailEnvelope envlop;

        Sender(MailSender mailSender, MailEnvelope envlop) {
            this.mailSender = mailSender;
            this.envlop = envlop;
        }

        @Override
        public Boolean call() throws Exception {
            return mailSender.send(envlop);
        }
    }

}
