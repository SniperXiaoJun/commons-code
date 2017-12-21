package test.log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;

import code.ponfee.commons.concurrent.RedisCircuitBreaker;
import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.serial.JdkSerializer;
import redis.clients.jedis.JedisPoolConfig;

public class FreqTester {

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
        jedisClient = new JedisClient(poolCfg, "127.0.0.1:6379;", new JdkSerializer());
    }
    
    @Test
    public void test1() throws InterruptedException {
        RedisCircuitBreaker f = new RedisCircuitBreaker(jedisClient, 1, 5);
        f.setRequestThreshold("abc", 70000);
        List<Thread> list = new ArrayList<>();
        AtomicBoolean flag = new AtomicBoolean(true);
        for (int i = 0; i < 50; i++) {
            list.add(new Thread(() -> {
                while (flag.get()) {
                    if (!f.checkpoint("abc")) {
                        System.err.println("error" + Thread.currentThread());
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
        for (Thread thread : list) {
            thread.start();
        }
        Thread.sleep(5000);
        flag.set(false);
        for (Thread thread : list) {
            thread.join();
        }
    }
}
