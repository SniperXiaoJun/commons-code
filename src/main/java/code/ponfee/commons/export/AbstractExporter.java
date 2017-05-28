package code.ponfee.commons.export;

/**
 * 导出
 * @author fupf
 */
public abstract class AbstractExporter implements DataExporter {

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

}
