package code.ponfee.commons.concurrent;

import java.util.List;

/**
 * 批量消费Runnable工厂类
 * @author fupf
 * @param <T>
 */
@FunctionalInterface
public interface RunnableFactory<T> {

    /**
     * Create a runnable Consumer
     *
     * @param list   the batch consume messages
     * @param isEnd  inform whether is the end batch consume
     * @return a runnable Consumer
     */
    Runnable create(List<T> list, boolean isEnd);
}
