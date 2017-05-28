package code.ponfee.commons.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常追踪
 * @author fupf
 */
public final class ExceptionTracker {
    private ExceptionTracker() {}

    public static String peekStackTrace(Throwable e) {
        if (e == null) return null;

        StringPrintWriter writer = new StringPrintWriter();
        try {
            e.printStackTrace(writer);
            return writer.getString();
        } finally {
            writer.close();
        }
    }

    private static final class StringPrintWriter extends PrintWriter {
        StringPrintWriter() {
            super(new StringWriter());
        }

        StringPrintWriter(int initialSize) {
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

}
