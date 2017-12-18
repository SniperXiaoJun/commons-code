package test.jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import code.ponfee.commons.io.WrappedBufferedReader;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;
import code.ponfee.commons.util.MavenProjects;
import code.ponfee.commons.util.ObjectUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jedis-cfg.xml" })
public class JedisLockTester {
    private static final String NAME = ObjectUtils.uuid(3);
    private @Resource JedisClient jedisClient;

    @Before
    public void setup() {
        /*JedisPoolConfig poolCfg = new JedisPoolConfig();
        poolCfg.setMaxTotal(100);
        poolCfg.setMaxIdle(200);
        poolCfg.setMinIdle(100);
        poolCfg.setMaxWaitMillis(1000);
        poolCfg.setTestOnBorrow(false);
        poolCfg.setTestOnReturn(false);
        poolCfg.setTestWhileIdle(false);
        poolCfg.setNumTestsPerEvictionRun(-1);
        poolCfg.setMinEvictableIdleTimeMillis(60000);
        poolCfg.setTimeBetweenEvictionRunsMillis(30000);
        jedisClient = new JedisClient(poolCfg, "local1:127.0.0.1:6379", new KryoSerializer());*/
        //jedisClient = new JedisClient(poolCfg, "127.0.0.1:6379;127.0.0.1:6380;", new JdkSerializer());
    }

    @After
    public void teardown() {
        jedisClient.destroy();
    }

    @Test
    public void test1() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final Printer printer = new Printer(new JedisLock(jedisClient, "testLock1", 5));
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            Thread thread = new Thread(() -> {
                printer.output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            });
            thread.start();
            threads.add(thread);
        }
        reader.close();
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test2() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final Lock lock = new JedisLock(jedisClient, "testLock1", 5);
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            Thread thread = new Thread(() -> {
                new Printer(lock).output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            });
            thread.start();
            threads.add(thread);
        }
        reader.close();
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    @Test
    public void test3() throws IOException, InterruptedException {
        WrappedBufferedReader reader = new WrappedBufferedReader(MavenProjects.getTestJavaFile(this.getClass()));
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        List<Thread> threads = new ArrayList<>();
        System.out.println("\n=========================START========================");
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            Thread thread = new Thread(() -> {
                new Printer(new JedisLock(jedisClient, "testLock2", 5)).output(NAME + "-" + num.getAndIncrement() + "\t" + _line + "\n");
            });
            thread.start();
            threads.add(thread);
        }
        reader.close();
        for (Thread thread : threads) {
            thread.join();
        }
        System.out.println("=========================END========================\n");
    }

    private static class Printer {
        private final Lock lock;

        Printer(Lock lock) {
            this.lock = lock;
        }

        private void output(final String name) {
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
