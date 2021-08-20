package bot;

import bot.excel.ExcelProcessor;
import bot.excel.ExcelTable;
import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MAProcessor extends ExcelProcessor {
    public final static BarSeries BAR_SERIES = BarSeriesHolder.BAR_SERIES;
    public final static List<Double> SMA5_SERIES = new ArrayList<>();
    public final static List<Double> SMA10_SERIES = new ArrayList<>();

    //Custom params
    public final static boolean LOSS_LESS = true;
    public final static int WARMUP_COUNT = 10;
    public final static double STOP_LOSS_PERCENTAGE = 0.1;
    public final static double TAKER_FEE = 0.000360;
    public final static CandlestickInterval interval = CandlestickInterval.DAILY;


    public static void main(String[] args) throws Exception {

        String filename = interval.val();

        //init series
        MAProcessor processor = new MAProcessor(SnapshotGenerator.FILE_PATH, filename + ".xls");
        processor.setNeededGenerateNewExcel(false);
        processor.setNewFileName(filename + "-backtest");
        processor.process();

    }


    private double lastDiff;
    private Record lastPosition;
    private Record currentPosition;

    public MAProcessor(String filepath, String filename) {
        super(filepath, filename);
    }


    static class Record {
        String ops;
        double point;
        double stopLoss;
        double profit;
        String id;

        public Record(String ops, double point, double stopLoss, double profit, String id) {
            this.ops = ops;
            this.point = point;
            this.stopLoss = stopLoss;
            this.profit = profit;
            this.id = id;
        }
    }

    private double getStopLoss(double point, int index) {
        int stopLossIdx = index;
        Bar stopLossBar = BAR_SERIES.getBar(--stopLossIdx);
        double stopLoss = stopLossBar.getLowPrice().doubleValue();
        for (int i = stopLossIdx; i > stopLossIdx - 5; i--) {
            Bar pre = BAR_SERIES.getBar(i);
            double other = pre.getLowPrice().doubleValue();
            stopLoss = Math.min(stopLoss, other);
        }
        if (LOSS_LESS) {
            stopLoss = Math.max(stopLoss, point * (1 - STOP_LOSS_PERCENTAGE));
        } else {
            stopLoss = Math.min(stopLoss, point * (1 - STOP_LOSS_PERCENTAGE));
        }


        return stopLoss;
    }


    @Override
    public void doProcess(ExcelTable table) throws Exception {
        table.addColumn("SMA5");
        table.addColumn("SMA10");
        table.addColumn("Diff");
        table.addColumn("Ops");
        table.addColumn("Point");
        table.addColumn("StopLoss");
        table.addColumn("Txid");
        table.addColumn("Profit");
        table.addColumn("Fee");
        table.addColumn("NetProfit");
        table.addColumn("Rate");

        int index = 0;
        double profit = 0;
        double fee = 0;
        double netProfit = 0;
        int openCount = 0;
        int closeCount = 0;
        int stopLossCount = 0;
        int successCount = 0;
        int lossCount = 0;
        double maxNetProfit = 0;
        double maxLoss = 0;
        double totalVolume = 0;

        for (Map<String, Object> row : table.getRows()) {
            BarSeriesHolder.load(row);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(BAR_SERIES);
            SMAIndicator sma5Indicator = new SMAIndicator(closePrice, 5);
            SMAIndicator sma10Indicator = new SMAIndicator(closePrice, 10);
            double ma5 = sma5Indicator.getValue(index).doubleValue();
            double ma10 = sma10Indicator.getValue(index).doubleValue();
            table.updateRow(row, "SMA5", ma5);
            table.updateRow(row, "SMA10", ma10);
            table.updateRow(row, "Rate", ma10 / ma5);
            SMA5_SERIES.add(ma5);
            SMA10_SERIES.add(ma10);
            if (index < WARMUP_COUNT) {
                index++;
                continue;
            }
            Bar current = BAR_SERIES.getBar(index);

            double diff = ma5 - ma10;
            table.updateRow(row, "Diff", diff);
            boolean mayCrossover = false;

            //正在缩小可能变盘
//            if (diff < 0 && diff > -100) {
//                lastDiff = diff;
//                mayCrossover = true;
//            }

            //May crossover
            if (ma5 > ma10 && currentPosition == null) {
                double stopLoss = getStopLoss(current.getOpenPrice().doubleValue(), index);
                currentPosition = new Record("OpenLong", current.getOpenPrice().doubleValue(), stopLoss, 0, String.valueOf(index));
                row.put("Ops", currentPosition.ops);
                row.put("Point", currentPosition.point);
                row.put("StopLoss", currentPosition.stopLoss);
                row.put("Txid", currentPosition.id);
                row.put("Profit", currentPosition.profit);
                double openFee = currentPosition.point * TAKER_FEE;
                row.put("Fee", openFee);
                row.put("NetProfit", currentPosition.profit - openFee);
                index++;
                openCount++;
                totalVolume += currentPosition.point;
                continue;
            }
            //StopLoss
            if (currentPosition != null && current.getLowPrice().doubleValue() < currentPosition.stopLoss) {
                row.put("Ops", "StopLoss");
                row.put("Point", String.valueOf(currentPosition.stopLoss));
                row.put("StopLoss", "DoStopLoss");
                row.put("Txid", currentPosition.id);

                double closeProfit = currentPosition.stopLoss - currentPosition.point;
                double closeFee = currentPosition.point * TAKER_FEE;

                row.put("Profit", String.valueOf(closeProfit));
                row.put("Fee", closeFee);
                row.put("NetProfit", closeProfit - closeFee);
                totalVolume += currentPosition.stopLoss;
                index++;
                stopLossCount++;
                lossCount++;
                currentPosition = null;
                continue;
            }
            //CloseLong
            if (currentPosition != null && ma5 < ma10) {
                row.put("Ops", "CloseLong");
                row.put("Point", String.valueOf(current.getOpenPrice().doubleValue()));
                row.put("StopLoss", "0");
                row.put("Txid", currentPosition.id);
                double closeProfit = current.getOpenPrice().doubleValue() - currentPosition.point;
                double closeFee = current.getOpenPrice().doubleValue() * TAKER_FEE;
                row.put("Profit", String.valueOf(closeProfit));
                row.put("Fee", closeFee);
                double closeNetProfit = closeProfit - closeFee;
                row.put("NetProfit", closeNetProfit);

                if (closeNetProfit > 0) {
                    successCount++;
                    maxNetProfit = Math.max(closeNetProfit, maxNetProfit);
                } else {
                    lossCount++;
                    maxLoss = Math.min(closeNetProfit, maxLoss);
                }
                totalVolume += current.getOpenPrice().doubleValue();
                closeCount++;
                currentPosition = null;
            }
            if (ma10 == ma5) {
                row.put("Ops", "Equals");
            }


            if (!row.containsKey("Profit")) {
                row.put("Profit", "0");
            }
            index++;
            profit += Double.parseDouble(String.valueOf(row.get("Profit") == null ? "0" : row.get("Profit")));
            fee += Double.parseDouble(String.valueOf(row.get("Fee") == null ? "0" : row.get("Fee")));
            netProfit += Double.parseDouble(String.valueOf(row.get("NetProfit") == null ? "0" : row.get("NetProfit")));
        }

        Bar firstBar = BAR_SERIES.getFirstBar();
        Bar lastBar = BAR_SERIES.getLastBar();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println("==> Date Range:" + formatter.format(firstBar.getBeginTime()) + " ==> " + formatter.format(lastBar.getBeginTime()));

        System.out.println("==> Profit:" + profit);
        System.out.println("==> Fee:" + fee);
        System.out.println("==> NetProfit:" + netProfit);
        System.out.println();
        System.out.println("==> MaxNetProfit:" + maxNetProfit);
        System.out.println("==> MaxLoss:" + maxLoss);
        System.out.println("==> TotalVolume:" + totalVolume);
        System.out.println("==> ReturnFee:" + fee * 0.2);

        System.out.println("==> OpenCount:" + openCount);
        System.out.println("==> CloseCount:" + closeCount);
        System.out.println("==> StopLossCount:" + stopLossCount);

        System.out.println("==> SuccessCount:" + successCount);
        System.out.println("==> LossCount:" + lossCount);
        double rate = (successCount * 1.00) / (openCount * 1.00);
        System.out.println("==> SuccessRate:" + (rate * 100) + "%");


    }


}
