package bot.excel;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * Created by louisyuu on 2020/12/10 3:24 下午
 */
public class ExcelDatum {


    /**
     * 标题
     */
    private List<String> headers;


    /**
     * Map<String,String>,Key是列名，Value是值
     * List<Map<String, String>> 是这些Map的集合
     * 对应{@link ExcelUtil#convertToList(Workbook, int)}的返回值
     */
    private List<Map<String, String>> datum;

    public ExcelDatum(List<String> headers, List<Map<String, String>> datum) {
        this.headers = headers;
        this.datum = datum;
    }


    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Map<String, String>> getDatum() {
        return datum;
    }

    public void setDatum(List<Map<String, String>> datum) {
        this.datum = datum;
    }
}
