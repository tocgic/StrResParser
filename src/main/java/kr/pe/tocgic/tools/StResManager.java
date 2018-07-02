package kr.pe.tocgic.tools;

import kr.pe.tocgic.tools.data.ResourceDataManager;
import kr.pe.tocgic.tools.data.enums.ExportXlsColumn;
import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.data.enums.Platform;
import kr.pe.tocgic.tools.file.UnionStResExcel;
import kr.pe.tocgic.tools.file.UnionStResXml;
import kr.pe.tocgic.tools.file.platform.AndroidXml;
import kr.pe.tocgic.tools.file.platform.IOSStrings;
import kr.pe.tocgic.tools.file.platform.ServerProperties;
import kr.pe.tocgic.tools.functions.IResourceString;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StResManager {
    private static final String TAG = StResManager.class.getSimpleName();

    private List<SourceDirInfo> sourceDirInfoList;
    private IResourceString[] resourceFileParser;
    private ResourceDataManager resourceDataManager;

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
        resourceFileParser[Platform.SERVER.ordinal()] = new ServerProperties();

        resourceDataManager = new ResourceDataManager();
    }

    /**
     * Language 별, resource Directory 설정
     * @param language
     * @param directory
     * @return
     */
    public boolean addResourcePath(Language language, String directory) {
        if (StringUtil.isNotEmpty(directory)) {
            File dir = new File(directory);
            if (dir.exists()) {
                sourceDirInfoList.add(new SourceDirInfo(language, dir));
                return true;
            }
        }
        return false;
    }

    /**
     * 설정된 resource directory 에서 리소스 파일 을 loading 하여, ResourceDataManager 에 등록 한다.
     * @return
     */
    public boolean doLoadResources() {
        return loadResources(true);
    }

    /**
     * 설정된 resource directory 에서 리소스 파일 을 ResourceDataManager 의 data 로 갱신 저장 한다.
     * @return
     */
    public boolean doWriteResources() {
        return loadResources(false);
    }

    /**
     * 설정된 resource Directory 를 통해, 리소스를 (read/write) 온다.
     * @param isRead
     * @return
     */
    private boolean loadResources(boolean isRead) {
        if (isRead) {
            resourceDataManager.clear();
        }

        for (SourceDirInfo sourceDirInfo : sourceDirInfoList) {
            if (sourceDirInfo.language != null && sourceDirInfo.dir != null) {
                loadResourceFile(resourceDataManager, sourceDirInfo.language, sourceDirInfo.dir, isRead);
            }
        }
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
        boolean result = unionStResXml.exportFile(resourceDataManager, target);
        Logger.i(TAG, "make UnionStResXml(" + target.getAbsolutePath()+ ") result : " + result);
        return result;
    }

    /**
     * Xml 파일 생성.
     * @param target 생성 파일
     * @param overWrite 덮어쓰기 여부
     * @param columns 생성시 포함 할 컬럼 정보 (null : 전체 표시)
     * @param isIgnoreEmptyString 모두(ko, en, ja..) 비어있는 리소스 의 경우 무시 여부
     * @return
     */
    public boolean makeExcel(File target, boolean overWrite, ExportXlsColumn[] columns, boolean isIgnoreEmptyString) {
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
            List<ExportXlsColumn> list = new ArrayList<>(Arrays.asList(ExportXlsColumn.values()));
            list.remove(ExportXlsColumn.HIDDEN_KEYS);
            columns = list.toArray(new ExportXlsColumn[0]);
        }
        UnionStResExcel unionStResExcel = new UnionStResExcel(columns, isIgnoreEmptyString);
        boolean result = unionStResExcel.exportFile(resourceDataManager, target);
        Logger.i(TAG, "make UnionStResExcel(" + target.getAbsolutePath()+ ") result : " + result);
        return result;
    }

    /**
     * UnionStResExcel 포멧의 xlsx 파일 데이터 import
     * @param source
     * @param isIgnoreEmptyString 각 Language(ko, en, ja..) 별 비어있는 리소스 의 경우 무시 여부
     * @return
     */
    public boolean importFromExcel(File source, boolean isIgnoreEmptyString) {
        if (source == null || !source.exists()) {
            Logger.e(TAG, "importFromExcel() Fail. source is not exist.");
            return false;
        }
        UnionStResExcel unionStResExcel = new UnionStResExcel(null, isIgnoreEmptyString);
        boolean result = unionStResExcel.importFile(source, resourceDataManager);
        Logger.i(TAG, "import UnionStResExcel(" + source.getAbsolutePath()+ ") result : " + result);
        return result;
    }

    /**
     * Platform 별 String Resource 파일을 load & parse 하여, resourceModeList 에 추가 한다.
     * @param resourceDataManager
     * @param language
     * @param file
     * @param isRead {true : 읽기, false : 쓰기}
     */
    private void loadResourceFile(ResourceDataManager resourceDataManager, Language language, File file, boolean isRead) {
        if (file != null) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File childFile : files) {
                        loadResourceFile(resourceDataManager, language, childFile, isRead);
                    }
                }
            } else {
                for (Platform platform : Platform.values()) {
                    IResourceString parser = resourceFileParser[platform.ordinal()];
                    if (parser != null) {
                        if (parser.isSupportFileType(file)) {
                            if (isRead) {
                                //read resource file
                                resourceDataManager.addItems(platform, language, parser.getKeyValueMap(file));
                            } else {
                                //write resource file
                                parser.applyValue(resourceDataManager.getLanguageMap(platform), language, file);
                            }
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
