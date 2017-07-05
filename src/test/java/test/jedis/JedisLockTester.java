package test.jedis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.jedis.JedisLock;
import code.ponfee.commons.serial.JdkSerializer;
import redis.clients.jedis.JedisPoolConfig;

public class JedisLockTester {
    private static final String name = UUID.randomUUID().toString().substring(0, 3);
    private JedisClient jedisClient;

    @Before
    public void setup() {
        JedisPoolConfig poolCfg = new JedisPoolConfig();
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
        //jedisClient = new JedisClient(poolCfg, "local1:127.0.0.1:6379", new KryoSerializer());
        jedisClient = new JedisClient(poolCfg, "127.0.0.1:6379;127.0.0.1:6380;", new JdkSerializer());
    }

    @After
    public void teardown() {
        jedisClient.destroy();
    }

    @Test
    public void test1() throws IOException, InterruptedException {
        BufferedReader reader = read();
        final Printer printer = new Printer(new JedisLock(jedisClient, "testLock1", 5));
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    printer.output(name + "-" + num.getAndIncrement() + "\t" + _line + "\n");
                }
            }).start();
        }
        reader.close();
        Thread.sleep(99999);
    }

    @Test
    public void test2() throws IOException, InterruptedException {
        BufferedReader reader = read();
        final Lock lock = new JedisLock(jedisClient, "testLock1", 5);
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new Printer(lock).output(name + "-" + num.getAndIncrement() + "\t" + _line + "\n");
                }
            }).start();
        }
        reader.close();
        Thread.sleep(99999);
    }

    @Test
    public void test3() throws IOException, InterruptedException {
        BufferedReader reader = read();
        final AtomicInteger num = new AtomicInteger(0);
        String line = null;
        while ((line = reader.readLine()) != null) {
            final String _line = line;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new Printer(new JedisLock(jedisClient, "testLock2", 5)).output(name + "-" + num.getAndIncrement() + "\t" + _line + "\n");
                }
            }).start();
        }
        reader.close();
        Thread.sleep(99999);
    }

    private BufferedReader read() throws FileNotFoundException {
        String path = Thread.currentThread().getContextClassLoader().getResource("").getFile();
        path = new File(path).getParentFile().getParentFile().getPath() + "/src/test/java/";
        path += this.getClass().getCanonicalName().replace('.', '/') + ".java";
        return new BufferedReader(new InputStreamReader(new FileInputStream(path)));
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
