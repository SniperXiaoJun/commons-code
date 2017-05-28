package code.ponfee.commons.cache;

/**
 * 缓存构建类
 * @author fupf
 */
public final class CacheBuilder {
    private boolean caseSensitiveKey = true; // （默认）区分大小写
    private boolean compressKey = false; // （默认）不压缩key
    private int autoReleaseInSeconds = 0; // （默认0为不清除）清除无效key的的定时时间间隔
    private long keepaliveInMillis = 0; // key保留时间，0表示无限制

    private CacheBuilder() {}

    public CacheBuilder caseSensitiveKey(boolean caseSensitiveKey) {
        this.caseSensitiveKey = caseSensitiveKey;
        return this;
    }

    public CacheBuilder compressKey(boolean compressKey) {
        this.compressKey = compressKey;
        return this;
    }

    public CacheBuilder autoReleaseInSeconds(Integer autoReleaseInSeconds) {
        this.autoReleaseInSeconds = autoReleaseInSeconds;
        return this;
    }

    public CacheBuilder keepaliveInMillis(Long keepaliveInMillis) {
        this.keepaliveInMillis = keepaliveInMillis;
        return this;
    }

    public <T> Cache<T> build() {
        return new Cache<T>(caseSensitiveKey, compressKey, autoReleaseInSeconds, keepaliveInMillis);
    }

    public static CacheBuilder newBuilder() {
        return new CacheBuilder();
    }
}
