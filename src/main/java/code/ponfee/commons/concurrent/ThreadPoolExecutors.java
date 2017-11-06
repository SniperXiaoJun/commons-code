package code.ponfee.commons.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

/**
 * 线程池执行器创建
 * @author Ponfee
 */
public final class ThreadPoolExecutors {
    private ThreadPoolExecutors() {}

    private static final RejectedExecutionHandler DEFAULT_HANDLER = new CallerRunsPolicy();

    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, long keepAliveTime) {
        return create(corePoolSize, maximumPoolSize, keepAliveTime, 0, null, null);
    }

    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, 
                                            long keepAliveTime, int queueCapacity) {
        return create(corePoolSize, maximumPoolSize, keepAliveTime, queueCapacity, null, null);
    }

    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
                                            int queueCapacity, RejectedExecutionHandler rejectedHandler) {
        return create(corePoolSize, maximumPoolSize, keepAliveTime, queueCapacity, null, rejectedHandler);
    }

    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, 
                                            long keepAliveTime, int queueCapacity, String threadName) {
        return create(corePoolSize, maximumPoolSize, keepAliveTime, queueCapacity, threadName, null);
    }

    /**
     * 线程池创建器
     * @param corePoolSize     核心线程数
     * @param maximumPoolSize  最大线程数
     * @param keepAliveTime    线程存活时间
     * @param queueCapacity    队列长度
     * @param threadName       线程名称
     * @param rejectedHandler  拒绝策略
     * @return a ThreadPoolExecutor instance
     */
    public static ThreadPoolExecutor create(int corePoolSize, int maximumPoolSize, long keepAliveTime, 
                                            int queueCapacity, String threadName, 
                                            RejectedExecutionHandler rejectedHandler) {
        // BlockingQueue<Runnable> queue
        BlockingQueue<Runnable> workQueue;
        if (queueCapacity > 0) {
            workQueue = new LinkedBlockingQueue<>(queueCapacity);
        } else {
            workQueue = new SynchronousQueue<>();
        }

        // thread factory
        ThreadFactory threadFactory;
        if (StringUtils.isEmpty(threadName)) {
            threadFactory = Executors.defaultThreadFactory();
        } else {
            threadFactory = new NamedThreadFactory(threadName, true);
        }

        // rejected Handler Strategy 
        if (rejectedHandler == null) {
            rejectedHandler = DEFAULT_HANDLER;
        }

        // create ThreadPoolExecutor instance
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, 
                                      workQueue, threadFactory, rejectedHandler);
    }

}
