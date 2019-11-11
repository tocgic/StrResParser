package kr.pe.tocgic.tools.strresparser.util;

public class Logger {

    /**
     * 호출한 메소드 정보 (클래스명.메소드명():라인넘버)
     *
     * @return
     */
    public static String getCallerInfo() {
        int depth = 2;
        String callerInfo = "";
        StackTraceElement[] elements = new Throwable().fillInStackTrace().getStackTrace();
        if (elements != null && elements.length > depth) {
            StackTraceElement el = elements[depth];
            String className = el.getClassName();
            className = className.substring(className.lastIndexOf('.') + 1);
            callerInfo = className + "." + el.getMethodName() + "():" + el.getLineNumber();
        }
        return callerInfo;
    }

    public static void v(String tag, String message) {
        Out.println("[" + tag + "] " + message);
    }

    public static void d(String tag, String message) {
        Out.println("[" + tag + "] " + message);
    }

    public static void i(String tag, String message) {
        Out.println(Out.ANSI_GREEN, "[" + tag + "] " + message);
    }

    public static void w(String tag, String message) {
        Out.println(Out.ANSI_PURPLE, "[" + tag + "] " + message);
    }

    public static void e(String tag, String message) {
        Out.println(Out.ANSI_RED, "[" + tag + "] " + message);
    }
}
