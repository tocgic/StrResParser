package kr.pe.tocgic.tools.util;

public class Out implements IAnsiColorCode {
    public static final void println(String string) {
        System.out.println(string);
    }

    public static final void println(String ansiColorCode, String string) {
        System.out.println(ansiColorCode + string + ANSI_RESET);
    }
}