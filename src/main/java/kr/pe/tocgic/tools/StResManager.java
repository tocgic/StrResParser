package kr.pe.tocgic.tools;

import kr.pe.tocgic.tools.data.ResourceModelList;
import kr.pe.tocgic.tools.data.enums.ExportXlsColumn;
import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.data.enums.Platform;
import kr.pe.tocgic.tools.file.UnionStResExcel;
import kr.pe.tocgic.tools.file.UnionStResXml;
import kr.pe.tocgic.tools.file.platform.AndroidXml;
import kr.pe.tocgic.tools.file.platform.IOSStrings;
import kr.pe.tocgic.tools.functions.IResourceString;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StResManager {
    private static final String TAG = StResManager.class.getSimpleName();

    private List<SourceDirInfo> sourceDirInfoList;
    private IResourceString[] resourceFileParser;
    private ResourceModelList resourceModelList;

    class SourceDirInfo {
        Language language;
        File dir;

        SourceDirInfo(Language language, File dir) {
            this.language = language;
            this.dir = dir;
        }
    }

    public StResManager() {
        sourceDirInfoList = new ArrayList<>();
        resourceFileParser = new IResourceString[Platform.values().length];
        resourceFileParser[Platform.ANDROID.ordinal()] = new AndroidXml();
        resourceFileParser[Platform.IOS.ordinal()] = new IOSStrings();

        resourceModelList = new ResourceModelList();
    }

    /**
     * Language 별, 가져올 resource Directory 설정
     * @param language
     * @param directory
     * @return
     */
    public boolean setImportResourceDirectory(Language language, String directory) {
        if (StringUtil.isNotEmpty(directory)) {
            File dir = new File(directory);
            if (dir.exists() && dir.isDirectory()) {
                sourceDirInfoList.add(new SourceDirInfo(language, dir));
                return true;
            }
        }
        return false;
    }

    /**
     * 설정된 resource Directory 를 통해, 리소스를 가져온다.
     * @return
     */
    public boolean importPlatformResources() {
        resourceModelList.clear();

        for (SourceDirInfo sourceDirInfo : sourceDirInfoList) {
            if (sourceDirInfo.language != null && sourceDirInfo.dir != null) {
                loadResourceFile(resourceModelList, sourceDirInfo.language, sourceDirInfo.dir);
            }
        }

        resourceModelList.sortByValue(Language.KO, true);
        return true;
    }

    /**
     * UnionStResXml 파일 생성
     * @param target 생성할 file
     * @param overWrite 덮어쓰기 여부
     * @return
     */
    public boolean makeUnionStResXml(File target, boolean overWrite) {
        if (target == null) {
            Logger.e(TAG, "Can NOT make UnionStResXml file. target is Null.");
            return false;
        }
        if (target.exists()) {
            if (overWrite) {
                target.delete();
            } else {
                Logger.e(TAG, "Can NOT make UnionStResXml file. target is Already exist.");
                return false;
            }
        }
        UnionStResXml unionStResXml = new UnionStResXml();
        boolean result = unionStResXml.exportFile(resourceModelList, target);
        Logger.i(TAG, "make UnionStResXml(" + target.getAbsolutePath()+ ") result : " + result);
        return result;
    }

    /**
     * Xml 파일 생성.
     * @param target 생성 파일
     * @param overWrite 덮어쓰기 여부
     * @param columns 생성시 포함 할 컬럼 정보 (null : 전체 표시)
     * @return
     */
    public boolean makeExcel(File target, boolean overWrite, ExportXlsColumn[] columns) {
        if (target == null) {
            Logger.e(TAG, "Can NOT make UnionStResExcel file. target is Null.");
            return false;
        }
        if (target.exists()) {
            if (overWrite) {
                target.delete();
            } else {
                Logger.e(TAG, "Can NOT make UnionStResExcel file. target is Already exist.");
                return false;
            }
        }
        if (columns == null) {
            columns = ExportXlsColumn.values();
        }
        UnionStResExcel unionStResExcel = new UnionStResExcel(columns);
        boolean result = unionStResExcel.exportFile(resourceModelList, target);
        Logger.i(TAG, "make UnionStResExcel(" + target.getAbsolutePath()+ ") result : " + result);
        return result;
    }

    /**
     * UnionStResExcel 포멧의 xlsx 파일 데이터 import
     * @param source
     * @return
     */
    public boolean importFromExcel(File source) {
        if (source == null || !source.exists()) {
            Logger.e(TAG, "importFromExcel() Fail. source is not exist.");
            return false;
        }
        UnionStResExcel unionStResExcel = new UnionStResExcel(null);
        boolean result = unionStResExcel.importFile(source, resourceModelList);
        Logger.i(TAG, "import UnionStResExcel(" + source.getAbsolutePath()+ ") result : " + result);
        return result;
    }

    /**
     * Platform 별 String Resource 파일을 load & parse 하여, resourceModeList 에 추가 한다.
     * @param resourceModelList
     * @param language
     * @param file
     */
    private void loadResourceFile(ResourceModelList resourceModelList, Language language, File file) {
        if (file != null) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File childFile : files) {
                        loadResourceFile(resourceModelList, language, childFile);
                    }
                }
            } else {
                for (Platform platform : Platform.values()) {
                    IResourceString parser = resourceFileParser[platform.ordinal()];
                    if (parser != null) {
                        if (parser.isSupportFileType(file)) {
                            resourceModelList.addItems(platform, language, parser.getKeyValueMap(file));
                        }
                    }
                }
            }
        }
    }

    private void printMap(Map<String, String> map) {
        if (map != null) {
            for (String key : map.keySet()) {
                Logger.i(key, map.get(key));
            }
        }
    }
}
