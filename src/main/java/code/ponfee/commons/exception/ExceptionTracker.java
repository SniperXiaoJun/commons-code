package code.ponfee.commons.exception;

import code.ponfee.commons.io.StringPrintWriter;

/**
 * 异常追踪
 * @author fupf
 */
public final class ExceptionTracker {
    private ExceptionTracker() {}

    /**
     * 查看错误信息
     * @param e
     * @return
     */
    public static String peekStackTrace(Throwable e) {
        if (e == null) return null;

        try (StringPrintWriter writer = new StringPrintWriter()) {
            e.printStackTrace(writer);
            return writer.getString();
        }
    }

}
