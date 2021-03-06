package code.ponfee.commons.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 变量持有，用于lambda方法体内
 * non-thread-safe
 * 
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

    /**
     * Returns the holder value whether null
     * 
     * @return a boolean, if {@code true} then the value is null
     */
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
        if (this.value == null && value != null) {
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

    public void ifPresent(Consumer<? super T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    public T get() {
        return this.value;
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    public T orElseGet(Supplier<T> other) {
        return value != null ? value : other.get();
    }

    public <E extends Throwable> T orElseThrow(
        Supplier<? extends E> exceptionSupplier) throws E {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Holder)) {
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
        return value != null
            ? String.format("Holder[%s]", value)
            : "Holder.empty";
    }
}
