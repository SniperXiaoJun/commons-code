package test.jedis;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonLockTester {
    private static final String name = UUID.randomUUID().toString().substring(0, 3);

    public RedissonClient redisson;

    @Before
    public void setup() {
        Config config = new Config();
        config.useSingleServer().setAddress("127.0.0.1:6379");
        redisson = Redisson.create(config);
    }

    @After
    public void teardown() {
        redisson.shutdown();
    }

    @Test
    public void test() throws Exception {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getFile();
        path = new File(path).getParentFile().getParentFile().getPath() + "/src/test/java/";
        path += this.getClass().getCanonicalName().replace('.', '/') + ".java";
        Scanner s = new Scanner(new FileInputStream(path));
        final AtomicInteger num = new AtomicInteger(0);
        while (s.hasNextLine()) {
            final String line = s.nextLine();
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    new Printer(redisson.getLock("redisson:lock")).output(name + "-" + num.getAndIncrement() + "\t" + line + "\n");
                }
            });
            t.start();
            //t.join();
        }
        s.close();
        Thread.sleep(99999);
    }

    private static class Printer {
        private final Lock lock;

        Printer(Lock lock) {
            this.lock = lock;
        }

        private void output(String name) {
            lock.lock();
            try {
                for (int i = 0; i < name.length(); i++) {
                    System.out.print(name.charAt(i));
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
