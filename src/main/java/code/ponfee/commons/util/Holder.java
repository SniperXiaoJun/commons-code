package code.ponfee.commons.util;

import java.util.Objects;
import java.util.Optional;

/**
 * 变量持有，用于lambda方法体内
 * @author Ponfee
 * @param <T>
 */
public final class Holder<T> {

    private T value;

    private Holder(T value) {
        this.value = value;
    }

    public static <T> Holder<T> empty() {
        return new Holder<>(null);
    }

    public static <T> Holder<T> of(T t) {
        return new Holder<>(t);
    }

    public boolean isEmpty() {
        return value == null;
    }

    /**
     * add a value, 
     * if former value is null then add success and return true
     * if former value is not null then add fail and return false
     * 
     * @param value
     * @return {@code true} add success, {@code false} add fail
     */
    public boolean add(T value) {
        if (this.value == null) {
            this.value = value;
            return true;
        }
        return false;
    }

    /**
     * replace value if former value is not null
     * and return former value
     * 
     * @param value the newly value
     * @return then former value
     */
    public T replace(T value) {
        T t = this.value;
        if (this.value != null) {
            this.value = value;
        }
        return t;
    }

    /**
     * set value and return former value
     * 
     * @param value the newly value
     * @return  then former value
     */
    public T set(T value) {
        T t = this.value;
        this.value = value;
        return t;
    }

    public T get() {
        return this.value;
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Optional)) {
            return false;
        }

        Holder<?> other = (Holder<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return String.format("Holder[%s]", Objects.toString(value, "null"));
    }
}
