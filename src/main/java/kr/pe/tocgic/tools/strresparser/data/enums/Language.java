package kr.pe.tocgic.tools.strresparser.data.enums;

public enum Language {
    KO("ko", true),
    EN("en", false),
    JA("ja", false);

    private final String value;
    private final boolean isDefault;

    Language(String value, boolean isDefault) {
        this.value = value;
        this.isDefault = isDefault;
    }

    public String getValue() {
        return value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public static Language getDefaultLanguage() {
        for (Language language : Language.values()) {
            if (language.isDefault()) {
                return language;
            }
        }
        return Language.KO;
    }

}
