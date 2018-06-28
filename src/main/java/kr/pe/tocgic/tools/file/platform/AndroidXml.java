package kr.pe.tocgic.tools.file.platform;

import com.sun.org.apache.xpath.internal.operations.Bool;
import kr.pe.tocgic.tools.functions.IResourceString;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Android 문구 리소스 (*.xml)
 *
 */
public class AndroidXml extends BaseStringResFile implements IResourceString {
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
    public boolean applyValue(Map<String, String> sourceMap) {
        return false;
    }
}
