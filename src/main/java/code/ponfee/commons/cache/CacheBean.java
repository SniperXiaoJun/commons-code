package code.ponfee.commons.cache;

/**
 * 缓存Bean
 * @author fupf
 * @param <T>
 */
class CacheBean<T> {
    private long expireTimeMillis; // 失效时间
    private T value; // 值

    CacheBean(T value, long expireTimeMillis) {
        this.value = value;
        this.expireTimeMillis = expireTimeMillis;
    }

    public boolean isAlive(long refTimeMillis) {
        if (Cache.KEEPALIVE_FOREVER == expireTimeMillis) {
            return true;
        } else {
            return expireTimeMillis > refTimeMillis;
        }
    }

    public boolean isExpire(long refTimeMillis) {
        return !isAlive(refTimeMillis);
    }

    public T getValue() {
        return value;
    }

}
