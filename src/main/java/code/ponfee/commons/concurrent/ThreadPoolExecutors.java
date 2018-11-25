package code.ponfee.commons.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import code.ponfee.commons.math.Numbers;

/**
 * Thread pool executor utility
 * 
 * @author Ponfee
 */
public final class ThreadPoolExecutors {
    private ThreadPoolExecutors() {}

    public static final int MAX_CAP = 0x7FFF; // max #workers - 1

    public static final RejectedExecutionHandler CALLER_RUN = new CallerRunsPolicy();

    public static final RejectedExecutionHandler DISCARD_POLICY = new DiscardPolicy();

    public static final ScheduledExecutorService CALLER_RUN_SCHEDULER =
        new DelegatedScheduledExecutorService("caller-run-sched", CALLER_RUN);

    public static final ScheduledExecutorService DISCARD_POLICY_SCHEDULER =
        new DelegatedScheduledExecutorService("discard-policy-sched", DISCARD_POLICY);

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
        // work queue
        BlockingQueue<Runnable> workQueue;
        if (queueCapacity > 0) {
            workQueue = new LinkedBlockingQueue<>(queueCapacity);
        } else {
            workQueue = new SynchronousQueue<>();
        }

        // thread factory, Executors.defaultThreadFactory()
        ThreadFactory threadFactory = new NamedThreadFactory(threadName);

        // rejected Handler Strategy 
        if (rejectedHandler == null) {
            rejectedHandler = CALLER_RUN;
        }

        maximumPoolSize = Numbers.bounds(maximumPoolSize, 1, MAX_CAP);
        corePoolSize = Numbers.bounds(corePoolSize, 1, maximumPoolSize);

        // create ThreadPoolExecutor instance
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, 
            workQueue, threadFactory, rejectedHandler
        );
        executor.allowCoreThreadTimeOut(true); // 设置允许核心线程超时关闭

        return executor;
    }

    private static class DelegatedScheduledExecutorService
        implements ScheduledExecutorService {

        private final ScheduledThreadPoolExecutor delegate;

        DelegatedScheduledExecutorService(String threadName,
                                          RejectedExecutionHandler handler) {
            this.delegate = new ScheduledThreadPoolExecutor(
                1, new NamedThreadFactory(threadName), handler
            );
            this.delegate.allowCoreThreadTimeOut(true);
            Runtime.getRuntime().addShutdownHook(
                new Thread(this.delegate::shutdownNow)
            );
        }

        @Override
        public void shutdown() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Runnable> shutdownNow() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isShutdown() {
            return this.delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return this.delegate.isTerminated();
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            return delegate.submit(task);
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            return delegate.submit(task, result);
        }

        @Override
        public Future<?> submit(Runnable task) {
            return delegate.submit(task);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) 
            throws InterruptedException {
            return delegate.invokeAll(tasks);
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, 
                                             long timeout, TimeUnit unit) 
            throws InterruptedException {
            return delegate.invokeAll(tasks, timeout, unit);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) 
            throws InterruptedException, ExecutionException {
            return delegate.invokeAny(tasks);
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, 
                               long timeout, TimeUnit unit) 
            throws InterruptedException, ExecutionException, TimeoutException {
            return delegate.invokeAny(tasks, timeout, unit);
        }

        @Override
        public void execute(Runnable command) {
            delegate.execute(command);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return delegate.schedule(command, delay, unit);
        }

        @Override
        public <V> ScheduledFuture<V> schedule(Callable<V> callable, 
                                               long delay, TimeUnit unit) {
            return delegate.schedule(callable, delay, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, 
                                                      long period, TimeUnit unit) {
            return delegate.scheduleAtFixedRate(command, initialDelay, period, unit);
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, 
                                                         long delay, TimeUnit unit) {
            return delegate.scheduleWithFixedDelay(command, initialDelay, delay, unit);
        }
    }

}
