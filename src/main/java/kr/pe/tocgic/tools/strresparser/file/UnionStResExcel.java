package kr.pe.tocgic.tools.strresparser.file;

import kr.pe.tocgic.tools.strresparser.data.LanguageModel;
import kr.pe.tocgic.tools.strresparser.data.ResourceDataManager;
import kr.pe.tocgic.tools.strresparser.data.ResourceModel;
import kr.pe.tocgic.tools.strresparser.data.enums.ExportXlsColumn;
import kr.pe.tocgic.tools.strresparser.data.enums.Language;
import kr.pe.tocgic.tools.strresparser.data.enums.Platform;
import kr.pe.tocgic.tools.strresparser.functions.IResourceTransform;
import kr.pe.tocgic.tools.strresparser.util.Logger;
import kr.pe.tocgic.tools.strresparser.util.StringUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnionStResExcel implements IResourceTransform {
    private static final String TAG = UnionStResExcel.class.getSimpleName();

    private String sheetName = "UnionStResExcel";

    private ExportXlsColumn[] columns;

    private Platform[] platforms;
    private CellStyle styleHeader;
    private CellStyle styleBody;
    private CellStyle styleLock;
    private boolean isIgnoreEmptyString;

    private int columnIndexHiddenKeys, columnIndexKey, columnIndexPlatform;


    public UnionStResExcel(ExportXlsColumn[] columns) {
        this.columns = columns;
        platforms = Platform.values();
    }

    public UnionStResExcel(ExportXlsColumn[] columns, boolean isIgnoreEmptyString) {
        this(columns);
        this.isIgnoreEmptyString = isIgnoreEmptyString;
    }

    @Override
    public boolean exportFile(ResourceDataManager source, File target) {
        if (source == null) {
            Logger.e(TAG, "exportFile() Fail. source is null.");
            return false;
        }
        if (target == null) {
            Logger.e(TAG, "exportFile() Fail. target is null.");
            return false;
        }
        if (columns == null || columns.length < 1) {
            Logger.e(TAG, "exportFile() Fail. columns is empty.");
            return false;
        }

        boolean result;
        List<ResourceModel> data = source.getCopyResourceModelList();

        //XSSFWorkbook 세팅
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sheetName);
        XSSFRow row;
        XSSFCell cell;
        createStyles(workbook);

        int indexRow = 0;
        int indexCol = 0;

        //Key 컬럼 포함 여부
        boolean isContainsColumnKey = false;
        for (ExportXlsColumn column : columns) {
            if (column == ExportXlsColumn.KEY) {
                isContainsColumnKey = true;
                break;
            }
        }

        //make Header
        row = sheet.createRow((short)indexRow++);
        for (ExportXlsColumn column : columns) {
            sheet.setColumnWidth(indexCol, column.getCellLength() * 256);//셀의 너비 (1글자:256)

            cell = row.createCell(indexCol++);
            cell.setCellType(CellType.STRING);
            cell.setCellStyle(styleHeader);
            cell.setCellValue(column.getValue());
        }

        //make body
        for (ResourceModel model : data) {
            if (isIgnoreEmptyString && model.getLanguageModel().isEmpty()) {
                continue;
            }
            if (isContainsColumnKey) {
                for (Platform platform : platforms) {
                    List<String> keys = model.getKeyList(platform);
                    for (String key : keys) {
                        row = sheet.createRow((short)indexRow++);
                        fillColumn(row, key, platform, model);
                    }
                }
            } else {
                row = sheet.createRow((short)indexRow++);
                fillColumn(row, null, null, model);
            }
        }

        //엑셀파일 세팅 후 파일 생성
        try {
            //file을 생성할 폴더가 없으면 생성합니다.
            File dir = target.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(target);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            result = true;
        } catch(Exception e) {
            e.printStackTrace();
            result = false;
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Excel Header & Body cell 에 대한 Style 정의
     * @param workbook
     */
    private void createStyles(Workbook workbook) {
        //CellStyle Header
        styleHeader = workbook.createCellStyle();
        styleHeader.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //styleHeader.setFillPattern("셀의 패턴을 세팅");
        styleHeader.setAlignment(HorizontalAlignment.CENTER);

        //CellStyle Body
        styleBody = workbook.createCellStyle();
        styleBody.setWrapText(true);
        styleBody.setAlignment(HorizontalAlignment.LEFT);

        //CellStyle Lock
        styleLock = workbook.createCellStyle();
        styleLock.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        styleLock.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleLock.setAlignment(HorizontalAlignment.LEFT);
    }

    private void fillColumn(XSSFRow row, String key, Platform platform, ResourceModel model) {
        int indexCol = 0;
        for (ExportXlsColumn column : columns) {
            XSSFCell cell = row.createCell(indexCol++);
            cell.setCellStyle(styleBody);
            cell.setCellType(CellType.STRING);
            String value = "";
            switch (column) {
                case HIDDEN_KEYS:
                    cell.setCellStyle(styleLock);
                    if (model != null) {
                        value = getHiddenKeys(model);
                    }
                    break;
                case KEY:
                    if (key != null) {
                        value = key;
                    }
                    break;
                case PLATFORM:
                    if (platform != null) {
                        value = platform.toString();
                    }
                    break;
                case LANGUAGE_KO:
                    if (model != null) {
                        value = model.getValue(Language.KO);
                    }
                    break;
                case LANGUAGE_JA:
                    if (model != null) {
                        value = model.getValue(Language.JA);
                    }
                    break;
                case LANGUAGE_EN:
                    if (model != null) {
                        value = model.getValue(Language.EN);
                    }
                    break;
            }
//            동작 안됨 (셀에 '' 노출됨) - 우선 보류
//            if (isStartedSpecialChar(value)) {
//                value = "'" + value;
//            }
            cell.setCellValue(value);
        }
    }

    /**
     * Excel에서 작은 따옴표가 사라지는 문제
     * (https://answers.microsoft.com/ko-kr/msoffice/forum/msoffice_excel-mso_other-mso_2007/%EC%97%91%EC%85%80%EC%9E%91%EC%9D%80/94dd56fb-02af-4957-a668-ca1eedbf506d?messageId=f5fc533c-3d3a-482e-9759-dee8e216dccb)
     *
     * 문의 하신 Excel에서 작은 따옴표가 사라지는 문제에 대한 답변을 드리겠습니다.
     * 말씀해 주신 작은 따옴표는 !@#$%^&*()~"',.? 등의 기호를 텍스트로 인식시키기 위해서 사용하는 구분자입니다.
     * 따라서 작은 따옴표가 나타나도록 설정하고자 하신다면, 작은 따옴표를 2개 연속으로 입력하시기 바랍니다.
     * 예를 들어서 ''내용  이런 방식으로 입력해 주시기 바랍니다.
     *
     * @param value
     * @return
     */
    private boolean isStartedSpecialChar(String value) {
        if (StringUtil.isNotEmpty(value)) {
            String[] specials = {"'"};
            for (String special : specials) {
                if (value.startsWith(special)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final String TOKEN_ITEM = "^:^";
    private static final String REGEX_TOKEN_ITEM = "\\^:\\^";

    /**
     * {Platform}{keyValue}{TOKEN_ITEM}{Platform}{keyValue}{TOKEN_ITEM}...
     * @param model
     * @return
     */
    private String getHiddenKeys(ResourceModel model) {
        StringBuilder keys = new StringBuilder();
        if (model != null) {
            List<String> keyPlatformList = new ArrayList<>();
            for (Platform platform : Platform.values()) {
                List<String> keyList = model.getKeyList(platform);
                for (String key : keyList) {
                    keyPlatformList.add(platform + key);
                }
            }
            int size = keyPlatformList.size();
            for (int i = 0; i < size; i++) {
                keys.append(keyPlatformList.get(i));
                if (i + 1 < size) {
                    keys.append(TOKEN_ITEM);
                }
            }
        }
        return keys.toString();
    }

    private Map<Platform, List<String>> getHiddenKeyMap(String keyPlatformString) {
        Map<Platform, List<String>> map = new HashMap<>();

        String[] items = keyPlatformString.split(REGEX_TOKEN_ITEM);
        for (String item : items) {
            if (StringUtil.isEmpty(item)) {
                continue;
            }
            for (Platform platform : Platform.values()) {
                if (item.startsWith(platform.toString())) {
                    List<String> list = map.get(platform);
                    if (list == null) {
                        list = new ArrayList<>();
                        map.put(platform, list);
                    }
                    String keyValue = item.substring(platform.toString().length());
                    if (StringUtil.isNotEmpty(keyValue)) {
                        list.add(keyValue);
                    }
                }
            }
        }
        return map;
    }

    private ExportXlsColumn.ParseType parseHeaderColumns(Map<Language, Integer> target, Row row) throws Exception {
        if (target == null || row == null) {
            throw new Exception("parseHeaderColumns(), invalid parameter");
        }
        target.clear();

        List<ExportXlsColumn> columns = new ArrayList<>();
        int cellSize = row.getPhysicalNumberOfCells();
        for (int index = 0; index < cellSize; index++) {
            String value = row.getCell(index).getStringCellValue();
            if (StringUtil.isNotEmpty(value)) {
                for (ExportXlsColumn column : ExportXlsColumn.values()) {
                    if (value.equals(column.getValue())) {
                        columns.add(column);

                        Language language = column.getLanguage();
                        if (language != null) {
                            target.put(language, index);
                        }
                        break;
                    }
                }
            }
        }

        ExportXlsColumn.ParseType parseType;
        if (columns.contains(ExportXlsColumn.HIDDEN_KEYS)) {
            parseType = ExportXlsColumn.ParseType.HIDDEN_KEY;
            columnIndexHiddenKeys = columns.indexOf(ExportXlsColumn.HIDDEN_KEYS);
        } else if (columns.contains(ExportXlsColumn.KEY) && columns.contains(ExportXlsColumn.PLATFORM)){
            parseType = ExportXlsColumn.ParseType.KEY;
            columnIndexKey = columns.indexOf(ExportXlsColumn.KEY);
            columnIndexPlatform = columns.indexOf(ExportXlsColumn.PLATFORM);
        } else {
            parseType = ExportXlsColumn.ParseType.VALUE;
        }
        return parseType;
    }

    @Override
    public boolean importFile(File source, ResourceDataManager target) {
        FileInputStream fis = null;
        Workbook workbook = null;

        Map<Language, Integer> cellIndexMapByLanguage = new HashMap<>();
        Language langDefault = Language.getDefaultLanguage();
        try {
            fis = new FileInputStream(source.getAbsolutePath());

            try {
                workbook = new HSSFWorkbook(fis);
            } catch (OfficeXmlFileException e) {
                Logger.i(TAG, "importFile(), File is not HSSF format. try to XSSF");
                fis = new FileInputStream(source.getAbsolutePath());
                workbook = new XSSFWorkbook(fis);
            }
            Sheet curSheet;
            Row curRow;

            if (workbook.getNumberOfSheets() < 1) {
                Logger.e(TAG, "importFile() Fail. sheet size 0.");
                return false;
            }
            int sheetIndex = workbook.getSheetIndex(sheetName);
            if (sheetIndex < 0) {
                Logger.e(TAG, "importFile() Fail. sheet '" + sheetName + "' not found.");
                return false;
            }
            if (workbook instanceof HSSFWorkbook) {
                HSSFWorkbook hssfWorkbook = ((HSSFWorkbook)workbook);
                curSheet = hssfWorkbook.getSheetAt(sheetIndex);
            } else {
                XSSFWorkbook xssfWorkbook = ((XSSFWorkbook)workbook);
                curSheet = xssfWorkbook.getSheetAt(sheetIndex);
            }
            if (curSheet == null) {
                Logger.e(TAG, "importFile() Fail. sheet not exist.");
                return false;
            }

            int rowSize = curSheet.getPhysicalNumberOfRows();
            if (rowSize < 1) {
                Logger.e(TAG, "importFile() Fail. rowSize < 1");
                return false;
            }
            //parse Header
            ExportXlsColumn.ParseType parseType = parseHeaderColumns(cellIndexMapByLanguage, curSheet.getRow(0));
            if (cellIndexMapByLanguage.size() < 1) {
                Logger.e(TAG, "importFile() Fail. header parse error. Not match format with UnionStResExcel file type.");
                return false;
            }

            //parse Body
            for (int rowIndex = 1; rowIndex < rowSize; rowIndex++) {
                curRow = curSheet.getRow(rowIndex);

                switch (parseType) {
                    case HIDDEN_KEY:
                        parseByHiddenKeys(curRow, cellIndexMapByLanguage, target);
                        break;
                    case KEY:
                        parseByKeyPlatform(curRow, cellIndexMapByLanguage, target);
                        break;
                    case VALUE:
                        parseByValue(curRow, cellIndexMapByLanguage, langDefault, target);
                        break;
                }
            }
            return true;
        } catch (Exception e) {
            Logger.e(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if( workbook!= null) workbook.close();
                if( fis!= null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private LanguageModel getLanguageModel(Row curRow, Map<Language, Integer> cellIndexMapByLanguage) {
        LanguageModel languageModel = new LanguageModel();
        if (curRow == null || cellIndexMapByLanguage == null) {
            Logger.e(TAG, "getLanguageModel() Fail. parameter error.");
            return languageModel;
        }

        for (Language language : cellIndexMapByLanguage.keySet()) {
            Cell langCell = curRow.getCell(cellIndexMapByLanguage.get(language));
            String langValue = langCell == null ? "" : langCell.getStringCellValue();
            if (isIgnoreEmptyString && langCell == null) {
                continue;
            }
            languageModel.setValue(language, langValue);
        }
        return languageModel;
    }

    private void parseByKeyPlatform(Row curRow, Map<Language, Integer> cellIndexMapByLanguage, ResourceDataManager target) {
        if (target == null || curRow == null) {
            Logger.e(TAG, "parseByKeyPlatform() Fail. parameter error.");
            return;
        }

        try {
            Cell cellKey = curRow.getCell(columnIndexKey);
            Cell cellPlatform = curRow.getCell(columnIndexPlatform);
            String key = cellKey.getStringCellValue();
            Platform platform = Platform.valueOf(cellPlatform.getStringCellValue());

            LanguageModel languageModel = getLanguageModel(curRow, cellIndexMapByLanguage);

            target.updateByKeyPlatform(platform, key, languageModel);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "parseByKeyPlatform(), e:"+e.getMessage());
        }
    }

    private void parseByHiddenKeys(Row curRow, Map<Language, Integer> cellIndexMapByLanguage, ResourceDataManager target) {
        if (target == null || curRow == null) {
            Logger.e(TAG, "parseByHiddenKeys() Fail. parameter error.");
            return;
        }

        Cell cellHiddenKeys = curRow.getCell(columnIndexHiddenKeys);
        if (cellHiddenKeys == null) {
            Logger.e(TAG, "parseByHiddenKeys() Fail. Cell not found [cellHiddenKeys].");
            return;
        }

        String keyPlatformString = cellHiddenKeys.getStringCellValue();
        Map<Platform, List<String>> map = getHiddenKeyMap(keyPlatformString);
        if (map.size() > 0) {
            LanguageModel languageModel = getLanguageModel(curRow, cellIndexMapByLanguage);

            for (Platform platform : map.keySet()) {
                List<String> keyList = map.get(platform);
                if (keyList != null) {
                    for (String key : keyList) {
                        target.updateByKeyPlatform(platform, key, languageModel);
                    }
                }
            }
        }
    }

    /**
     *
     * @param curRow
     * @param cellIndexMapByLanguage
     * @param langDefault
     * @param target
     */
    private void parseByValue(Row curRow, Map<Language, Integer> cellIndexMapByLanguage, Language langDefault, ResourceDataManager target) {
        //기본 언어에 대한 StringValue
        Cell curCell = curRow.getCell(cellIndexMapByLanguage.get(langDefault));
        String langDefValue = curCell != null ? curCell.getStringCellValue() : "";
        if (StringUtil.isNotEmpty(langDefValue)) {
            for (Language language : cellIndexMapByLanguage.keySet()) {
                if (language.isDefault()) {
                    continue;
                }
                Cell langCell = curRow.getCell(cellIndexMapByLanguage.get(language));
                String langValue = langCell == null ? "" : langCell.getStringCellValue();
                if (isIgnoreEmptyString && langCell == null) {
                    continue;
                }
                boolean updateResult = target.updateValue(langDefault, langDefValue, language, langValue);
                if (!updateResult) {
                    Logger.w(TAG, "ResourceDataManager.updateValue() Fail. " + langDefValue + "[" + langDefault + "], " + langValue + "[" + language + "]");
                }
            }
        }
    }
}
