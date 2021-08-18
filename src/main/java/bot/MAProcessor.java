package bot;

import bot.excel.ExcelDatum;
import bot.excel.ExcelProcessor;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MAProcessor extends ExcelProcessor {


    public static BarSeries series = new BaseBarSeriesBuilder().build();
    public static List<Double> sma5Results = new ArrayList<>();
    public static List<Double> sma10Results = new ArrayList<>();

    public MAProcessor(String filepath, String filename) {
        super(filepath, filename);
    }


    private Record record;


    class Record {
        String ops;
        double point;
        double stopLoss;
        String id;
    }

    @Override
    public void doProcess(ExcelDatum raw) throws Exception {
        raw.getHeaders().add("SMA5");
        raw.getHeaders().add("SMA10");
        raw.getHeaders().add("Ops");
        raw.getHeaders().add("Point");
        raw.getHeaders().add("StopLoss");
        raw.getHeaders().add("Txid");
        raw.getHeaders().add("Profit");
        raw.getHeaders().add("ma10/ma5");

        List<Map<String, String>> rows = raw.getDatum();
        int index = 0;
        double bigest = 0;
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
            Num sma5Value = sma5.getValue(index);
            Num sma10Value = sma10.getValue(index);
            row.put("SMA5", String.valueOf(sma5Value));
            row.put("SMA10", String.valueOf(sma10Value));
            sma5Results.add(sma5Value.doubleValue());
            sma10Results.add(sma10Value.doubleValue());

            if (index >= 10) {
                double ma5 = sma5Value.doubleValue();
                double ma10 = sma10Value.doubleValue();
                row.put("ma10/ma5", String.valueOf(ma10 / ma5));

                bigest = Math.max(bigest, ma10 / ma5);
                if (ma10 > ma5) {
                    if (record == null) {
                        int stopLossIdx = index;
                        Bar stopLossBar = series.getBar(--stopLossIdx);
                        double stopLoss = stopLossBar.getLowPrice().doubleValue();
                        for (int i = stopLossIdx; i > stopLossIdx - 5; i--) {
                            Bar pre = series.getBar(i);
                            double other = pre.getLowPrice().doubleValue();
                            stopLoss = Math.min(stopLoss, other);
                        }
                        Bar current = series.getBar(index);
                        record = new Record();
                        record.ops = "Long";
                        record.point = current.getOpenPrice().doubleValue();
                        record.stopLoss = stopLoss;
                        record.id = String.valueOf(index);

                        row.put("Ops", "OpenLong");
                        row.put("Point", String.valueOf(current.getOpenPrice().doubleValue()));
                        row.put("StopLoss", String.valueOf(stopLoss));
                        row.put("Txid", String.valueOf(index));
                        row.put("Profit", "0");
                        index++;
                        continue;
                    }
                }
                //StopLoss
                Bar current1 = series.getBar(index);
                if (record != null && current1.getLowPrice().doubleValue() < record.stopLoss) {
                    row.put("Ops", "StopLoss");
                    row.put("Point", String.valueOf(record.stopLoss));
                    row.put("StopLoss", "DoStopLoss");
                    row.put("Txid", record.id);
                    row.put("Profit", String.valueOf(record.stopLoss - record.point));
                    record = null;
                    index++;
                    continue;
                }
                //CloseLong
                if (ma10 < ma5) {
                    if (record != null) {
                        Bar current = series.getBar(index);
                        row.put("Ops", "CloseLong");
                        row.put("Point", String.valueOf(current.getOpenPrice().doubleValue()));
                        row.put("StopLoss", "0");
                        row.put("Txid", record.id);
                        row.put("Profit", String.valueOf(current.getOpenPrice().doubleValue() - record.point));
                        record = null;
                    }
                }
                if (ma10 == ma5) {
                    row.put("Ops", "Equals");
                }
            }

            if (!row.containsKey("Profit")) {
                row.put("Profit", "0");
            }
            index++;
        }

        System.out.println("bigest :" + bigest);
    }

    public static void main(String[] args) throws Exception {

        //init series
        MAProcessor processor = new MAProcessor(ExcelProcessor.filePath, SnapshotGenerator.MINUTES_30);
//        processor.setNeededGenerateNewExcel(false);
        processor.setNewFilename(SnapshotGenerator.MINUTES_30 + "_test.xls");
        processor.process();


    }


}
