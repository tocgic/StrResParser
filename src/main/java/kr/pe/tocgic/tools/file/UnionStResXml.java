package kr.pe.tocgic.tools.file;

import kr.pe.tocgic.tools.data.ResourceDataManager;
import kr.pe.tocgic.tools.data.ResourceModel;
import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.data.enums.Platform;
import kr.pe.tocgic.tools.functions.IResourceTransform;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.List;

/**
 * Total String Resource Xml
 * <?xml version="1.0" encoding="UTF-8"?>
 * <items>
 *     <item>
 *         <value language="ko">확인</value>
 *         <value language="en">OK</value>
 *         <value language="ja">확인</value>
 *         <key platform="android">btn_ok</key>
 *             ...
 *         <key platform="ios">확인</key>
 *             ...
 *         <key platform="server">button_ok</key>
 *             ...
 *     </item>
 * </items>
 */
public class UnionStResXml implements IResourceTransform {
    private static final String TAG = UnionStResXml.class.getSimpleName();

    private static final String TAG_ITEMS = "items";
    private static final String TAG_ITEM = "item";
    private static final String TAG_VALUE = "value";
    private static final String TAG_KEY = "key";
    private static final String ATTR_LANGUAGE = "language";
    private static final String ATTR_PLATFORM = "platform";

    @Override
    public boolean exportFile(ResourceDataManager source, File target) {
        if (target == null || target.isDirectory()) {
            return false;
        }
        if (source == null) {
            return false;
        }
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            document.setXmlStandalone(true);

            //create node <items>
            Node items = document.createElement(TAG_ITEMS);
            document.appendChild(items);

            List<ResourceModel> resourceModels = source.getCopyResourceModelList();

            //create node <item>
            for (ResourceModel model : resourceModels) {
                Element item = document.createElement(TAG_ITEM);
                items.appendChild(item);

                //create node <value>
                for (Language language : Language.values()) {
                    String value = model.getValue(language);
                    if (value != null) {
                        Element childItem = createSingleNode(document, TAG_VALUE, ATTR_LANGUAGE, language.getValue(), value);
                        if (childItem != null) {
                            item.appendChild(childItem);
                        }
                    }
                }

                //create node <key>
                for (Platform platform : Platform.values()) {
                    List<String> keys = model.getKeyList(platform);
                    for (String key : keys) {
                        Element childItem = createSingleNode(document, TAG_KEY, ATTR_PLATFORM, platform.toString(), key);
                        if (childItem != null) {
                            item.appendChild(childItem);
                        }
                    }
                }
            }

            // Document 저장
            DOMSource xmlDOM = new DOMSource(document);
            StreamResult xmlFile = new StreamResult(target);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlDOM, xmlFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, e.getMessage());
        }

        return false;
    }

    @Override
    public boolean importFile(File source, ResourceDataManager target) {
        return false;
    }

    /**
     * single node 생성
     * <{tagName} {attrName}="{attrValue}">{value}</{tagName}>
     * @param document
     * @param tagName {key, value}
     * @param attrName
     * @param attrValue
     * @param value
     * @return
     */
    private Element createSingleNode(Document document, String tagName, String attrName, String attrValue, String value) {
        Element element = null;
        if (value != null) {
            element = document.createElement(tagName);
            if (StringUtil.isNotEmpty(attrName) && StringUtil.isNotEmpty(attrValue)) {
                element.setAttribute(attrName, attrValue);
            }
            element.appendChild(document.createTextNode(value));
        }
        return element;
    }
}
