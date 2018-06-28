package kr.pe.tocgic.tools.file;

import kr.pe.tocgic.tools.StResManager;
import kr.pe.tocgic.tools.data.ResourceDataManager;
import kr.pe.tocgic.tools.data.enums.ExportXlsColumn;
import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.data.enums.Platform;
import kr.pe.tocgic.tools.file.platform.AndroidXml;
import kr.pe.tocgic.tools.file.platform.IOSStrings;
import kr.pe.tocgic.tools.util.Logger;
import org.junit.Test;

import java.io.File;
import java.util.Map;

public class TestFile {
    Map<String, String> androidXml(String fileFullPath) {
        AndroidXml xml = new AndroidXml();
        File source = new File(fileFullPath);
        Logger.i("TEST", "isSupport : " + xml.isSupportFileType(source));
        return xml.getKeyValueMap(source);
    }

    Map<String, String> iOSStrings(String fileFullPath) {
        IOSStrings strings = new IOSStrings();
        File source = new File(fileFullPath);
        Logger.i("TEST", "isSupport : " + strings.isSupportFileType(source));
        return strings.getKeyValueMap(source);
    }

    void printMap(Map<String, String> map) {
        if (map != null) {
            for (String key : map.keySet()) {
                Logger.i(key, map.get(key));
            }
        }
    }

    @Test
    public void test() {
        ResourceDataManager resourceDataManager = new ResourceDataManager();

        Map<String, String> map;
//        map = androidXml("/Users/tocgic/Documents/_Temp/agent-string-resources/Android/strings.xml");
//        printMap(map);
//        resourceModelList.addItems(Platform.ANDROID, Language.KO, map);
        map = iOSStrings("/Users/tocgic/Documents/_Temp/agent-string-resources/iOS/Localizable.strings");
        printMap(map);
        resourceDataManager.addItems(Platform.IOS, Language.KO, map);
//
//        resourceDataManager.sortByValue(Language.KO, true);
//
//        makeTotalStResXml(resourceDataManager, "/Users/tocgic/Documents/_Temp/agent-string-resources/totalStRes.xml");
    }

    @Test
    public void testStResManager() {
        StResManager manager = new StResManager();

        //import, platform string resources
        manager.setImportResourceDirectory(Language.EN, "/Users/tocgic/Temp/test/android/values/");
        manager.setImportResourceDirectory(Language.KO, "/Users/tocgic/Temp/test/android/values-ko/");
//        manager.setImportResourceDirectory(Language.JA, "/Users/tocgic/Temp/test/android/values-ja/");
        manager.importPlatformResources();

        //export, union string resource
        manager.makeUnionStResXml(new File("/Users/tocgic/Temp/unionStRes.xml"), true);
        manager.makeExcel(new File("/Users/tocgic/Temp/unionStRes.xlsx"), true, null, true);
        ExportXlsColumn[] columns = {ExportXlsColumn.LANGUAGE_KO, ExportXlsColumn.LANGUAGE_JA, ExportXlsColumn.LANGUAGE_EN};
        manager.makeExcel(new File("/Users/tocgic/Temp/unionStRes_onlyString.xlsx"), true, columns, true);
        ExportXlsColumn[] columns2 = {ExportXlsColumn.HIDDEN_KEYS, ExportXlsColumn.LANGUAGE_KO, ExportXlsColumn.LANGUAGE_JA, ExportXlsColumn.LANGUAGE_EN};
        manager.makeExcel(new File("/Users/tocgic/Temp/unionStRes_hiddenKeys.xlsx"), true, columns2, true);


//        //import, platform string resources
//        manager.setImportResourceDirectory(Language.KO, "/Users/tocgic/Temp/test/android/values-ko/");
//        manager.setImportResourceDirectory(Language.EN, "/Users/tocgic/Temp/test/android/values/");
//        manager.setImportResourceDirectory(Language.JA, "/Users/tocgic/Temp/test/android/values-ja/");
//        manager.importPlatformResources();
//
//        //export, union string resource
//        manager.makeUnionStResXml(new File("/Users/tocgic/Temp/test/unionStRes.xml"), true);
//        manager.makeExcel(new File("/Users/tocgic/Temp/test/unionStRes.xlsx"), true, null);
//        ExportXlsColumn[] columns = {ExportXlsColumn.LANGUAGE_KO, ExportXlsColumn.LANGUAGE_JA, ExportXlsColumn.LANGUAGE_EN};
//        manager.makeExcel(new File("/Users/tocgic/Temp/test/unionStResOnlyString.xlsx"), true, columns);
    }

    @Test
    public void testStResManagerUpdate() {
        StResManager manager = new StResManager();

        //import, platform string resources
        manager.setImportResourceDirectory(Language.EN, "/Users/tocgic/Temp/test/android/values/");
        manager.setImportResourceDirectory(Language.KO, "/Users/tocgic/Temp/test/android/values-ko/");
        manager.importPlatformResources();

        //update, string resource from xlsx file
        manager.importFromExcel(new File("/Users/tocgic/Temp/unionStRes_hiddenKeys_edit.xlsx"), true);

        //export, updated union string resource
        manager.makeUnionStResXml(new File("/Users/tocgic/Temp/unionStRes.xml"), true);
        manager.makeExcel(new File("/Users/tocgic/Temp/unionStRes.xlsx"), true, null, true);
        ExportXlsColumn[] columns = {ExportXlsColumn.LANGUAGE_KO, ExportXlsColumn.LANGUAGE_JA, ExportXlsColumn.LANGUAGE_EN};
        manager.makeExcel(new File("/Users/tocgic/Temp/unionStRes_onlyString.xlsx"), true, columns, true);
        ExportXlsColumn[] columns2 = {ExportXlsColumn.HIDDEN_KEYS, ExportXlsColumn.LANGUAGE_KO, ExportXlsColumn.LANGUAGE_JA, ExportXlsColumn.LANGUAGE_EN};
        manager.makeExcel(new File("/Users/tocgic/Temp/unionStRes_hiddenKeys.xlsx"), true, columns2, true);
    }
}
