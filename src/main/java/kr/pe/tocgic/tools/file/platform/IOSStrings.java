package kr.pe.tocgic.tools.file.platform;

import kr.pe.tocgic.tools.data.LanguageModel;
import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.functions.IResourceString;
import kr.pe.tocgic.tools.util.FileUtil;
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

    public IOSStrings() {
        NODE_TOKEN = '"';
    }

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
        BufferedReader bufferedReader = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(source);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (isSkipLine(line)) {
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
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Logger.i(TAG, ">> parsed key map size : " + map.size());
        return map;
    }

    @Override
    public boolean applyValue(Map<String, LanguageModel> sourceMap, Language language, File target) {
        if (!isValidFile(target)) {
            return false;
        }
        boolean result = false;
        Logger.i(TAG, ">> parsing target file : " + target.getAbsolutePath());

        File temp = new File(target.getAbsolutePath() + "." + System.currentTimeMillis() + ".tmp");
        if (temp.exists()) {
            temp.delete();
        }

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(target);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            FileWriter fileWriter = new FileWriter(temp);
            bufferedWriter = new BufferedWriter(fileWriter);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (isSkipLine(line)) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }
                String key, value;
                StringBuilder item = new StringBuilder();
                int index = getStringNode(line, 0, item);
                if (index > 0) {
                    key = item.toString();
                    item.delete(0, item.length());
                    int startIndex = index;
                    index = getStringNode(line, startIndex, item);
                    if (StringUtil.isNotEmpty(key) && index > startIndex) {
                        value = item.toString();

                        LanguageModel languageModel = sourceMap.get(key);
                        if (languageModel != null && languageModel.hasDifferentValue(language, value)) {
                            String newValue = languageModel.getValue(language, null);
                            Logger.v(TAG, "update [" + key + "] " + value + " >>>> " + newValue);

                            StringBuilder newLine = new StringBuilder();
                            newLine.append(line.substring(0, startIndex));
                            String valueLine = line.substring(startIndex, line.length());
                            newLine.append(valueLine.replace(value, newValue));

                            line = newLine.toString();
                        }

                    }
                }
                bufferedWriter.write(line);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            result = true;
        } catch (Exception e) {
            Logger.w(TAG, e.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (result) {
            result = FileUtil.renameFile(temp, target, true);
        }
        Logger.i(TAG, ">> applyValue() result : " + result);
        return result;
    }

    @Override
    protected boolean isSkipLine(String line) {
        if (!super.isSkipLine(line)) {
            return line.startsWith("/*");
        }
        return false;
    }
}
