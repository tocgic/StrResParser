package kr.pe.tocgic.tools.file.platform;

import kr.pe.tocgic.tools.functions.IResourceString;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * iOS 문구 리소스 (*.strings)
 *
 */
public class IOSStrings extends BaseStringResFile implements IResourceString {

    @Override
    public boolean isSupportFileType(File file) {
        if (isValidFile(file)) {
            return file.getAbsolutePath().toLowerCase().endsWith(".strings");
        }
        return false;
    }

    @Override
    public Map<String, String> getKeyValueMap(File source) {
//      /* No comment provided by engineer. */
//      "'퇴장' 후 삭제요청이 가능합니다.\n\n방문 기업으로부터 GPS 위치 기반 거리를\n측정하여 일정 거리 이상 멀어진 경우,\n확인을 선택하시면\n차단되었던 기능이 해제됩니다." = "'퇴장' 후 삭제요청이 가능합니다.\n\n방문 기업으로부터 GPS 위치 기반 거리를\n측정하여 일정 거리 이상 멀어진 경우,\n확인을 선택하시면\n차단되었던 기능이 해제됩니다.";
//
//      /* No comment provided by engineer. */
//      "'OTP 번호 생성'\n버튼을 클릭해주세요" = "'OTP 번호 생성'\n버튼을 클릭해주세요";
        Map<String, String> map =  new HashMap<>();
        if (!isValidFile(source)) {
            return map;
        }
        Logger.i(TAG, ">> parsing source file : " + source.getAbsolutePath());
        try {
            FileInputStream fileInputStream = new FileInputStream(source);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (StringUtil.isNull(line)) {
                    continue;
                }
                if (line.startsWith("/*")) {
                    continue;
                }
                String key = null, value = null;
                StringBuilder item = new StringBuilder();
                int index = getStringNode(line, 0, item);
                if (index > 0) {
                    key = item.toString();
                    item.delete(0, item.length());
                    int startIndex = index;
                    index = getStringNode(line, startIndex, item);
                    if (index > startIndex) {
                        value = item.toString();
                    }
                }
                if (StringUtil.isNotEmpty(key)) {
                    map.put(key, value);
                } else {
                    Logger.w(TAG, "fail parse : " + line);
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, e.getMessage());
        }
        Logger.i(TAG, ">> parsed key map size : " + map.size());
        return map;
    }

    private int getStringNode(String line, int startIndex, StringBuilder target) {
        if (StringUtil.isNull(line)) {
            return startIndex;
        }
        if (target == null) {
            return startIndex;
        }
        char preChar = 0;
        boolean isStart = false;
        int index = startIndex;
        int lineLength = line.length();
        while (index < lineLength) {
            char ch = line.charAt(index++);
            if (ch == '"') {
                if (!isStart) {
                    isStart = true;
                    continue;
                } else {
                    if (preChar != '\\') {
                        break;
                    }
                }
            }
            if (isStart) {
                target.append(ch);
            }
            preChar = ch;
        }
        return index;
    }

    @Override
    public boolean applyValue(Map<String, String> sourceMap) {
        return false;
    }
}
