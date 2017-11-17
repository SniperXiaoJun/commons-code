package test.concurrent;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import code.ponfee.commons.concurrent.AsyncBatchTransmitter;

public class TestBatchTransmitter {

    public static void main(String[] args) throws InterruptedException {
        final AsyncBatchTransmitter<Integer> transmitter = new AsyncBatchTransmitter<>((list, isEnd) -> {
            return () -> {
                System.out.println(list.size() + "==" + Thread.currentThread().getId()
                    + "-" + Thread.currentThread().getName() + ", " + isEnd);
                //System.out.println(1 / 0); // submit方式不会打印异常
            };
        });

        AtomicBoolean flag = new AtomicBoolean(true);
        AtomicInteger increment = new AtomicInteger(0);
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                while (flag.get()) {
                    try {
                        Thread.sleep(85 + ThreadLocalRandom.current().nextInt(850));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    transmitter.put(increment.getAndIncrement());
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
        Thread.sleep(5000);
        flag.set(false);
        Thread.sleep(1000);
        transmitter.end();
        System.out.println(increment.get());
        Thread.sleep(1000);
    }
}