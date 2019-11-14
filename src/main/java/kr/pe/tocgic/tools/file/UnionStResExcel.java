package kr.pe.tocgic.tools.file;

import kr.pe.tocgic.tools.data.ResourceDataManager;
import kr.pe.tocgic.tools.data.ResourceModel;
import kr.pe.tocgic.tools.data.enums.ExportXlsColumn;
import kr.pe.tocgic.tools.data.enums.Language;
import kr.pe.tocgic.tools.data.enums.Platform;
import kr.pe.tocgic.tools.functions.IResourceTransform;
import kr.pe.tocgic.tools.util.Logger;
import kr.pe.tocgic.tools.util.StringUtil;
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

    public UnionStResExcel(ExportXlsColumn[] columns) {
        this.columns = columns;
        platforms = Platform.values();
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

        //make Header
        row = sheet.createRow((short)indexRow++);
        for (ExportXlsColumn column : columns) {
            sheet.setColumnWidth(indexCol, column.getCellLength() * 256);//셀의 너비 (1글자:256)

            cell = row.createCell(indexCol++);
            cell.setCellStyle(styleHeader);
            cell.setCellValue(column.getValue());

            if (column == ExportXlsColumn.KEY) {
                isContainsColumnKey = true;
            }
        }

        //make body
        for (ResourceModel model : data) {
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

    }

    private void fillColumn(XSSFRow row, String key, Platform platform, ResourceModel model) {
        int indexCol = 0;
        for (ExportXlsColumn column : columns) {
            XSSFCell cell = row.createCell(indexCol++);
            cell.setCellStyle(styleBody);
            String value = "";
            switch (column) {
                case KEY:
                    if (key != null) {
                        value = key;
                    }
                    break;
                case PLATFORM:
                    if (platform != null) {
                        value = platform.getValue();
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
            cell.setCellValue(value);
        }
    }

    private void parseHeaderColumns(Map<Language, Integer> target, Row row) throws Exception {
        if (target == null || row == null) {
            throw new Exception("parseHeaderColumns(), invalid parameter");
        }
        target.clear();

        ExportXlsColumn[] columns = ExportXlsColumn.values();
        int cellSize = row.getPhysicalNumberOfCells();
        for (int index = 0; index < cellSize; index++) {
            String value = row.getCell(index).getStringCellValue();
            if (StringUtil.isNotEmpty(value)) {
                for (ExportXlsColumn column : columns) {
                    if (value.equals(column.getValue())) {
                        Language language = column.getLanguage();
                        if (language != null) {
                            target.put(language, index);
                        }
                        break;
                    }
                }
            }
        }
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

            int sheetIndex = 0;
            if (workbook instanceof HSSFWorkbook) {
                curSheet = ((HSSFWorkbook)workbook).getSheetAt(sheetIndex);
            } else {
                curSheet = ((XSSFWorkbook)workbook).getSheetAt(sheetIndex);
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
            parseHeaderColumns(cellIndexMapByLanguage, curSheet.getRow(0));
            if (cellIndexMapByLanguage.size() < 1) {
                Logger.e(TAG, "importFile() Fail. header parse error. Not match format with UnionStResExcel file type.");
                return false;
            }
            //parse Body
            for (int rowIndex = 1; rowIndex < rowSize; rowIndex++) {
                curRow = curSheet.getRow(rowIndex);

                //기본 언어에 대한 StringValue
                Cell curCell = curRow.getCell(cellIndexMapByLanguage.get(langDefault));
                String langDefValue = curCell != null ? curCell.getStringCellValue() : "";
                if (StringUtil.isNotEmpty(langDefValue)) {
                    for (Language language : cellIndexMapByLanguage.keySet()) {
                        if (language.isDefault()) {
                            continue;
                        }
                        Cell langCell = curRow.getCell(cellIndexMapByLanguage.get(language));
                        String langValue = langCell != null ? langCell.getStringCellValue() : "";
                        boolean updateResult = target.updateValue(langDefault, langDefValue, language, langValue);
                        if (!updateResult) {
                            Logger.w(TAG, "ResourceDataManager.updateValue() Fail. "+langDefValue+"["+langDefault+"], "+langValue+"["+language+"]");
                        }
                    }
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
}
