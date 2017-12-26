package code.ponfee.commons.util;

/**
 * 上下文持有，用于lambda方法体内
 * @author Ponfee
 * @param <T>
 */
public final class ContextHolder<T> {

    private T value;

    private ContextHolder(T value) {
        this.value = value;
    }

    public static <T> ContextHolder<T> empty() {
        return new ContextHolder<>(null);
    }

    public static <T> ContextHolder<T> of(T t) {
        return new ContextHolder<>(t);
    }

    public boolean isEmpty() {
        return value == null;
    }

    public void set(T value) {
        this.value = value;
    }

    public T getAndSet(T value) {
        T t = this.value;
        this.value = value;
        return t;
    }

    public T get() {
        return this.value;
    }

}
