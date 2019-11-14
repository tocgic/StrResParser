package kr.pe.tocgic.tools.data;

import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.data.enums.Platform;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ResourceModelList {
    private static final String TAG = ResourceModelList.class.getSimpleName();

    private List<ResourceModel> resourceModelList = new ArrayList<>();

    public void clear() {
        resourceModelList.clear();
    }

    /**
     * Platform & Language 에 대한, ResourceModel item 추가
     * @param platform platform 종류
     * @param language language 종류
     * @param key resource key
     * @param value resource value
     */
    public void addItem(Platform platform, Language language, String key, String value) {
        ResourceModel model = getItemByValue(language, value);
        if (model == null) {
            model = getItemByKey(platform, key);
            if (model == null) {
                resourceModelList.add(new ResourceModel(platform, language, key, value));
            } else {
                if (model.hasLanguage(language)) {
                    //이미 language 에 대한 값이 있다면, 언어별 동일 단어에 대한 다른 번역이 존재 하는 것이므로, 해당 key 는 새 ResourceModel 로 이관 해야 한다.

                    //key 에 대한 model 데이터 복사 (이미 존재 하는 language 는 새로운 language 값으로 변경)
                    ResourceModel newResourceModel = model.newValueInstance();
                    newResourceModel.setValue(language, value);
                    newResourceModel.addKey(platform, key);
                    resourceModelList.add(newResourceModel);

                    //기존 ResourceModel 의 key 제거
                    model.deleteKey(platform, key);
                } else {
                    model.setValue(language, value);
                }
            }
        } else {
            if (!model.hasKey(platform, key)) {
                ResourceModel modelByKey = getItemByKey(platform, key);
                if (modelByKey != null && !modelByKey.hasLanguage(language)) {
                    modelByKey.deleteKey(platform, key);
                }
                model.addKey(platform, key);
            } else {
                Logger.v(TAG, "KEY:" + key + "(" + platform + ") is already exists. (but, value is the same.)" );
            }
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
        return new ArrayList<>(resourceModelList);
    }

    /**
     * 정렬
     * @param language 정렬 기준 언어
     * @param isAscending 오름차순 여부
     */
    public void sortByValue(final Language language, final boolean isAscending) {
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
        ResourceModel item = getItemByValue(defaultLanguage, defaultLangValue);
        if (item != null) {
            item.setValue(language, langValue);
            return true;
        }
        return false;
    }

    private boolean contains(Language language, String value) {
        return getItemByValue(language, value) != null;
    }

    private ResourceModel getItemByValue(Language language, String value) {
        String val = value == null ? "" : value;
        for (ResourceModel model : resourceModelList) {
            if (val.equals(model.getValue(language))) {
                return model;
            }
        }
        return null;
    }

    private ResourceModel getItemByKey(Platform platform, String key) {
        String _key = key == null ? "" : key;
        for (ResourceModel model : resourceModelList) {
            if (model.hasKey(platform, _key)) {
                return model;
            }
        }
        return null;
    }

}
