package kr.pe.tocgic.tools.file.platform;

import kr.pe.tocgic.tools.data.LanguageModel;
import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.functions.IResourceString;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Android 문구 리소스 (*.xml)
 *
 * DOM 파서를 이용하여, Android Resource xml 파일을 파싱한다.
 * DOM 파서를 이용하기 때문에, 기존 xml 의 문구 (value) 를 갱신하는 경우,
 * 원본 xml 과 줄간격, Tag 정렬 등 여러가지 변경 사항이 발생 할 수 있다.
 *
 */
public class AndroidDomXml extends BaseStringResFile implements IResourceString {
    private static final String TAG_RESOURCES = "resources";
    private static final String TAG_STRING = "string";

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
//        </resources>
        Map<String, String> map =  new HashMap<>();
        if (!isValidFile(source)) {
            return map;
        }
        Logger.i(TAG, ">> parsing source file : " + source.getAbsolutePath());
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(source);
            doc.getDocumentElement().normalize();

            NodeList resourcesNodeList = doc.getElementsByTagName(TAG_RESOURCES);
            int nodeSize = resourcesNodeList.getLength();
            if (nodeSize > 0) {
                Element resourcesElement = (Element) resourcesNodeList.item(0);
                NodeList stringNodeList = resourcesElement.getElementsByTagName(TAG_STRING);
                int stringNodeLength = stringNodeList.getLength();
                for (int i = 0; i < stringNodeLength; i++) {
                    Node stringNode = stringNodeList.item(i);
                    Element stringElement = (Element) stringNode;
                    String key = stringElement.getAttribute("name");
                    String translatable = stringElement.getAttribute("translatable");
                    if (StringUtil.isNotEmpty(translatable)) {
                        if (!Boolean.parseBoolean(translatable)) {
                            continue;
                        }
                    }
                    String value = stringNode.getTextContent();
                    map.put(key, value);
                }
            }
        } catch (Exception e) {
            Logger.w(TAG, e.getMessage());
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
        boolean isChanged = false;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(target);
            doc.getDocumentElement().normalize();
            doc.setXmlStandalone(true);

            NodeList resourcesNodeList = doc.getElementsByTagName(TAG_RESOURCES);
            int nodeSize = resourcesNodeList.getLength();
            if (nodeSize > 0) {
                Element resourcesElement = (Element) resourcesNodeList.item(0);
                NodeList stringNodeList = resourcesElement.getElementsByTagName(TAG_STRING);
                int stringNodeLength = stringNodeList.getLength();
                for (int i = 0; i < stringNodeLength; i++) {
                    Node stringNode = stringNodeList.item(i);
                    Element stringElement = (Element) stringNode;
                    String key = stringElement.getAttribute("name");
                    String translatable = stringElement.getAttribute("translatable");
                    if (StringUtil.isNotEmpty(translatable)) {
                        if (!Boolean.parseBoolean(translatable)) {
                            continue;
                        }
                    }
                    String value = stringNode.getTextContent();
                    LanguageModel languageModel = sourceMap.get(key);
                    if (languageModel != null && languageModel.hasDifferentValue(language, value)) {
                        String newValue = languageModel.getValue(language, null);
                        Logger.v(TAG, "update [" + key + "] " + value + " >>>> " + newValue);
                        stringNode.setTextContent(newValue);
                        isChanged = true;
                    }
                }
            }

            if (isChanged) {
                // Document 저장
                DOMSource xmlDOM = new DOMSource(doc);
                StreamResult xmlFile = new StreamResult(target);
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(xmlDOM, xmlFile);
            }

            result = true;
        } catch (Exception e) {
            Logger.w(TAG, e.getMessage());
        }
        Logger.i(TAG, ">> applyValue() result : " + result);
        return result;
    }
}
