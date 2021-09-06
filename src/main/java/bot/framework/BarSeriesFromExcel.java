package bot.framework;

import bot.utils.excel.ExcelProcessor;
import bot.utils.excel.ExcelTable;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.BarSeries;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by louisyuu on 2021/9/6 3:09 下午
 */
public class BarSeriesFromExcel extends GenericBarSeriesSource {

    private final String filepath;
    private final String filename;

    public BarSeriesFromExcel(String filepath, String filename,
                              String symbol, CandlestickInterval interval, int initKLineCount) {
        super(symbol, interval, initKLineCount);
        this.filepath = filepath;
        this.filename = filename;
    }



    @Override
    public boolean isLivingStream() {
        return false;
    }

    @Override
    public void process() {
        new ExcelProcessor(filepath, filename) {
            @Override
            public void doProcess(ExcelTable table) throws Exception {
                for (Map<String, Object> row : table.getRows()) {
                    BigDecimal open = new BigDecimal(String.valueOf(row.get("Open")));
                    BigDecimal high = new BigDecimal(String.valueOf(row.get("High")));
                    BigDecimal low = new BigDecimal(String.valueOf(row.get("Low")));
                    BigDecimal close = new BigDecimal(String.valueOf(row.get("Close")));
                    BigDecimal volume = new BigDecimal(String.valueOf(row.get("Volume")));
                    String timestamp = String.valueOf(row.get("Timestamp"));
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(Long.parseLong(timestamp)).toInstant(), ZoneId.systemDefault());
                    barSeries.addBar(zonedDateTime, open, high, low, close, volume);
                }
            }
        }
    }
}
