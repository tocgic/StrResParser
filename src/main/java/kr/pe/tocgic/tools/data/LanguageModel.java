package kr.pe.tocgic.tools.data;

import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class LanguageModel {
    private static final String TAG = LanguageModel.class.getSimpleName();

    Map<Language, String> valueMap = new HashMap<>();

    public String getValue(Language language) {
        return valueMap.get(language);
    }

    public String getValue(Language language, String defaultValue) {
        String value = getValue(language);
        return value == null ? defaultValue : value;
    }

    public boolean setValue(Language language, String value) {
        String newValue = value != null ? value : "";
        String oldValue = getValue(language);
        if (oldValue == null) {
            valueMap.put(language, newValue);
            return true;
        } else {
            if (newValue.equals(oldValue)) {
                Logger.v(TAG, "setValue(), already same value stored. [" + language + "] value : " + newValue);
                return true;
            } else {
                Logger.w(TAG, "setValue(), already stored. [" + language + "] value : " + newValue + ", oldValue : " + oldValue);
            }
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof LanguageModel) {
            LanguageModel other = (LanguageModel) obj;
            for (Language language : Language.values()) {
                if (!getValue(language, "").equals(other.getValue(language, ""))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
