package test.utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import code.ponfee.commons.cache.Cache;
import code.ponfee.commons.cache.CacheBuilder;
import code.ponfee.commons.util.Dates;
import code.ponfee.commons.util.ObjectUtils;

public class TestCache {

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        Cache<Void> cache = CacheBuilder.newBuilder().caseSensitiveKey(false).compressKey(true).autoReleaseInSeconds(2).build();
        AtomicBoolean flag = new AtomicBoolean(true);
        int n = 10;
        Thread[] threads = new Thread[n];
        for (int i = 0; i < n; i++) {
            threads[i] = new Thread(() -> {
                while (flag.get()) {
                    if (cache.isDestroy()) break;
                    cache.set(ObjectUtils.uuid(8), null, Dates.millis() + random.nextInt(3000));
                }
            });
        }

        for (Thread thread : threads) {
            thread.start();
        }
        for (int i = 0; i < 5; i++) {
            System.out.println(cache.size());
            Thread.sleep(1000);
        }
        flag.set(false);
        for (Thread thread : threads) {
            thread.join();
        }
        cache.destroy();
    }
}
