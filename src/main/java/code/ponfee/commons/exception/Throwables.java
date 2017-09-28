package code.ponfee.commons.exception;

import code.ponfee.commons.io.StringPrintWriter;

/**
 * 异常工具类
 * @author Ponfee
 */
public final class Throwables {
    private Throwables() {}

    /**
     * get the throwable stack trace
     * @param e
     * @return
     */
    public static String getStackTrace(Throwable e) {
        if (e == null) return null;

        try (StringPrintWriter writer = new StringPrintWriter()) {
            e.printStackTrace(writer);
            return writer.getString();
        }
    }

    /**
     * ignore the throwable
     * @param ignored
     */
    public static void ignore(Throwable ignored) {
        ignore(ignored, false);
    }

    /**
     * ignore the throwable, if {@code console} is true then will be 
     * print the throwable stack trace to console
     * @param ignored
     * @param console
     */
    public static void ignore(Throwable ignored, boolean console) {
        if (console) {
            ignored.printStackTrace();
        }
    }

    /**
     * print the throwable stack trace to console
     * @param ignored
     */
    public static void console(Throwable ignored) {
        ignored.printStackTrace();
    }

    /**
     * get the root cause of throwable
     * @param throwable
     * @return
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable slowPointer = throwable;
        boolean advanceSlowPointer = false;

        Throwable cause;
        while ((cause = throwable.getCause()) != null) {
            throwable = cause;

            if (throwable == slowPointer) {
                throw new IllegalArgumentException("Loop in causal chain detected @ " + throwable);
            }
            if (advanceSlowPointer) {
                slowPointer = slowPointer.getCause();
            }
            advanceSlowPointer = !advanceSlowPointer; // only advance every other iteration
        }

        return throwable;
    }
}
