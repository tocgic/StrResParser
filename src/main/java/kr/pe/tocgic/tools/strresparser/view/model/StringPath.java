package kr.pe.tocgic.tools.strresparser.view.model;

import kr.pe.tocgic.tools.strresparser.data.enums.Language;

public class StringPath {
    Language language;
    String path;

    public StringPath(Language language, String path) {
        this.language = language;
        this.path = path;
    }

    public Language getLanguage() {
        return language;
    }

    public String getPath() {
        return path;
    }

    public String getLanguageValue() {
        return language.getValue();
    }

    @Override
    public String toString() {
        return "Lan:" + getLanguageValue() + "\t" + getPath();
    }
}
