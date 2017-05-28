package code.ponfee.commons.jedis;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;

/**
 * redis key（键）操作类
 * @author fupf
 */
public class KeysOperations extends JedisOperations {
    private static Logger logger = LoggerFactory.getLogger(KeysOperations.class);

    KeysOperations(JedisClient jedisClient) {
        super(jedisClient);
    }

    /**
     * 获取有效期
     * @param key
     * @return
     */
    public Long ttl(final String key) {
        return new JedisHook<Long>(this) {
            @Override
            Long operate(ShardedJedis shardedJedis) {
                return shardedJedis.ttl(key);
            }

            @Override
            Long except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    /**
     * 获取有效期
     * @param key
     * @return
     */
    public Long pttl(final String key) {
        return new JedisHook<Long>(this) {
            @Override
            Long operate(ShardedJedis shardedJedis) {
                return shardedJedis.pttl(key);
            }

            @Override
            Long except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    /**
     * 获取key列表
     * @param keyWildcard
     * @return
     */
    public Set<String> keys(final String keyWildcard) {
        return new JedisHook<Set<String>>(this) {
            @Override
            Set<String> operate(ShardedJedis shardedJedis) {
                Set<String> keys = new HashSet<>();
                for (Jedis jedis : shardedJedis.getAllShards()) {
                    keys.addAll(jedis.keys(keyWildcard));
                }
                return keys;
            }

            @Override
            Set<String> except(JedisException e) {
                logger.error(ops.buildError(keyWildcard), e);
                return null;
            }
        }.hook();
    }

    /**
     * 设置失效时间
     * @param key
     * @param seconds
     * @return 是否设置成功
     */
    public boolean expire(final String key, final int seconds) {
        return new JedisHook<Boolean>(this) {
            @Override
            Boolean operate(ShardedJedis shardedJedis) {
                return JedisOperations.expire(shardedJedis, key, seconds);
            }

            @Override
            Boolean except(JedisException e) {
                logger.error(ops.buildError(key, seconds), e);
                return false;
            }
        }.hook();
    }

    /**
     * 设置失效时间
     * @param key
     * @param milliseconds
     * @return 是否设置成功
     */
    public boolean pexpire(final String key, final int milliseconds) {
        return new JedisHook<Boolean>(this) {
            @Override
            Boolean operate(ShardedJedis shardedJedis) {
                return JedisOperations.pexpire(shardedJedis, key, milliseconds);
            }

            @Override
            Boolean except(JedisException e) {
                logger.error(ops.buildError(key, milliseconds), e);
                return false;
            }
        }.hook();
    }

    /**
     * 判断key是否存在
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return new JedisHook<Boolean>(this) {
            @Override
            Boolean operate(ShardedJedis shardedJedis) {
                return shardedJedis.exists(key);
            }

            @Override
            Boolean except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return false;
            }
        }.hook();
    }

    /**
     * 删除
     * @param key
     * @return 被删除 key 的数量
     */
    public Long del(final String key) {
        return new JedisHook<Long>(this) {
            @Override
            Long operate(ShardedJedis shardedJedis) {
                return shardedJedis.del(key);
            }

            @Override
            Long except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    /**
     * 删除
     * @param key
     * @return 被删除 key 的数量
     */
    public Long del(final byte[] key) {
        return new JedisHook<Long>(this) {
            @Override
            Long operate(ShardedJedis shardedJedis) {
                return shardedJedis.del(key);
            }

            @Override
            Long except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    /**
     * 删除key（匹配通配符）
     * @param keyWildcard
     * @return 被删除 key 的数量
     */
    public long dels(final String keyWildcard) {
        return new JedisHook<Long>(this) {
            @Override
            Long operate(ShardedJedis shardedJedis) {
                long delCounts = 0;
                for (Jedis jedis : shardedJedis.getAllShards()) {
                    Set<String> keys = jedis.keys(keyWildcard);
                    if (keys != null && keys.size() > 0) {
                        delCounts += jedis.del(keys.toArray(new String[keys.size()]));
                    }
                }
                return delCounts;
            }

            @Override
            Long except(JedisException e) {
                logger.error(ops.buildError(keyWildcard), e);
                return 0L;
            }
        }.hook();
    }

    /**
     * 返回 key 所储存的值的类型。
     * @param key
     * @return none (key不存在)；string (字符串)；list (列表)；set (集合)；zset (有序集)；hash (哈希表)
     */
    public String type(final String key) {
        return new JedisHook<String>(this) {
            @Override
            String operate(ShardedJedis shardedJedis) {
                return shardedJedis.type(key);
            }

            @Override
            String except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    /**
     * key watch操作
     * @param key
     * @return
     */
    public String watch(final String key) {
        return new JedisHook<String>(this) {
            @Override
            String operate(ShardedJedis shardedJedis) {
                return shardedJedis.getShard(key).watch(key);
            }

            @Override
            String except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    /**
     * key unwatch操作
     * @param key
     * @return
     */
    public String unwatch(final String key) {
        return new JedisHook<String>(this) {
            @Override
            String operate(ShardedJedis shardedJedis) {
                return shardedJedis.getShard(key).unwatch();
            }

            @Override
            String except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    /**
     * multi 操作
     * @param key
     * @return
     */
    public Transaction multi(final String key) {
        return new JedisHook<Transaction>(this) {
            @Override
            Transaction operate(ShardedJedis shardedJedis) {
                return shardedJedis.getShard(key).multi();
            }

            @Override
            Transaction except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    public Transaction multi(final byte[] key) {
        return new JedisHook<Transaction>(this) {
            @Override
            Transaction operate(ShardedJedis shardedJedis) {
                return shardedJedis.getShard(key).multi();
            }

            @Override
            Transaction except(JedisException e) {
                logger.error(ops.buildError(key), e);
                return null;
            }
        }.hook();
    }

    /**
     * 将脚本 script 添加到脚本缓存中，但并不立即执行这个脚本。
     * @param script
     * @return 给定 script 的 SHA1 校验和
     */
    public String scriptLoad(final String script) {
        return new JedisHook<String>(this) {
            @Override
            String operate(ShardedJedis shardedJedis) {
                return shardedJedis.getShard(script).scriptLoad(script);
            }

            @Override
            String except(JedisException e) {
                logger.error(ops.buildError(script), e);
                return null;
            }
        }.hook();
    }

}
