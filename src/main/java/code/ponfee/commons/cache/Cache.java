package code.ponfee.commons.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import code.ponfee.commons.jce.hash.HashUtils;
import code.ponfee.commons.util.ObjectUtils;

/**
 * 缓存类
 * @author fupf
 * @param <T>
 */
public class Cache<T> {

    public static final long KEEPALIVE_FOREVER = 0; // 为0表示不失效

    private final boolean caseSensitiveKey; // 是否忽略大小写（只针对String）
    private final boolean compressKey; // 是否压缩key（只针对String）
    private final long keepAliveInMillis; // 默认的数据保存的时间
    private final Map<Comparable<?>, CacheBean<T>> cache = new ConcurrentHashMap<>(); // 缓存容器

    private volatile boolean isDestroy = false; // 是否被销毁
    private DateProvider dateProvider = DateProvider.SYSTEM;
    private final Lock lock = new ReentrantLock(); // 定时清理加锁
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    Cache(boolean caseSensitiveKey, boolean compressKey, long keepAliveInMillis, int autoReleaseInSeconds) {
        this.caseSensitiveKey = caseSensitiveKey;
        this.compressKey = compressKey;
        this.keepAliveInMillis = keepAliveInMillis;

        if (autoReleaseInSeconds > 0) {
            // 定时清理
            executor.scheduleAtFixedRate(() -> {
                if (!lock.tryLock()) return;
                try {
                    long now = now();
                    for (Iterator<Entry<Comparable<?>, CacheBean<T>>> t = cache.entrySet().iterator(); t.hasNext();) {
                        if (t.next().getValue().isExpire(now)) {
                            t.remove();
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }, autoReleaseInSeconds, autoReleaseInSeconds, TimeUnit.SECONDS);
        }
    }

    public boolean isCaseSensitiveKey() {
        return caseSensitiveKey;
    }

    public boolean isCompressKey() {
        return compressKey;
    }

    public long getKeepAliveInMillis() {
        return keepAliveInMillis;
    }

    public DateProvider getDateProvider() {
        return dateProvider;
    }

    private long now() {
        return dateProvider.now();
    }

    protected void setDateProvider(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    // --------------------------------cache value-------------------------------
    public void set(Comparable<?> key) {
        set(key, null);
    }

    public void set(Comparable<?> key, T value) {
        long expireTimeMillis;
        if (keepAliveInMillis > 0) {
            expireTimeMillis = now() + keepAliveInMillis;
        } else {
            expireTimeMillis = KEEPALIVE_FOREVER;
        }

        this.set(key, value, expireTimeMillis);
    }

    public void setWithAliveInMillis(Comparable<?> key, T value, int aliveInMillis) {
        this.set(key, value, now() + aliveInMillis);
    }

    public void setWithNull(Comparable<?> key, long expireTimeMillis) {
        set(key, null, expireTimeMillis);
    }

    public void set(Comparable<?> key, T value, long expireTimeMillis) {
        if (isDestroy) return;

        if (expireTimeMillis == KEEPALIVE_FOREVER || expireTimeMillis > now()) {
            cache.put(getEffectiveKey(key), new CacheBean<T>(value, expireTimeMillis));
        }
    }

    /**
     * 获取
     * @param key
     * @return
     */
    public T get(Comparable<?> key) {
        if (isDestroy) return null;

        key = getEffectiveKey(key);
        CacheBean<T> cacheBean = cache.get(key);
        if (cacheBean == null) {
            return null;
        } else if (cacheBean.isExpire(now())) {
            cache.remove(key);
            return null;
        } else {
            return cacheBean.getValue();
        }
    }

    /**
     * get value and remove it
     * @param key
     */
    public T getAndRemove(Comparable<?> key) {
        if (isDestroy) return null;

        CacheBean<T> cacheBean = cache.remove(getEffectiveKey(key));
        return cacheBean == null ? null : cacheBean.getValue();
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(Comparable<?> key) {
        if (isDestroy) return false;

        key = getEffectiveKey(key);
        CacheBean<T> cacheBean = cache.get(key);
        if (cacheBean == null) {
            return false;
        } else if (cacheBean.isExpire(now())) {
            cache.remove(key);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 是否包含指定的value
     * @param value
     * @return
     */
    public boolean containsValue(T value) {
        if (isDestroy) return false;

        Entry<Comparable<?>, CacheBean<T>> entry;
        for (Iterator<Entry<Comparable<?>, CacheBean<T>>> iter = cache.entrySet().iterator(); iter.hasNext();) {
            entry = iter.next();
            if (entry.getValue().isAlive(now())) {
                if ((value == null && entry.getValue().getValue() == null) ||
                    (value != null && value.equals(entry.getValue().getValue()))) {
                    return true;
                }
            } else {
                iter.remove();
            }
        }
        return false;
    }

    /**
     * get for value collection
     * @return  the collection of values
     */
    public Collection<T> values() {
        if (isDestroy) return Collections.emptyList();

        Collection<T> values = new ArrayList<>();
        Entry<Comparable<?>, CacheBean<T>> entry;
        for (Iterator<Entry<Comparable<?>, CacheBean<T>>> iter = cache.entrySet().iterator(); iter.hasNext();) {
            entry = iter.next();
            if (entry.getValue().isAlive(now())) {
                values.add(entry.getValue().getValue());
            } else {
                iter.remove();
            }
        }
        return values;
    }

    /**
     * get size of the cache keys
     * @return
     */
    public int size() {
        if (isDestroy) return 0;

        int size = 0;
        long now = now();

        for (Iterator<Entry<Comparable<?>, CacheBean<T>>> iter = cache.entrySet().iterator(); iter.hasNext();) {
            if (iter.next().getValue().isAlive(now)) {
                size++;
            } else {
                iter.remove();
            }
        }
        return size;
    }

    /**
     * check is empty
     * @return
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * clear all
     */
    public void clear() {
        if (isDestroy) return;

        cache.clear();
    }

    /**
     * destory the cache self
     */
    public void destroy() {
        isDestroy = true;
        executor.shutdown();
        cache.clear();
    }

    public boolean isDestroy() {
        return isDestroy;
    }

    /**
     * get effective key
     * @param key
     * @return
     */
    private Comparable<?> getEffectiveKey(Comparable<?> key) {
        if (CharSequence.class.isInstance(key)) {
            if (!caseSensitiveKey) {
                key = key.toString().toLowerCase(); // 不区分大小写（转小写）
            }
            if (compressKey) {
                key = HashUtils.sha1Hex(key.toString()); // 压缩key
            }
        }
        return key;
    }

    public static void main(String[] args) throws InterruptedException {
        Random random = new Random();
        DateProvider dateProvider = DateProvider.SYSTEM;
        Cache<Void> cache = CacheBuilder.newBuilder().caseSensitiveKey(false).compressKey(true)
                                                     .autoReleaseInSeconds(2).build();
        for (int i = 0; i < 10; i++) {
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        if (cache.isDestroy()) break;
                        cache.set(ObjectUtils.uuid(8), null, dateProvider.now() + random.nextInt(3000));
                    }
                }
            }.start();
        }

        for (int i = 0; i < 5; i++) {
            System.out.println(cache.size());
            Thread.sleep(1000);
        }
        cache.destroy();
    }

}
