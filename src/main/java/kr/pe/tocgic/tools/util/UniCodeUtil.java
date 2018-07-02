package kr.pe.tocgic.tools.util;

public class UniCodeUtil {
    public static String decode(String unicode) {
        StringBuilder str = new StringBuilder();

        char ch = 0;
        for (int i = unicode.indexOf("\\u"); i > -1; i = unicode.indexOf("\\u")) {
            ch = (char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16);
            str.append(unicode.substring(0, i));
            str.append(String.valueOf(ch));
            unicode = unicode.substring(i + 6);
        }
        str.append(unicode);
        return str.toString();
    }

    public static String encode(String unicode) {
        StringBuilder str = new StringBuilder();

        for (int i = 0; i < unicode.length(); i++) {
            if (((int) unicode.charAt(i) == 32)) {
                str.append(" ");
                continue;
            }
            str.append("\\u");
            str.append(Integer.toHexString((int) unicode.charAt(i)));
        }
        return str.toString();
    }
}
