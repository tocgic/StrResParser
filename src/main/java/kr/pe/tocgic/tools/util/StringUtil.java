package kr.pe.tocgic.tools.util;

public class StringUtil {
    private static final String TAG = StringUtil.class.getSimpleName();

    /**
     * Null check​
     * @param str
     * @return
     */
    public static boolean isNull(String str) {
        return str == null || str.length() < 1 || "null".equalsIgnoreCase(str);
    }

    /**
     * check isNotEmpty string
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return str != null && str.length() > 0;
    }

    /**
     * check isEmpty string
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() < 1;
    }

    /**
     * int 값 반환
     * @param strInteger
     * @param defaultValue
     * @return
     */
    public static int toInt(String strInteger, int defaultValue) {
        int integer = defaultValue;
        try {
            if (strInteger != null) {
                integer = Integer.parseInt(strInteger);
            }
        } catch (Exception e) {
            Logger.v(TAG, Logger.getCallerInfo() + " toInt(" + strInteger + "):" + e.getMessage());
        }
        return integer;
    }

    /**
     * int 값 반환
     * @param strInteger
     * @return
     */
    public static int toInt(String strInteger) {
        return toInt(strInteger, 0);
    }

    /**
     * boolean 값 반환
     * @param strBoolean
     * @param defaultValue
     * @return
     */
    public static boolean toBoolean(String strBoolean, boolean defaultValue) {
        boolean value = defaultValue;
        try {
            if (strBoolean != null) {
                value = Boolean.parseBoolean(strBoolean);
            }
        } catch (Exception e) {
            Logger.v(TAG, Logger.getCallerInfo() + " toBoolean(" + strBoolean + "):" + e.getMessage());
        }
        return value;
    }

    /**
     * boolean 값 반환
     * @param strBoolean
     * @return
     */
    public static boolean toBoolean(String strBoolean) {
        return toBoolean(strBoolean, false);
    }

    /**
     * Wrapping substring
     * @param message
     * @param start
     * @param defaultValue
     * @return
     */
    public static String substring(String message, int start, String defaultValue) {
        String string;
        try {
            string = message.substring(start);
        } catch (Exception e) {
            string = defaultValue;
            Logger.e(TAG, "substring(\"" + message + "\", " + start + ", \"" + defaultValue + "\"), " + Logger.getCallerInfo());
        }
        return string;
    }

    /**
     * Wrapping substring
     * @param message
     * @param start
     * @param end
     * @param defaultValue
     * @return
     */
    public static String substring(String message, int start, int end, String defaultValue) {
        String string;
        try {
            string = message.substring(start, end);
        } catch (Exception e) {
            string = defaultValue;
            Logger.e(TAG, "substring(\"" + message + "\", " + start + ", " + end + ", \"" + defaultValue + "\"), " + Logger.getCallerInfo());
        }
        return string;
    }
}
