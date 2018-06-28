package kr.pe.tocgic.tools.data.enums;

public enum ExportXlsColumn {
    HIDDEN_KEYS("keys", 0, null),
    KEY("key", 50, null),
    PLATFORM("platform", 10, null),
    LANGUAGE_KO("ko", 50, Language.KO),
    LANGUAGE_JA("ja", 50, Language.JA),
    LANGUAGE_EN("en", 50, Language.EN);

    private final String value;
    private final int cellLength;
    private final Language language;

    ExportXlsColumn(String value, int cellLength, Language language) {
        this.value = value;
        this.cellLength = cellLength;
        this.language = language;
    }

    public String getValue() {
        return value;
    }

    public int getCellLength() {
        return cellLength;
    }

    public Language getLanguage() {
        return language;
    }

    public enum ParseType {
        HIDDEN_KEY,
        KEY,
        VALUE
    }
}
