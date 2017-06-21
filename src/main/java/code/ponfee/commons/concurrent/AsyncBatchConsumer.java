package code.ponfee.commons.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异步批量消费
 * @author fupf
 * @param <T>
 */
public final class AsyncBatchConsumer<T> implements Runnable {
    private final RunnableFactory<T> factory;
    private final ExecutorService executor;
    private final int thresholdPeriod;
    private final int thresholdQuantity;
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private long lastHandleTimeMillis = System.currentTimeMillis();
    private boolean needDistoryWhenEnd = false;
    private volatile boolean isEnd = false;

    public AsyncBatchConsumer(RunnableFactory<T> factory) {
        this(factory, new ThreadPoolExecutor(1, 20, 300, TimeUnit.SECONDS, 
                                             new SynchronousQueue<Runnable>(), 
                                             new ThreadPoolExecutor.CallerRunsPolicy()), 
             2000, 200);
        needDistoryWhenEnd = true;
    }

    public AsyncBatchConsumer(RunnableFactory<T> factory, ExecutorService executor,
        int thresholdPeriod, int thresholdQuantity) {
        this.factory = factory;
        this.executor = executor;
        this.thresholdPeriod = thresholdPeriod;
        this.thresholdQuantity = thresholdQuantity;
        Thread thread = new Thread(this, "async-batch-consumer-" + Integer.toHexString(hashCode()));
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        List<T> list = null;
        while (true) {
            if (isEnd && queue.isEmpty()) {
                if (needDistoryWhenEnd) {
                    executor.shutdown();
                }
                break; // exit while loop when end
            }

            if (list == null) {
                list = new ArrayList<>(thresholdQuantity);
            }

            // 尽量不要使用queue.size()，时间复杂度O(n)
            if (!queue.isEmpty()) {
                for (int n = thresholdQuantity - list.size(), i = 0; i < n; i++) {
                    T t = queue.poll();
                    if (t == null) break; // break for loop
                    list.add(t);
                }
            }

            if (list.size() > thresholdQuantity
                || (!list.isEmpty() && (isEnd || System.currentTimeMillis() - lastHandleTimeMillis > thresholdPeriod))) {
                // task抛异常后： execute会输出错误信息，线程结束，后续任务会创建新线程执行
                //               submit不会输出错误信息，线程继续分配执行其它任务
                executor.submit(factory.create(list, isEnd && queue.isEmpty())); // 提交到异步处理
                list = null;
                lastHandleTimeMillis = System.currentTimeMillis();
            }
        }
    }

    /**
     * consume for queue
     * @param t
     * @return
     */
    public boolean consume(T t) {
        return queue.offer(t);
    }

    /**
     * consume for queue
     * @param list
     * @return
     */
    public boolean consume(List<T> list) {
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
        final AsyncBatchConsumer<Integer> writer = new AsyncBatchConsumer<>((list, isEnd) -> {
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
                        Thread.sleep(150 + new Random().nextInt(1780));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    writer.consume(increment.getAndIncrement());
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
        Thread.sleep(20000);
        writer.end();
        System.out.println("to end:" + writer.queue.size());
        Thread.sleep(1000);
    }

}
