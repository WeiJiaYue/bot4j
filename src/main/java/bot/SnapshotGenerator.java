package bot;

import bot.excel.ExcelDatum;
import bot.excel.ExcelProcessor;
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
public class SnapshotGenerator extends ExcelProcessor {

    public final static String snapshot_file_name = "Binance_latest_one_min_candlestick.xls";



    public static void main(String[] args) throws Exception {
        SnapshotGenerator processor = new SnapshotGenerator(filePath, templateFileName);
        processor.setNewFilename(snapshot_file_name);
        processor.process();
    }


    public SnapshotGenerator(String filepath, String filename) {
        super(filepath, filename);
    }

    private List<Candlestick> getCandlestick() {
        SyncRequestClient syncRequestClient = SyncRequestClient.create();
        return syncRequestClient.getCandlestick("BTCUSDT",
                CandlestickInterval.ONE_MINUTE,
                null,
                null, 1000);

    }

    @Override
    public void doProcess(ExcelDatum raw) throws Exception {
        List<Candlestick> candlesticks = getCandlestick();
        List<Map<String, String>> rows = new ArrayList<>();
        for (Candlestick candlestick : candlesticks) {
            Map<String, String> row = new HashMap<>();
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
            rows.add(row);
        }
        raw.setDatum(rows);
    }


}
