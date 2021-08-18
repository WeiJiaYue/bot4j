package excel;


import java.util.List;
import java.util.Map;

/**
 * Created by louisyuu on 2020/12/10 3:24 pm
 * A instance of ExcelTable holds a sheet of excel data
 */
public class ExcelTable {


    /**
     * Excel columns
     */
    private final List<String> columns;


    /**
     * Excel rows
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

    public ExcelTable updateColumn(String column) {
        getColumns().remove(column);
        return this;
    }

    public ExcelTable updateRow(int rowIdx,Map<String, Object> row){
        getRows().get(rowIdx).put()
    }
}
