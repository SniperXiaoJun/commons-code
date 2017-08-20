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
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private boolean needDestroyWhenEnd = false;

    private volatile long lastProcessTimeMillis = System.currentTimeMillis();
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
        this(factory, new ThreadPoolExecutor(0, 10, 300, TimeUnit.SECONDS, 
                                             new SynchronousQueue<Runnable>(), // 超过则让调用方线程处理
                                             new ThreadPoolExecutor.CallerRunsPolicy()), 
             thresholdPeriod, thresholdChunk);
        needDestroyWhenEnd = true;
    }

    /**
     * @param factory          消费线程工厂
     * @param executor         线程执行器
     * @param thresholdPeriod  消费周期阀值
     * @param thresholdChunk   消费数量阀值
     */
    public AsyncBatchConsumer(RunnableFactory<T> factory, ExecutorService executor,
                              int thresholdPeriod, int thresholdChunk) {
        this.factory = factory;
        this.executor = executor;
        this.thresholdPeriod = thresholdPeriod;
        this.thresholdChunk = thresholdChunk;
        super.setName("async-batch-consumer-" + Integer.toHexString(hashCode()));
        super.setDaemon(true);
        super.start();
    }

    @Override
    public synchronized void start() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void run() {
        List<T> list = null;
        for (;;) {
            if (isEnd && queue.isEmpty()) {
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
                    T t = queue.poll();
                    if (t == null) break; // break inner for loop
                    list.add(t);
                }
            }

            if (list.size() > thresholdChunk
                || (!list.isEmpty() && (isEnd || System.currentTimeMillis() - lastProcessTimeMillis > thresholdPeriod))) {
                // task抛异常后： execute会输出错误信息，线程结束，后续任务会创建新线程执行
                //            submit不会输出错误信息，线程继续分配执行其它任务
                executor.submit(factory.create(list, isEnd && queue.isEmpty())); // 提交到异步处理
                list = null;
                lastProcessTimeMillis = System.currentTimeMillis();
            } else {
                try {
                    TimeUnit.MILLISECONDS.sleep(9); // sleep 9 millis seconds
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
        return queue.offer(t);
    }

    /**
     * batch add {@link #add(List)}
     * @param t
     * @return
     */
    public boolean add(@SuppressWarnings("unchecked") T... t) {
        return add(Arrays.asList(t));
    }

    /**
     * batch add
     * @param list
     * @return
     */
    public boolean add(List<T> list) {
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
    }

}
