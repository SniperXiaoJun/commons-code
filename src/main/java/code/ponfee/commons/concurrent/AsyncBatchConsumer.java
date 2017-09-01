package code.ponfee.commons.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import code.ponfee.commons.util.Numbers;

/**
 * 异步批量消费
 * @author fupf
 * @param <T>
 */
public final class AsyncBatchConsumer<T> extends Thread {

    private final RunnableFactory<T> factory; // 线程工厂
    private final ExecutorService executor; // 线程池执行器
    private final int thresholdPeriod; // 消费周期阀值
    private final int thresholdChunk; // 消费数量阀值
    private final int sleepTimeMillis; // 休眠时间
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private final boolean needDestroyWhenEnd;

    private long lastConsumeTimeMillis = System.currentTimeMillis();
    private volatile boolean isEnd = false;

    /**
     * @param factory  消费线程工厂
     */
    public AsyncBatchConsumer(RunnableFactory<T> factory) {
        this(factory, 1000, 200);
    }

    /**
     * @param factory  消费线程工厂
     */
    public AsyncBatchConsumer(RunnableFactory<T> factory, int thresholdPeriod, int thresholdChunk) {
        this(factory, null, thresholdPeriod, thresholdChunk);
    }

    /**
     * @param factory          消费线程工厂
     * @param executor         线程执行器
     * @param thresholdPeriod  消费周期阀值
     * @param thresholdChunk   消费数量阀值
     */
    public AsyncBatchConsumer(RunnableFactory<T> factory, ExecutorService executor,
                              int thresholdPeriod, int thresholdChunk) {
        Preconditions.checkArgument(thresholdPeriod > 0);
        Preconditions.checkArgument(thresholdChunk > 0);
        if (executor == null) {
            this.executor = new ThreadPoolExecutor(0, 10, 300, TimeUnit.SECONDS, 
                                                   new SynchronousQueue<Runnable>(), // 超过则让调用方线程处理
                                                   new ThreadPoolExecutor.CallerRunsPolicy());
            this.needDestroyWhenEnd = true;
        } else {
            this.executor = executor;
            this.needDestroyWhenEnd = false;
        }
        this.factory = factory;
        this.thresholdPeriod = thresholdPeriod;
        this.sleepTimeMillis = Numbers.bounds(thresholdPeriod, 9, thresholdPeriod);
        this.thresholdChunk = thresholdChunk;
        super.setName("async-batch-consumer-" + Integer.toHexString(hashCode()));
        super.setDaemon(true);
        super.start();
    }

    /**
     * thread run, don't to direct call into the code
     */
    public @Override void run() {
        List<T> list = null;
        T t;
        for (;;) {
            if (isEnd && queue.isEmpty() && isRefresh()) {
                if (needDestroyWhenEnd) {
                    executor.shutdown();
                }
                break; // exit while loop when end
            }

            if (list == null) {
                list = new ArrayList<>(thresholdChunk);
            }

            // 尽量不要使用queue.size()，时间复杂度O(n)
            if (!queue.isEmpty()) {
                for (int n = thresholdChunk - list.size(), i = 0; i < n; i++) {
                    t = queue.poll();
                    if (t == null) {
                        break; // break inner for loop
                    } else {
                        list.add(t);
                    }
                }
            }

            if (list.size() == thresholdChunk || (!list.isEmpty() && (isEnd || isRefresh()))) {
                // task抛异常后： execute会输出错误信息，线程结束，后续任务会创建新线程执行
                //               submit不会输出错误信息，线程继续分配执行其它任务
                executor.submit(factory.create(list, isEnd && queue.isEmpty())); // 提交到异步处理
                list = null;
                refresh();
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(this.sleepTimeMillis); // sleep
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    /**
     * add one
     * @param t
     * @return
     */
    public boolean add(T t) {
        Preconditions.checkState(!isEnd);

        return queue.offer(t);
    }

    /**
     * batch add {@link #add(List)}
     * @param t
     * @return
     */
    public boolean add(@SuppressWarnings("unchecked") T... t) {
        Preconditions.checkState(!isEnd);

        return add(Arrays.asList(t));
    }

    /**
     * batch add
     * @param list
     * @return
     */
    public boolean add(List<T> list) {
        Preconditions.checkState(!isEnd);

        boolean flag = true;
        for (T t : list) {
            if (!queue.offer(t)) {
                flag = false;
            }
        }
        return flag;
    }

    public void end() {
        isEnd = true;
        this.refresh();
    }

    private void refresh() {
        lastConsumeTimeMillis = System.currentTimeMillis();
    }

    private boolean isRefresh() {
        return System.currentTimeMillis() - lastConsumeTimeMillis > thresholdPeriod;
    }

    public @Override void interrupt() {
        throw new UnsupportedOperationException();
    }

    public @Override void destroy() {
        throw new UnsupportedOperationException();
    }

    public @Override void setContextClassLoader(ClassLoader cl) {
        throw new UnsupportedOperationException();
    }

    public @Override void setUncaughtExceptionHandler(UncaughtExceptionHandler eh) {
        throw new UnsupportedOperationException();
    }

    public @Override synchronized void start() {
        throw new UnsupportedOperationException();
    }

}
