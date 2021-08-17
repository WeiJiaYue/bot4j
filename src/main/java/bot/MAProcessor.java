package bot;

import bot.excel.ExcelDatum;
import bot.excel.ExcelProcessor;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MAProcessor extends ExcelProcessor {


    public static BarSeries series = new BaseBarSeriesBuilder().build();
    public static List<BigDecimal> sma5Results = new ArrayList<>();
    public static List<BigDecimal> sma10Results = new ArrayList<>();

    public MAProcessor(String filepath, String filename) {
        super(filepath, filename);
    }

    @Override
    public void doProcess(ExcelDatum raw) throws Exception {
        raw.getHeaders().add("SMA5");
        raw.getHeaders().add("SMA10");
        List<Map<String, String>> rows = raw.getDatum();
        int index = 0;
        for (Map<String, String> row : rows) {
            BigDecimal open = new BigDecimal(row.get("Open"));
            BigDecimal high = new BigDecimal(row.get("High"));
            BigDecimal low = new BigDecimal(row.get("Low"));
            BigDecimal close = new BigDecimal(row.get("Close"));
            BigDecimal volume = new BigDecimal(row.get("Volume"));
            String timestamp = row.get("Timestamp");

            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
                    new Date(Long.parseLong(timestamp)).toInstant(), ZoneId.systemDefault());
            series.addBar(zonedDateTime, open, high, low, close, volume);


            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
            SMAIndicator sma10 = new SMAIndicator(closePrice, 10);



            row.put("SMA5",String.valueOf(sma5.getValue(index)));
            row.put("SMA10",String.valueOf(sma10.getValue(index)));
            index++;
        }
    }

    public static void main(String[] args) throws Exception {

        //init series
        MAProcessor processor = new MAProcessor(ExcelProcessor.filePath, SnapshotGenerator.snapshot_file_name);
        processor.process();



    }


}
