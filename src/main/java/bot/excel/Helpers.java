package bot.excel;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by louisyuu
 * <p>
 * The core helper methods are
 * excelToTable: Let excel data convert to java object {@link ExcelTable}
 * tableToExcel: Let java object {@link ExcelTable} convert to excel data
 */
public class Helpers {

    public enum ExcelSuffix {
        xls, xlsx
    }

    public static ExcelTable excelToTable(InputStream inputStream, int sheetNo) throws Exception {
        return excelToTable(newWorkbook(inputStream), sheetNo);
    }

    public static ExcelTable excelToTable(Workbook workbook, int sheetNo) throws Exception {
        List<String> columns = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();
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
                    columns.add(getCellValue(cell));
                }
                continue;
            }
            Map<String, Object> cellContentMap = new HashMap<>();
            for (int cellIndex = firstCellNum; cellIndex <= lastCellNum; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                if (cell == null) {
                    continue;
                }
                String colName = columns.get(cellIndex);
                String value = getCellValue(cell);

                cellContentMap.put(colName, "" + value);
            }
            if (cellContentMap.size() > 0) {
                rows.add(cellContentMap);
            }
        }
        return new ExcelTable(columns, rows);
    }


    public static Workbook tableToExcel(ExcelTable table, ExcelSuffix suffix, String sheetName) {
        Workbook workbook = newWorkbook(suffix);
        /**
         * 1.Create a sheet
         */
        Sheet sheet = workbook.createSheet(sheetName);
        sheet.setColumnWidth(0, 20 * 256);
        sheet.setDefaultColumnWidth(20);
        sheet.setDefaultRowHeightInPoints(20);
        /**
         * 2.Create a 1st row as title row
         */
        Row excelRow = sheet.createRow(0);
        /**
         * 3.Padding title row
         */
        int cellIndex = 0;
        for (String column : table.getColumns()) {
            Cell cell = excelRow.createCell(cellIndex);
            cell.setCellValue(column);
            cellIndex++;
        }
        /**
         * 4.Padding data row
         */
        for (Map<String, Object> tableRow : table.getRows()) {
            excelRow = sheet.createRow((excelRow.getRowNum() + 1));
            cellIndex = 0;
            for (String column : table.getColumns()) {
                Cell cell = excelRow.createCell(cellIndex);
                cell.setCellValue(tableRow.get(column) == null ? null : String.valueOf(tableRow.get(column)));
                cellIndex++;
            }
        }
        return workbook;
    }

    public static Workbook newWorkbook(ExcelSuffix suffix) {
        if (suffix.equals(ExcelSuffix.xls)) {
            return new HSSFWorkbook();
        }
        return new XSSFWorkbook();
    }

    public static Workbook newWorkbook(InputStream in) throws IOException {
        return WorkbookFactory.create(in);
    }


    /**
     * =================================Private methods=================================
     */


    private static String getCellValue(Cell cell) {
        return getCellValue(cell, null, "yyyy-MM-dd:HH:mm:ss");
    }


    private static String getCellValue(Cell cell, FormulaEvaluator evaluator, String dataFormatter) {
        if (cell == null
                || (cell.getCellType() == CellType.STRING && isBlank(cell.getStringCellValue()))) {
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
            if (DateUtil.isCellDateFormatted(cell)) {
                DateFormat dateFormat = new SimpleDateFormat(dataFormatter);
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

    private static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }
}
