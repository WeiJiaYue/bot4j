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

public class BackingTest {

    static BarSeries series = new BaseBarSeriesBuilder().build();


    static List<BigDecimal> sma5Results = new ArrayList<>();
    static List<BigDecimal> sma10Results = new ArrayList<>();


    public static void main(String[] args) throws Exception {

        //init series
        ExcelProcessor processor = new ExcelProcessor(ExcelProcessor.filePath, SnapshotGenerator.snapshot_file_name) {
            @Override
            public void doProcess(ExcelDatum raw) throws Exception {
                List<Map<String, String>> rows = raw.getDatum();
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
                }
            }
        };
        processor.setNeededGenerateNewExcel(false);
        processor.process();

        //within an indicator:
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // Getting the simple moving average (SMA) of the close price over the last 5 ticks
        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);


        for (int i = 1; i <= series.getEndIndex(); i++) {
            if (i < 4) {
                sma5Results.add(BigDecimal.ZERO);
                sma10Results.add(BigDecimal.ZERO);
                continue;
            }
            sma5.getValue(i);
            sma10.getValue(i);
        }


        System.out.println(series);


    }


}
