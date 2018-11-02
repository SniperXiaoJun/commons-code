package code.ponfee.commons.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;

/**
 * Multi Thread executor
 * 
 * usual use in test case
 * 
 * @author Ponfee
 */
public class MultithreadExecutor {

    private static Logger logger = LoggerFactory.getLogger(MultithreadExecutor.class);

    /**
     * Moment async
     * 
     * @param times the exec times
     * @param command the command
     * @param momentSeconds the momentSeconds
     */
    public static void momentAsync(int times, Runnable command, int momentSeconds) {
        Stopwatch watch = Stopwatch.createStarted();
        AtomicBoolean flag = new AtomicBoolean(true);
        Thread[] threads = new Thread[times];
        for (int i = 0; i < times; i++) {
            threads[i] = new Thread(() -> {
                while (flag.get() && !Thread.interrupted()) {
                    command.run();
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(momentSeconds * 1000); // main thread sleep
            flag.set(false);

            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            flag.set(false);
            throw new RuntimeException(e);
        } finally {
            logger.info("multi thread execute duration: {}", watch.stop());
        }
    }

    // -----------------------------------------------------------------execAsync
    public static void execAsync(Runnable command, int times) {
        ExecutorService executor = Executors.newFixedThreadPool(times);
        try {
            execAsync(command, times, executor);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Exec async
     *  
     * @param command the command
     * @param times  the times to exec command
     * @param executor  thread executor service
     */
    public static void execAsync(Runnable command, int times, 
                                 Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        IntStream.range(0, times).mapToObj(
            x -> CompletableFuture.runAsync(command, executor)
        ).collect(
            Collectors.toList()
        ).stream().forEach(
            CompletableFuture::join
        );
        logger.info("multi thread execute duration: {}", watch.stop());
    }

    // -----------------------------------------------------------------runAsync
    public static <T> void runAsync(Collection<T> coll, Consumer<T> command) {
        ExecutorService executor = Executors.newFixedThreadPool(coll.size());
        try {
            runAsync(coll, command, executor);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Run async, consume the T collection
     * @param coll the T collection
     * @param command the T consumer
     * @param executor  thread executor service
     */
    public static <T> void runAsync(Collection<T> coll, Consumer<T> command, 
                                    Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        coll.stream().map(
            x -> CompletableFuture.runAsync(() -> command.accept(x), executor)
        ).collect(
            Collectors.toList()
        ).stream().forEach(
            CompletableFuture::join
        );
        logger.info("multi thread execute duration: {}", watch.stop());
    }

    // -----------------------------------------------------------------callAsync
    public static <T, U> List<U> callAsync(
        Collection<T> coll, Function<T, U> command) {
        ExecutorService executor = Executors.newFixedThreadPool(coll.size());
        try {
            return callAsync(coll, command, executor);
        } finally {
            executor.shutdown();
        }
    }

    /**
     * Call async, mapped T to U
     * 
     * @param coll the T collection
     * @param command  the mapper of T to U
     * @param executor thread executor service
     * @return the U collection
     */
    public static <T, U> List<U> callAsync(Collection<T> coll, Function<T, U> command, 
                                           Executor executor) {
        Stopwatch watch = Stopwatch.createStarted();
        List<U> result = coll.stream().map(
            x -> CompletableFuture.supplyAsync(() -> command.apply(x), executor)
        ).collect(
            Collectors.toList()
        ).stream().map(
            CompletableFuture::join
        ).collect(
            Collectors.toList()
        );
        logger.info("multi thread execute duration: {}", watch.stop());
        return result;
    }
}
