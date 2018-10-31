package code.ponfee.commons.export;

import java.util.function.BiConsumer;

/**
 * 导出
 * @author fupf
 */
public abstract class AbstractExporter implements DataExporter {

    private static final int AWAIT_TIME_MILLIS = 31;

    private boolean empty = true;
    private String name; // non thread safe

    @Override
    public boolean isEmpty() {
        return empty;
    }

    public final void nonEmpty() {
        this.empty = false;
    }

    public final AbstractExporter setName(String name) {
        this.name = name;
        return this;
    }

    public final String getName() {
        return name;
    }

    protected final void rollingTbody(Table table,
        BiConsumer<Object[], Integer> action) {
        try {
            Object[] data;
            for (int i = 0; table.isNotEnd(); i++) {
                if ((data = table.poll()) != null) {
                    action.accept(data, i);
                } else {
                    Thread.sleep(AWAIT_TIME_MILLIS);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
