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
 * Android 문구 리소스 (*.xml)
 *
 * DOM 파서를 이용하여 xml 을 갱신할 경우 발생하는 단점 (value 이외의 값에 대한 변경사항 발생)
 * 을 해소 하기 위해, DOM 파서를 사용하지 않고, <string></string> tag 에 대해서만, 파일을 읽어 value 값 만을 갱신하도록 처리.
 *
 */
public class AndroidStringXml extends BaseStringResFile implements IResourceString {
    private static final String TAG_CLOSE = "/>";
    private static final String TAG_DOCTYPE_OPEN = "<!DOCTYPE";
    private static final String TAG_ENTITY_OPEN = "<!ENTITY";
    private static final String TAG_ENTITY_CLOSE = ">";
    private static final String TAG_COMMENT_OPEN = "<--";
    private static final String TAG_COMMENT_CLOSE = "-->";
    private static final String TAG_STRING_OPEN = "<string ";
    private static final String TAG_STRING_CLOSE = "</string>";

    class StringNode {
        String key;
        String value;

        StringNode(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "StringNode {" + key + ", " + value + "}";
        }
    }

    public AndroidStringXml() {
        String[] special_anp = {"&", "&amp;", "&#38;"};
//      String[] special_lt = {"<", "&lt;", "&#60;"};
//      String[] special_gt = {">", "&gt;", "&#62;"};
        String[] special_apos = {"'", "\\\\'"};
//      String[] special_quot = {"\"", "\\\\\""};
//      specials = new String[][]{special_anp, special_lt, special_gt, special_apos, special_quot};
        specials = new String[][]{special_anp, /*special_lt, special_gt, */special_apos/*, special_quot*/};
    }

    @Override
    public boolean isSupportFileType(File file) {
        if (isValidFile(file)) {
            return file.getAbsolutePath().toLowerCase().endsWith(".xml");
        }
        return false;
    }

