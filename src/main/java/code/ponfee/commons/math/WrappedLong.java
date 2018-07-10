package code.ponfee.commons.math;

/**
 * 包装long，用于lamda方法体内计算
 * 
 * @author Ponfee
 */
public class WrappedLong {

    private Long value;

    public WrappedLong() {}

    public WrappedLong(Long value) {
        this.value = value;
    }

    public synchronized void setValue(Long value) {
        this.value = value;
    }

    public synchronized void add(long number) {
        this.value += number;
    }

    public synchronized void minus(long number) {
        this.value -= number;
    }

    public synchronized Long getValue() {
        return value;
    }

}
