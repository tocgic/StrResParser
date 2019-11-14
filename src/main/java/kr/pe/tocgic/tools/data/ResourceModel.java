package kr.pe.tocgic.tools.data;

import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.data.enums.Platform;
import kr.pe.tocgic.tools.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * keyMap keys {iOS, Android, Server ...}
 * valueMap keys {ko, en, ja ...}
 */
public class ResourceModel {
    private static final String TAG = ResourceModel.class.getSimpleName();

    Map<Platform, List<String>> keyMap = new HashMap<>();
    Map<Language, String> valueMap = new HashMap<>();

    private ResourceModel() {}

    public ResourceModel(Language language, String value) {
        setValue(language, value);
    }

    public ResourceModel(Platform platform, Language language, String key, String value) {
        this(language, value);
        addKey(platform, key);
    }

    public List<String> getKeyList(Platform platform) {
        List<String> keys = keyMap.get(platform);
        if (keys == null) {
            keys = new ArrayList<>();
            keyMap.put(platform, keys);
        }
        return keys;
    }

    public void addKey(Platform platform, String key) {
        List<String> keys = getKeyList(platform);
        if (!keys.contains(key)) {
            keys.add(key);
        } else {
            Logger.w(TAG, "already exist key [" + key + "]");
        }
    }

    public boolean deleteKey(Platform platform, String key) {
        List<String> keys = getKeyList(platform);
        return keys.remove(key);
    }

    public boolean hasKey(Platform platform, String key) {
        List<String> keys = getKeyList(platform);
        return keys.contains(key);
    }

    public void setValue(Language language, String value) {
        valueMap.put(language, value);
    }

    public String getValue(Language language) {
        return valueMap.get(language);
    }

    public boolean hasLanguage(Language language) {
        return valueMap.containsKey(language);
    }

    public ResourceModel newValueInstance() {
        ResourceModel newInstance = new ResourceModel();
        for (Language language : valueMap.keySet()) {
            if (valueMap.containsKey(language)) {
                newInstance.setValue(language, valueMap.get(language));
            }
        }
        return newInstance;
    }
}