    @Override
    public Map<String, String> getKeyValueMap(File source) {
//        <?xml version="1.0" encoding="UTF-8"?>
//        <resources xmlns:android="http://schemas.android.com/apk/res/android"
//            xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
//            <string name="debug_on_name_is_debug">IS_DEBUG</string>
//            <string name="debug_on_detail_is_debug">디버그 모드 사용여부</string>
//            <string name="debug_on_detail_is_debug_empty"/>
//        </resources>
        Map<String, String> map =  new HashMap<>();
        if (!isValidFile(source)) {
            return map;
        }
        entityMap.clear();
        entityMapRev.clear();
        Logger.i(TAG, ">> parsing source file : " + source.getAbsolutePath());
        BufferedReader bufferedReader = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(source);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            int value;
            StringBuilder buffer = new StringBuilder();
            boolean tagging = false;
            while ((value = bufferedReader.read()) > -1) {
                char ch = (char) value;
                buffer.append(ch);
                if (ch == '<') {
                    tagging = true;
                }
                if (tagging) {
                    if (ch == '>') {
                        String tagItem = buffer.toString();
                        if (tagItem.startsWith(TAG_COMMENT_OPEN)) {
                            if (tagItem.endsWith(TAG_COMMENT_CLOSE)) {
                                tagging = false;
                            }
                        } else if (tagItem.startsWith(TAG_DOCTYPE_OPEN)) {
                            if (tagItem.endsWith(TAG_CLOSE)) {
                                tagging = false;
                                parseDoctypeTag(tagItem);
                            }
                        } else if (tagItem.startsWith(TAG_STRING_OPEN)) {
                            if (tagItem.endsWith(TAG_STRING_CLOSE) || tagItem.endsWith(TAG_CLOSE)) {
                                tagging = false;
                                StringNode node = getStringNode(tagItem);
                                //Logger.v(TAG, "StringNode : " + node);
                                if (node != null) {
                                    map.put(node.key, replaceCommonExpression(true, node.value));
                                }
                            }
                        } else {
                            tagging = false;
                        }
                    }
                }
                if (!tagging) {
                    //clear buffer
                    buffer.setLength(0);
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

    /**
     * <string name="key" translatable="true">value</string>
     * @param tagItem
     * @return
     */
    private StringNode getStringNode(String tagItem) {
        if (StringUtil.isEmpty(tagItem)) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();

        //check translatable
        int index = tagItem.indexOf(" translatable");
        if (index > -1) {
            boolean translatable = true;
            getStringNode(tagItem, index, buffer);
            if (buffer.length() > 0) {
                translatable = Boolean.parseBoolean(buffer.toString());
            }
            if (!translatable) {
                return null;
            }
        }

        //get name
        String name = null;
        index = tagItem.indexOf(" name");
        if (index > -1) {
            getStringNode(tagItem, index, buffer);
            if (buffer.length() > 0) {
                name = buffer.toString();
            }
            if (StringUtil.isEmpty(name)) {
                return null;
            }
        }

        //get value
        String value = "";
        index = tagItem.indexOf(">") + 1;
        if (index > 0 && index < tagItem.length()) {
            int endIndex = tagItem.indexOf(TAG_STRING_CLOSE);
            if (endIndex > -1) {
                value = tagItem.substring(index, endIndex);
            }
        }

        return new StringNode(name, value);
    }

    @Override
    public boolean applyValue(Map<String, LanguageModel> sourceMap, Language language, File target) {
        if (!isValidFile(target)) {
            return false;
        }
        entityMap.clear();
        entityMapRev.clear();
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

            int value;
            StringBuilder buffer = new StringBuilder();
            boolean tagging = false;
            while ((value = bufferedReader.read()) > -1) {
                char ch = (char) value;
                buffer.append(ch);
                if (ch == '<') {
                    tagging = true;
                }
                if (tagging) {
                    if (ch == '>') {
                        String tagItem = buffer.toString();
                        if (tagItem.startsWith(TAG_COMMENT_OPEN)) {
                            if (tagItem.endsWith(TAG_COMMENT_CLOSE)) {
                                tagging = false;
                            }
                        } else if (tagItem.startsWith(TAG_DOCTYPE_OPEN)) {
                            if (tagItem.endsWith(TAG_CLOSE)) {
                                tagging = false;
                                parseDoctypeTag(tagItem);
                            }
                        } else if (tagItem.startsWith(TAG_STRING_OPEN)) {
                            if (tagItem.endsWith(TAG_STRING_CLOSE) || tagItem.endsWith(TAG_CLOSE)) {
                                tagging = false;
                                StringNode node = getStringNode(tagItem);
                                //Logger.v(TAG, "StringNode : " + node);
                                if (node != null) {
                                    LanguageModel languageModel = sourceMap.get(node.key);
                                    if (languageModel != null && languageModel.hasDifferentValue(language, replaceCommonExpression(true, node.value))) {
                                        String newValue = replaceCommonExpression(false, languageModel.getValue(language, null));
                                        Logger.v(TAG, "update [" + node.key + "] " + node.value + " >>>> " + newValue);

                                        int stringTagEndIndex = tagItem.indexOf(">");
                                        if (stringTagEndIndex > 0) {
                                            String keyString;
                                            if (tagItem.charAt(stringTagEndIndex - 1) == '/') {
                                                //값이 없는 string tag. ex) <string name="debug_on_detail_is_debug"/>
                                                keyString = tagItem.substring(0, stringTagEndIndex - 1) + ">";
                                            } else {
                                                keyString = tagItem.substring(0, stringTagEndIndex + 1);
                                            }
                                            buffer.setLength(0);
                                            buffer.append(keyString);
                                            buffer.append(newValue);
                                            buffer.append(TAG_STRING_CLOSE);
                                        }
                                    }
                                }
                            }
                        } else {
                            tagging = false;
                        }
                    }
                }
                if (!tagging) {
                    bufferedWriter.write(buffer.toString());
                    bufferedWriter.flush();
                    //clear buffer
                    buffer.setLength(0);
                }
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

    private void parseDoctypeTag(String source) {
        BufferedReader bufferedReader = null;
        try {
            source = source.replaceFirst(TAG_DOCTYPE_OPEN, "");
            bufferedReader = new BufferedReader(new StringReader(source));
            int value;
            StringBuilder buffer = new StringBuilder();
            boolean tagging = false;
            while ((value = bufferedReader.read()) > -1) {
                char ch = (char) value;
                buffer.append(ch);
                if (ch == '<') {
                    tagging = true;
                }
                if (tagging) {
                    if (ch == '>') {
                        String tagItem = buffer.toString();
                        if (tagItem.startsWith(TAG_ENTITY_OPEN)) {
                            if (tagItem.endsWith(TAG_ENTITY_CLOSE)) {
                                tagging = false;
                                addEntityItem(tagItem);
                            }
                        } else {
                            tagging = false;
                        }
                    }
                }
                if (!tagging) {
                    //clear buffer
                    buffer.setLength(0);
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
    }

    private void addEntityItem(String tagItem) {
        //<!ENTITY key "value">
        String body = tagItem.replaceFirst(TAG_ENTITY_OPEN, "").trim();
        String[] items = body.split(" ");
        if (items.length == 2) {
            String key = items[0];
            StringBuilder stringBuilder = new StringBuilder();
            getStringNode(items[1], 0, stringBuilder);
            entityMap.put(key, stringBuilder.toString());
            entityMapRev.put(stringBuilder.toString(), key);
        }
    }
}
