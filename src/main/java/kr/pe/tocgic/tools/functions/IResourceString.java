package kr.pe.tocgic.tools.functions;

import java.io.File;
import java.util.Map;

public interface IResourceString {
    /**
     * 파일 지원 여부
     * @param file
     * @return
     */
    boolean isSupportFileType(File file);

    /**
     * Key, Value 의 Map 데이터 반환
     * @return
     */
    Map<String, String> getKeyValueMap(File source);

    /**
     * 파일에 sourceMap 과 동일 한 Key 에 대해 value 값을 변경 하여 파일 생성
     * @param sourceMap
     * @return
     */
    boolean applyValue(Map<String, String> sourceMap);
}
