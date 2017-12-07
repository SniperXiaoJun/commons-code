package code.ponfee.commons.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.base.Preconditions;

import code.ponfee.commons.math.Numbers;

/**
 * 异步批量数据中转站
 * @author fupf
 * @param <T>
 */
public final class AsyncBatchTransmitter<T> extends Thread {

    private final RunnableFactory<T> factory; // 线程工厂
    private final ThreadPoolExecutor executor; // 线程池执行器
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
    public AsyncBatchTransmitter(RunnableFactory<T> factory) {
        this(factory, 1000, 200);
    }

    /**
     * @param factory  消费线程工厂
     */
    public AsyncBatchTransmitter(RunnableFactory<T> factory, 
                                 int thresholdPeriod, int thresholdChunk) {
        this(factory, null, thresholdPeriod, thresholdChunk);
    }

    /**
     * @param factory          消费线程工厂
     * @param executor         线程执行器
     * @param thresholdPeriod  消费周期阀值
     * @param thresholdChunk   消费数量阀值
     */
    public AsyncBatchTransmitter(RunnableFactory<T> factory, ThreadPoolExecutor executor,
                                 int thresholdPeriod, int thresholdChunk) {
        Preconditions.checkArgument(thresholdPeriod > 0);
        Preconditions.checkArgument(thresholdChunk > 0);
        if (executor == null) {
            this.executor = ThreadPoolExecutors.create(0, 10, 300, 0, "async-batch-transmitter");
            this.needDestroyWhenEnd = true;
        } else {
            this.executor = executor;
            this.needDestroyWhenEnd = false;
        }
        this.factory = factory;
        this.thresholdPeriod = thresholdPeriod;
        this.sleepTimeMillis = Numbers.bounds(thresholdPeriod, 9, thresholdPeriod);
        this.thresholdChunk = thresholdChunk;

        super.setName("async-batch-transmitter-" + Integer.toHexString(hashCode()));
        super.setDaemon(true);
        super.start();
    }

    /**
     * thread run, don't to direct call into the code
     * it is a thread and the alone thread
     */
    public @Override void run() {
        T t;
        List<T> list = new ArrayList<>(thresholdChunk);
        for (;;) {
            if (isEnd && queue.isEmpty() && isRefresh()) {
                if (needDestroyWhenEnd) {
                    executor.shutdown();
                }
                break; // exit while loop when end
            }

            // 尽量不要使用queue.size()，时间复杂度O(n)
            if (!queue.isEmpty()) {
                for (int n = thresholdChunk - list.size(), i = 0; i < n; i++) {
                    t = queue.poll();
                    if (t == null) {
                        break; // break inner loop
                    } else {
                        list.add(t);
                    }
                }
            }

            if (list.size() == thresholdChunk || (!list.isEmpty() && (isEnd || isRefresh()))) {
                // task抛异常后： execute会输出错误信息，线程结束，后续任务会创建新线程执行
                //               submit不会输出错误信息，线程继续分配执行其它任务
                executor.submit(factory.create(list, isEnd && queue.isEmpty())); // 提交到异步批量处理
                list = new ArrayList<>(thresholdChunk);
                refresh();
            } else {
                try {
                    Thread.sleep(this.sleepTimeMillis); // to sleep for prevent endless loop
                } catch (InterruptedException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    /**
     * put one
     * @param t
     * @return
     */
    public boolean put(T t) {
        Preconditions.checkState(!isEnd);

        return queue.offer(t);
    }

    /**
     * batch put {@link #add(List)}
     * @param t
     * @return
     */
    public boolean put(@SuppressWarnings("unchecked") T... t) {
        Preconditions.checkState(!isEnd);

        return put(Arrays.asList(t));
    }

    /**
     * batch put
     * @param list
     * @return
     */
    public boolean put(List<T> list) {
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
        this.refresh();
        isEnd = true;
        this.refresh();
    }

    private void refresh() {
        lastConsumeTimeMillis = System.currentTimeMillis();
    }

    private boolean isRefresh() {
        return System.currentTimeMillis() - lastConsumeTimeMillis > thresholdPeriod;
    }

    // ---------------------------unsupported methods of Thread class--------------------------
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
