package code.ponfee.commons.concurrent;

import java.util.List;

/**
 * 批量消费Runnable工厂类
 * @author fupf
 * @param <T>
 */
@FunctionalInterface
public interface RunnableFactory<T> {

    Runnable create(List<T> list, boolean isEnd);
}
