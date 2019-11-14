package kr.pe.tocgic.tools.data.enums;

public enum Platform {
    ANDROID("android"),
    IOS("iOS"),
    SERVER("server");

    private final String value;
    Platform(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
