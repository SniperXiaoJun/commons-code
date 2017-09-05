package test.log;

import org.junit.Before;
import org.junit.Test;

import code.ponfee.commons.jedis.JedisClient;
import code.ponfee.commons.log.RedisFrequencyLimiter;
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
        RedisFrequencyLimiter f = new RedisFrequencyLimiter(jedisClient, 1, 5);
        f.setLimitsInMinute("abc", 50000000);
        for (int i = 0; i < 200; i++) {
            new Thread(){
                @Override
                public void run() {
                    while (true) {
                        if (!f.checkAndTrace("abc")) {
                            System.err.println("error" + Thread.currentThread());
                        }

                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                
            }.start();
        }
        Thread.sleep(10000);
        
    }
}
