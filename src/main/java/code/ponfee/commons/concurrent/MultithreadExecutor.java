package code.ponfee.commons.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

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

    public static void exec(int threadCount, Runnable runnable, int sleepSeconds) {
        Stopwatch watch = Stopwatch.createStarted();
        AtomicBoolean flag = new AtomicBoolean(true);
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                while (flag.get() && !Thread.interrupted()) {
                    runnable.run();
                }
            });
        }
        for (Thread thread : threads) {
            thread.start();
        }

        try {
            Thread.sleep(sleepSeconds * 1000); // main thread sleep
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
}
