package bot.excel;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lewis on 2017/7/10.
 */
public class ExcelUtil {
    public static Workbook newWorkbook(InputStream in) throws IOException {
        return WorkbookFactory.create(in);
    }


    public static ExcelDatum convertToExcelDatum(Workbook workbook, int sheetNo) throws Exception {
        List<String> columnNames = new ArrayList<>();
        List<Map<String, String>> resultList = new ArrayList<>();
        //第几个sheet
        Sheet sheet = workbook.getSheetAt(sheetNo);
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        for (int rowIndex = firstRowNum; rowIndex <= lastRowNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            short firstCellNum = row.getFirstCellNum();
            short lastCellNum = row.getLastCellNum();
            //当是第一行的时候
            if (rowIndex == firstRowNum) {
                for (int cellIndex = firstCellNum; cellIndex <= lastCellNum; cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    if (cell == null) {
                        continue;
                    }
                    columnNames.add(getCellValue(cell));
                }
                continue;
            }
            Map<String, String> cellContentMap = new HashMap<>();
            for (int cellIndex = firstCellNum; cellIndex <= lastCellNum; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                if (cell == null) {
                    continue;
                }
                String colName = columnNames.get(cellIndex);
                String value = getCellValue(cell);

                cellContentMap.put(colName, "" + value);
            }
            if (cellContentMap.size() > 0) {
                resultList.add(cellContentMap);
            }
        }
        return new ExcelDatum(columnNames, resultList);
    }


    public static HSSFWorkbook newWorkbookWithDatum(ExcelDatum excelDatum, String sheetName) {
        HSSFWorkbook workbook = new HSSFWorkbook();

        List<Map<String, String>> bodies = excelDatum.getDatum();

        List<String> headers = excelDatum.getHeaders();

        /**
         * 1.创建一个sheet
         */
        HSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setDefaultColumnWidth(20);
        sheet.setDefaultRowHeightInPoints(20);
        /**
         * 2.创建第一行作为标题行
         */
        HSSFRow row = sheet.createRow(0);
        /**
         * 3.给标题行存入标题
         */
        int cellIndex = 0;
        for (String header : headers) {
            HSSFCell cell = row.createCell(cellIndex);
            cell.setCellValue(header);
            cellIndex++;
        }
        /**
         * 4.给数据行存入数据
         */
        for (Map<String, String> mapRow : bodies) {
            row = sheet.createRow((row.getRowNum() + 1));
            cellIndex = 0;
            for (String header : headers) {
                HSSFCell cell = row.createCell(cellIndex);
                cell.setCellValue(mapRow.get(header));
                cellIndex++;
            }
        }
        return workbook;
    }

    public static String getCellValue(Cell cell) {
        return getCellValue(cell, null, "yyyy-MM-dd:HH:mm:ss");
    }


    public static String getCellValue(Cell cell, FormulaEvaluator evaluator, String dataFormatter) {
        if (cell == null
                || (cell.getCellType() == CellType.STRING && StringUtils.isBlank(cell
                .getStringCellValue()))) {
            return null;
        }
        CellType cellType = cell.getCellType();
        if (cellType == CellType.BLANK) {
            return null;

        } else if (cellType == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cellType == CellType.ERROR) {
            return String.valueOf(cell.getErrorCellValue());
        } else if (cellType == CellType.FORMULA) {
            try {
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    return String.valueOf(cell.getDateCellValue());
                } else {
                    if (evaluator != null) {
                        CellValue cellValue = evaluator.evaluate(cell);
                        if (cellValue.getCellType() == CellType.STRING) {
                            return cellValue.getStringValue();
                        } else if (cellValue.getCellType() == CellType.NUMERIC) {
                            return getNumericVal(String.valueOf(evaluator.evaluate(cell).getNumberValue()));
                        } else {
                            return cellValue.getStringValue();
                        }
                    } else {
                        return getNumericVal(String.valueOf(cell.getNumericCellValue()));
                    }
                }
            } catch (IllegalStateException e) {
                try {
                    return cell.getStringCellValue();
                } catch (Exception e1) {
                    return cell.getCellFormula();
                }
            }
        } else if (cellType == CellType.NUMERIC) {
            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy:HH:mm:ss");
                return dateFormat.format(cell.getDateCellValue());
            } else {
                DecimalFormat df = new DecimalFormat();
                String s = String.valueOf(df.format(cell.getNumericCellValue()));
                if (s.contains(",")) {
                    s = s.replace(",", "");
                }
                return getNumericVal(s);
            }
        } else if (cellType == CellType.STRING)
            return cell.getStringCellValue();
        else {
            return null;
        }

    }


    //fixme 暂时的法子
    private static String getNumericVal(String val) {
        int point = val.indexOf(".");
        if (point == -1) {
            return val;
        }
        String decimal = val.substring(point + 1);

        if (decimal.length() == 1) {
            if ("0".equals(decimal)) {
                return val.substring(0, point);
            } else {
                return val;
            }
        } else if (decimal.length() == 2) {
            return val;
        } else if (decimal.length() > 2) {
            return new BigDecimal(val).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        } else {
            return val;
        }

    }
}
