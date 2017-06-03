package code.ponfee.commons.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(5, 20, 300, TimeUnit.SECONDS, 
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
        List<Future<Boolean>> futures = new ArrayList<>();
        for (MailEnvelope envlop : envlops) {
            futures.add(EXECUTOR.submit(new Sender(mailSender, envlop)));
        }

        if (async) {
            futures.clear();
            return true; // 异步发送，直接返回成功
        }

        // 同步发送
        boolean flag = true;
        for (Future<Boolean> future : futures) {
            try {
                //future.isDone(); // 是否完成
                if (!future.get()) flag = false;
            } catch (InterruptedException | ExecutionException e) {
                logger.error("thread send mail error", e);
                flag = false;
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
