package bot.utils;

import bot.utils.excel.ExcelProcessor;
import bot.utils.excel.ExcelTable;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Created by louisyuu on 2021/8/13 5:34 下午
 */
public class BarSeriesToExcel extends ExcelProcessor {

    public final static String PROJECT_PATH = System.getProperty("user.dir");
    public final static String SRC_PATH = "/src/main/java/bot/file/";
    public final static String FILE_PATH = PROJECT_PATH + SRC_PATH;
    public final static String CANDLESTICK_TEMPLE_FILE_NAME = "CandlestickTemple.xlsx";


    //Custom
    private final static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    private final static String symbol = "SOLUSDT";
    private final static int KLINE_COUNT = 1000;


    public static void main(String[] args) throws Exception {
        BarSeriesToExcel processor = new BarSeriesToExcel(FILE_PATH, CANDLESTICK_TEMPLE_FILE_NAME);
        processor.setNewFileName(INTERVAL.val()+symbol);
        processor.process();
    }


    public BarSeriesToExcel(String filepath, String filename) {
        super(filepath, filename);
    }

    private List<Candlestick> getCandlestick() {
        SyncRequestClient syncRequestClient = SyncRequestClient.create();
        return syncRequestClient.getCandlestick(symbol,
                INTERVAL,
                null,
                null, KLINE_COUNT);

    }

    @Override
    public void doProcess(ExcelTable table) throws Exception {
        List<Candlestick> candlesticks = getCandlestick();
        for (Candlestick candlestick : candlesticks) {
            Map<String, Object> row = table.createEmptyRow();
            Date date = new Date(candlestick.getCloseTime());
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
            ZonedDateTime zoned = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            row.put("ZoneDateTime", zoned.toString());
            row.put("Timestamp", String.valueOf(candlestick.getCloseTime()));
            row.put("DateTime", dateFormat.format(date));
            row.put("Open", String.valueOf(candlestick.getOpen()));
            row.put("High", String.valueOf(candlestick.getHigh()));
            row.put("Low", String.valueOf(candlestick.getLow()));
            row.put("Close", String.valueOf(candlestick.getClose()));
            row.put("Volume", String.valueOf(candlestick.getVolume()));
            table.addRow(row);
        }
    }


}
