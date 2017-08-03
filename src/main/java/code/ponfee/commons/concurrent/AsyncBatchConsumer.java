package code.ponfee.commons.concurrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步批量消费
 * @author fupf
 * @param <T>
 */
public final class AsyncBatchConsumer<T> extends Thread {
    private final RunnableFactory<T> factory;
    private final ExecutorService executor;
    private final int thresholdPeriod;
    private final int thresholdChunk;
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private long lastProcessTimeMillis = System.currentTimeMillis();
    private boolean needDestroyWhenEnd = false;
    private volatile boolean isEnd = false;

    public AsyncBatchConsumer(RunnableFactory<T> factory) {
        this(factory, new ThreadPoolExecutor(1, 20, 300, TimeUnit.SECONDS, 
                                             new SynchronousQueue<Runnable>(), // 超过则让调用方线程处理
                                             new ThreadPoolExecutor.CallerRunsPolicy()), 
             2000, 200);
        needDestroyWhenEnd = true;
    }

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
    public void run() {
        List<T> list = null;
        while (true) {
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
                    if (t == null) break; // break for loop
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

    public static void main(String[] args) throws InterruptedException {
        final AsyncBatchConsumer<Integer> consumer = new AsyncBatchConsumer<>((list, isEnd) -> {
            return () -> {
                try {
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println(list.size() + "==" + Thread.currentThread().getId()
                    + "-" + Thread.currentThread().getName() + ", " + isEnd);
                System.out.println(1 / 0); // submit方式不会打印异常
            };
        });

        AtomicInteger increment = new AtomicInteger();
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(150 + ThreadLocalRandom.current().nextInt(1780));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    consumer.add(increment.getAndIncrement());
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
        Thread.sleep(20000);
        consumer.end();
        System.out.println("to end:" + consumer.queue.size());
        Thread.sleep(1000);
    }

}
