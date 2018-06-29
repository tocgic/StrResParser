package kr.pe.tocgic.tools.data;

import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        return setValue(language, value, false);
    }

    public boolean setValue(Language language, String value, boolean isForce) {
        String newValue = value != null ? value : "";
        String oldValue = getValue(language);
        if (oldValue == null || isForce) {
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

    public Set<Language> getLanguages() {
        return valueMap.keySet();
    }

    public boolean isEmpty() {
        if (valueMap.size() < 1) {
            return true;
        }
        for (Language language : valueMap.keySet()) {
            if (StringUtil.isNotEmpty(valueMap.get(language))) {
                return false;
            }
        }
        return true;
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

    public boolean hasDifferentValue(Language language, String source) {
        String stored = getValue(language, null);
        if (stored != null && !stored.equals(source)) {
            return true;
        }
        return false;
    }
}
