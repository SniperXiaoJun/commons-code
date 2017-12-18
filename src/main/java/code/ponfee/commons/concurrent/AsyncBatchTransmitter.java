package code.ponfee.commons.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.base.Preconditions;

import code.ponfee.commons.math.Numbers;

/**
 * 异步批量数据中转站
 * @author Ponfee
 * @param <T>
 */
public final class AsyncBatchTransmitter<T> extends Thread {

    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private final AsyncBatchThread batch;
    private volatile boolean isEnd = false;

    public AsyncBatchTransmitter(RunnableFactory<T> factory) {
        this(factory, 1000, 200);
    }

    public AsyncBatchTransmitter(RunnableFactory<T> factory, 
                                 int thresholdPeriod, int thresholdChunk) {
        this(factory, thresholdPeriod, thresholdChunk, null);
    }

    /**
     * @param factory         消费线程工厂
     * @param thresholdPeriod 消费周期阀值
     * @param thresholdChunk  消费数量阀值
     * @param executor        线程执行器
     */
    public AsyncBatchTransmitter(RunnableFactory<T> factory, int thresholdPeriod, 
                                 int thresholdChunk, ThreadPoolExecutor executor) {
        this.batch = new AsyncBatchThread(factory, thresholdPeriod, 
                                          thresholdChunk, executor);
    }

    /**
     * put one
     * @param t
     * @return
     */
    public boolean put(T t) {
        return this.queue.offer(t);
    }

    /**
     * batch put {@link #add(List)}
     * @param t
     * @return
     */
    public boolean put(@SuppressWarnings("unchecked") T... ts) {
        if (ts == null || ts.length == 0) {
            return false;
        }

        boolean flag = true;
        for (T t : ts) {
            flag &= this.queue.offer(t);
        }
        return flag;
    }

    /**
     * batch put
     * @param list
     * @return
     */
    public boolean put(List<T> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }

        boolean flag = true;
        for (T t : list) {
            flag &= this.queue.offer(t);
        }
        return flag;
    }

    /**
     * 结束
     */
    public void end() {
        this.batch.refresh();
        this.isEnd = true;
    }

    /**
     * asnyc batch consume into this alone thread
     */
    private class AsyncBatchThread extends Thread {

        final RunnableFactory<T> factory; // 线程工厂
        final int sleepTimeMillis; // 休眠时间
        final int thresholdPeriod; // 消费周期阀值
        final int thresholdChunk; // 消费数量阀值
        final boolean needDestroyWhenEnd;
        final ThreadPoolExecutor executor;

        long lastConsumeTimeMillis = System.currentTimeMillis(); // 最近刷新时间

        /**
         * @param factory          消费线程工厂
         * @param executor         线程执行器
         * @param thresholdPeriod  消费周期阀值
         * @param thresholdChunk   消费数量阀值
         */
        AsyncBatchThread(RunnableFactory<T> factory,int thresholdPeriod, 
                         int thresholdChunk, ThreadPoolExecutor executor) {
            Preconditions.checkArgument(thresholdPeriod > 0);
            Preconditions.checkArgument(thresholdChunk > 0);

            this.factory = factory;
            this.sleepTimeMillis = Numbers.bounds(thresholdPeriod, 9, thresholdPeriod);
            this.thresholdPeriod = thresholdPeriod;
            this.thresholdChunk = thresholdChunk;
            if (executor == null) {
                this.needDestroyWhenEnd = true;
                this.executor = ThreadPoolExecutors.create(0, 10, 300, 0, "async-batch-transmitter");
            } else {
                this.needDestroyWhenEnd = false;
                this.executor = executor;
            }
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
                    //            submit不会输出错误信息，线程继续分配执行其它任务
                    executor.submit(factory.create(list, isEnd && queue.isEmpty())); // 提交到异步批量处理
                    list = new ArrayList<>(thresholdChunk);
                    refresh();
                } else {
                    try {
                        Thread.sleep(sleepTimeMillis); // to sleep for prevent endless loop
                    } catch (InterruptedException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }

        void refresh() {
            lastConsumeTimeMillis = System.currentTimeMillis();
        }

        boolean isRefresh() {
            return System.currentTimeMillis() - lastConsumeTimeMillis > thresholdPeriod;
        }
    }

}
