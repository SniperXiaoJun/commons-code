package code.ponfee.commons.concurrent;

import java.util.List;

@FunctionalInterface
public interface RunnableFactory<T> {

    Runnable create(List<T> list, boolean isEnd);
}
