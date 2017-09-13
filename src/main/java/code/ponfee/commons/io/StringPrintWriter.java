package code.ponfee.commons.io;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 字符串输出类
 * @author Ponfee
 */
public class StringPrintWriter extends PrintWriter {
    public StringPrintWriter() {
        super(new StringWriter());
    }

    public StringPrintWriter(int initialSize) {
        super(new StringWriter(initialSize));
    }

    public String getString() {
        flush();
        return this.out.toString();
    }

    @Override
    public String toString() {
        return getString();
    }
}
