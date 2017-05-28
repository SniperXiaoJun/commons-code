package code.ponfee.commons.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import code.ponfee.commons.jce.hash.HashUtils;

/**
 * 缓存类
 * @author fupf
 * @param <T>
 */
public class Cache<T> {
    public static final long KEEPALIVE_FOREVER = 0;
    private final boolean caseSensitiveKey;
    private final boolean compressKey;
    private final int autoReleaseInSeconds;
    private final long keepAliveInMillis;
    private final Map<Comparable<?>, CacheBean<T>> cache;

    private final Lock lock = new ReentrantLock();
    private DateProvider dateProvider = DateProvider.SYSTEM;

    Cache(boolean caseSensitiveKey, boolean compressKey, int autoReleaseInSeconds, long keepAliveInMillis) {
        this.caseSensitiveKey = caseSensitiveKey;
        this.compressKey = compressKey;
        this.autoReleaseInSeconds = autoReleaseInSeconds;
        this.keepAliveInMillis = keepAliveInMillis;

        cache = new ConcurrentHashMap<Comparable<?>, CacheBean<T>>();
        if (this.autoReleaseInSeconds > 0) {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    cleanExpiration();
                }
            }, this.autoReleaseInSeconds, this.autoReleaseInSeconds, TimeUnit.SECONDS);
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

    public Integer getAutoReleaseInSeconds() {
        return autoReleaseInSeconds;
    }

    private long now() {
        return dateProvider.now();
    }

    protected void setDateProvider(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

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

    public void set(Comparable<?> key, long expireTimeMillis) {
        set(key, null, expireTimeMillis);
    }

    public void set(Comparable<?> key, T value, long expireTimeMillis) {
        if (expireTimeMillis == KEEPALIVE_FOREVER || expireTimeMillis > now()) {
            cache.put(getEffectiveKey(key), new CacheBean<T>(value, expireTimeMillis));
        }
    }

    /**
     * 获取并移除
     * @param key
     * @return
     */
    public T get(Comparable<?> key) {
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
     * 删除
     * @param key
     */
    public T remove(Comparable<?> key) {
        key = getEffectiveKey(key);
        CacheBean<T> cacheBean = cache.get(key);
        if (cacheBean == null) {
            return null;
        } else if (cacheBean.isExpire(now())) {
            cache.remove(key);
            return null;
        } else {
            cache.remove(key);
            return cacheBean.getValue();
        }
    }

    /**
     * 移除所有
     */
    public void clear() {
        cache.clear();
    }

    /**
     * 查看数量
     * @return
     */
    public int size() {
        int size = 0;
        for (CacheBean<T> cacheValue : cache.values()) {
            if (cacheValue.isAlive(now())) {
                size++;
            }
        }
        return size;
    }

    /**
     * 是否为空
     * @return
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(Comparable<?> key) {
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
        for (T val : values()) {
            if (val.equals(value)) return true;
        }
        return false;
    }

    /**
     * @return
     */
    public Collection<T> values() {
        Collection<T> values = new ArrayList<>();
        for (CacheBean<T> cacheValue : cache.values()) {
            if (cacheValue.isAlive(now())) {
                values.add(cacheValue.getValue());
            }
        }
        return values;
    }

    /**
     * @param key
     * @return
     */
    private Comparable<?> getEffectiveKey(Comparable<?> key) {
        if (CharSequence.class.isInstance(key)) {
            if (!caseSensitiveKey) {
                key = String.valueOf(key).toLowerCase(); // 不区分大小写
            }
            if (compressKey) {
                key = HashUtils.sha1Hex(String.valueOf(key)); // 压缩
            }
        }
        return key;
    }

    /**
     * 删除过期数据
     */
    private void cleanExpiration() {
        if (!lock.tryLock()) return;
        long now = now();
        try {
            for (Map.Entry<Comparable<?>, CacheBean<T>> entry : cache.entrySet()) {
                if (entry.getValue().isExpire(now)) cache.remove(entry.getKey());
            }
        } finally {
            lock.unlock();
        }
    }

}
