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

    @Override
    public void doProcess(ExcelDatum raw) throws Exception {
        raw.getHeaders().add("SMA5");
        raw.getHeaders().add("SMA10");
        raw.getHeaders().add("Ops");
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
            Num sma5Value = sma5.getValue(index);
            Num sma10Value = sma10.getValue(index);
            row.put("SMA5", String.valueOf(sma5Value));
            row.put("SMA10", String.valueOf(sma10Value));
            sma5Results.add(sma5Value.doubleValue());
            sma10Results.add(sma10Value.doubleValue());

            if (index >= 10) {
                double ma5 = sma5Value.doubleValue();
                double ma10 = sma10Value.doubleValue();


                if (ma10 > ma5) {
                    System.out.println("==> 开多并且平空");
                    int stopLossIdx = index;
                    double stopLoss = series.getBar(stopLossIdx--).getLowPrice().doubleValue();
                    for (int i = stopLossIdx; i > stopLossIdx - 5; i--) {
                        double other = series.getBar(i).getLowPrice().doubleValue();
                        stopLoss = Math.min(stopLoss, other);
                    }
                    Bar current = series.getBar(index);

                    System.out.println("==> 开多点位：" + current.getOpenPrice().doubleValue() + " 止损点：" + stopLoss);


                }
                if (ma10 == ma5) {
//                    System.out.println("待定");
                }
                if (ma10 < ma5) {
//                    System.out.println("开空且平多");
                }
            }

            index++;
        }
    }

    public static void main(String[] args) throws Exception {

        //init series
        MAProcessor processor = new MAProcessor(ExcelProcessor.filePath, SnapshotGenerator.snapshot_file_name);
        processor.setNeededGenerateNewExcel(false);
        processor.process();

//        System.out.println(sma5Results);
//        System.out.println(sma10Results);

//
//        for (int i = 10; i < sma10Results.size(); i++) {
//            double ma5 = sma5Results.get(i);
//            double ma10 = sma10Results.get(i);
//
//            if (ma10 > ma5) {
//                System.out.println("多并且平空");
//            }
//            if (ma10 == ma5) {
//                System.out.println("待定");
//            }
//            if (ma10 < ma5) {
//                System.out.println("开空且平多");
//            }
//
//        }


    }


}
