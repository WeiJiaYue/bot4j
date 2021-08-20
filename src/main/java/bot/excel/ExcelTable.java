package bot.excel;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by louisyuu
 * <p>
 * A instance of ExcelTable holds a sheet of excel data
 */
public class ExcelTable {


    /**
     * Excel columns
     */
    private final List<String> columns;


    /**
     * Excel rows
     * <p>
     * The Map<String, String> is the key with column name and the value with corresponding data
     */
    private final List<Map<String, Object>> rows;


    public ExcelTable(List<String> columns, List<Map<String, Object>> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public ExcelTable addColumn(String column) {
        getColumns().add(column);
        return this;
    }

    public ExcelTable deleteColumn(String column) {
        getColumns().remove(column);
        return this;
    }


    public Map<String, Object> createEmptyRow() {
        return new HashMap<>();
    }

    /**
     * Add ops
     *
     * @param newRow New add row will be added
     * @return this
     */
    public ExcelTable addRow(Map<String, Object> newRow) {
        getRows().add(newRow);
        return this;
    }


    /**
     * Delete ops
     *
     * @param rowIdx Row index will be deleted
     * @return boolean
     */
    public boolean deleteRow(int rowIdx) {
        try {
            getRows().remove(rowIdx);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    /**
     * Update ops
     *
     * @param rowRef Row will be updated
     * @param column Column name
     * @param data   New data
     * @return this
     */
    public ExcelTable updateRow(Map<String, Object> rowRef, String column, Object data) {
        rowRef.put(column, data);
        return this;
    }


    public Map<String, Object> getRow(int rowIdx) {
        return getRows().get(rowIdx);
    }

    public List<Map<String, Object>> selectRowsEqualsColumn(String column, String data) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : getRows()) {
            if (row.containsKey(column)) {
                Object columnData = row.get(column);
                if (data == null && columnData == null) {
                    rows.add(row);
                } else if (data != null && data.equals(columnData)) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public List<Map<String, Object>> selectRowsLikeColumn(String column, String data) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : getRows()) {
            if (row.containsKey(column)) {
                Object columnData = row.get(column);
                if (data == null && columnData == null) {
                    rows.add(row);
                } else if (columnData != null && data != null && String.valueOf(columnData).contains(data)) {
                    rows.add(row);
                }
            }
        }
        return rows;
    }
}
