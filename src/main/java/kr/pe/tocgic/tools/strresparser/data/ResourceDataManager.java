package kr.pe.tocgic.tools.strresparser.data;

import kr.pe.tocgic.tools.strresparser.data.enums.Language;
import kr.pe.tocgic.tools.strresparser.data.enums.Platform;
import kr.pe.tocgic.tools.strresparser.util.Logger;
import kr.pe.tocgic.tools.strresparser.util.StringUtil;

import java.util.*;

public class ResourceDataManager {
    private static final String TAG = ResourceDataManager.class.getSimpleName();

    private Map<Platform, Map<String, LanguageModel>> platformMapMap = new HashMap<>();

    public void clear() {
        for (Platform platform : platformMapMap.keySet()) {
            Map<String, LanguageModel> map = getLanguageMap(platform);
            map.clear();
        }
        platformMapMap.clear();
    }

    public Map<String, LanguageModel> getLanguageMap(Platform platform) {
        Map<String, LanguageModel> map = platformMapMap.get(platform);
        if (map == null) {
            map = new HashMap<>();
            platformMapMap.put(platform, map);
        }
        return map;
    }

    private LanguageModel getLanguageModel(Platform platform, String key) {
        Map<String, LanguageModel> languageMap = getLanguageMap(platform);
        LanguageModel model = languageMap.get(key);
        if (model == null) {
            model = new LanguageModel();
            languageMap.put(key, model);
        }
        return model;
    }

    /**
     * Platform & Language 에 대한, ResourceModel item 추가
     * @param platform platform 종류
     * @param language language 종류
     * @param key resource key
     * @param value resource value
     */
    public void addItem(Platform platform, Language language, String key, String value) {
        LanguageModel model = getLanguageModel(platform, key);
        if (!model.setValue(language, value)) {
            Logger.w(TAG, "Fail, addItem(), key : " + key+ ", [" + language + "] value : " + value);
        }
    }

    /**
     * Platform & Language 에 대한, ResourceModel items 추가
     * @param platform platform 종류
     * @param language language 종류
     * @param map resource key & value map
     */
    public void addItems(Platform platform, Language language, Map<String, String> map) {
        if (map == null || map.size() < 1) {
            return;
        }
        for (String key : map.keySet()) {
            String value = map.get(key);
            addItem(platform, language, key, value);
        }
    }

    /**
     * ResourceModel list 에 대한 사본
     * @return copied list
     */
    public List<ResourceModel> getCopyResourceModelList() {
        List<ResourceModel> resourceModelList = new ArrayList<>();
        merge(resourceModelList);
        sortByValue(resourceModelList, Language.KO, true);
        return resourceModelList;
    }

    /**
     * defaultLanguage 에 해당하는 defaultLangValue 가 동일한 item 의 lanValue 를 갱신한다.
     * 단, defaultLangValue 가 '' 인 경우는 skip 한다.
     *
     * @param defaultLanguage 기본 Language
     * @param defaultLangValue 기본 Language 에 대한 String
     * @param language Language
     * @param langValue Language 에 대한 String
     * @return 성공 여부
     */
    public boolean updateValue(Language defaultLanguage, String defaultLangValue, Language language, String langValue) {
        if (StringUtil.isEmpty(defaultLangValue)) {
            Logger.w(TAG, "SKIP, updateValue(), defaultLangValue : ''("+defaultLanguage+"). langValue : "+langValue+"("+langValue+")");
            return false;
        }
        List<LanguageModel> list = getLanguageModesByValue(defaultLanguage, defaultLangValue);
        if (list != null && list.size() > 0) {
            for (LanguageModel model : list) {
                model.setValue(language, langValue, true);
            }
            return true;
        }
        return false;
    }

    public boolean updateByKeyPlatform(Platform platform, String key, LanguageModel languageModel) {
        LanguageModel stored = getLanguageModel(platform, key);
        for (Language language : languageModel.getLanguages()) {
            String value = languageModel.getValue(language, null);
            if (value != null) {
                stored.setValue(language, value, true);
            }
        }
        return true;
    }

    private List<LanguageModel> getLanguageModesByValue(Language language, String value) {
        List<LanguageModel> target = new ArrayList<>();
        for (Platform platform : platformMapMap.keySet()) {
            Map<String, LanguageModel> languageModelMap = getLanguageMap(platform);
            for (String key : languageModelMap.keySet()) {
                LanguageModel languageModel = getLanguageModel(platform, key);
                if (languageModel != null) {
                     String storedValue = languageModel.getValue(language, "");
                     if (storedValue.equals(value)) {
                         target.add(languageModel);
                     }
                }
            }
        }
        return target;
    }

    private void merge(List<ResourceModel> resourceModelList) {
        if (resourceModelList == null) {
            return;
        }
        resourceModelList.clear();

        ResourceMerger merger = new ResourceMerger(resourceModelList);
        merger.merge();
    }

    /**
     * 정렬
     * @param language 정렬 기준 언어
     * @param isAscending 오름차순 여부
     */
    private void sortByValue(List<ResourceModel> resourceModelList, final Language language, final boolean isAscending) {
        Collections.sort(resourceModelList, (lhs, rhs) -> {
            String left = lhs != null ? lhs.getValue(language) : null;
            if (left == null)
                left = "";
            String right = rhs != null ? rhs.getValue(language) : null;
            if (right == null)
                right = "";
            int sort = isAscending ? 1 : -1;
            return sort * left.compareTo(right);
        });
    }

    class ResourceMerger {
        private List<ResourceModel> target;

        ResourceMerger(List<ResourceModel> target) {
            this.target = target;
        }

        void merge() {
            for (Platform platform : platformMapMap.keySet()) {
                Map<String, LanguageModel> languageModelMap = getLanguageMap(platform);
                for (String key : languageModelMap.keySet()) {
                    LanguageModel languageModel = getLanguageModel(platform, key);
                    ResourceModel item = getItemByLanguage(languageModel);
                    if (item == null) {
                        addItem(platform, key, languageModel);
                    } else {
                        item.addKey(platform, key);
                    }
                }
            }
        }

        private void addItem(Platform platform, String key, LanguageModel languageModel) {
            target.add(new ResourceModel(platform, key, languageModel));
        }

        private ResourceModel getItemByLanguage(LanguageModel languageModel) {
            for (ResourceModel model : target) {
                if (model.getLanguageModel().equals(languageModel)) {
                    return model;
                }
            }
            return null;
        }
    }
}
