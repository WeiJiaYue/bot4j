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

public class SMABackTesting extends ExcelProcessor {
    public final static BarSeries BAR_SERIES = BarSeriesHolder.BAR_SERIES;
    public final static List<Double> SMA5_SERIES = new ArrayList<>();
    public final static List<Double> SMA10_SERIES = new ArrayList<>();

    //Custom params
    public final static boolean LOSS_LESS = true;
    public final static int WARMUP_COUNT = 10;
    public final static double STOP_LOSS_PERCENTAGE = 0.1;
    public final static double TAKER_FEE = 0.00040;
    public final static CandlestickInterval interval = CandlestickInterval.ONE_MINUTE;
    public static double BALANCE = 10000;
    public static double INITIAL_BALANCE = 10000;


    public static void main(String[] args) throws Exception {

        String filename = interval.val();

        //init series
        SMABackTesting processor = new SMABackTesting(SnapshotGenerator.FILE_PATH, filename + ".xls");
        processor.setNeededGenerateNewExcel(true);
        processor.setNewFileName(filename + "-backtest");
        processor.process();

    }

    private Record currentPosition;

    public SMABackTesting(String filepath, String filename) {
        super(filepath, filename);
    }


    static class Record {
        String ops;
        double point;
        double stopLoss;
        String id;
        double volume;


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
        table.addColumn("TradingVolume");
        table.addColumn("Balance");
        table.addColumn("Fee");
        table.addColumn("NetProfit");
        table.addColumn("Rate");

        int index = 0;
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
        double totalAmount = 0;

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
            //正在缩小可能变盘
//            if (diff < 0 && diff > -100) {
//                lastDiff = diff;
//                mayCrossover = true;
//            }
            //crossover
            if (ma5 > ma10 && currentPosition == null) {
                double stopLoss = getStopLoss(current.getClosePrice().doubleValue(), index);
                currentPosition = new Record();
                currentPosition.ops = "OpenLongOrder";
                currentPosition.point = current.getClosePrice().doubleValue();
                currentPosition.stopLoss = stopLoss;
                currentPosition.id = String.valueOf(index);
                currentPosition.volume = BALANCE / currentPosition.point;

                row.put("Ops", currentPosition.ops);
                row.put("Point", currentPosition.point);
                row.put("StopLoss", currentPosition.stopLoss);
                row.put("Txid", currentPosition.id);
                double openFee = BALANCE * TAKER_FEE;
                BALANCE -= openFee;
                row.put("Fee", openFee);
                row.put("NetProfit", 0 - openFee);
                row.put("Balance", BALANCE);
                row.put("TradingVolume", currentPosition.volume);
                index++;
                openCount++;
                totalVolume += currentPosition.volume;
                totalAmount += BALANCE;
                continue;
            }
            //StopLoss
            if (currentPosition != null && current.getLowPrice().doubleValue() < currentPosition.stopLoss) {
                row.put("Ops", "StopLoss");
                row.put("Point", currentPosition.stopLoss);
                row.put("StopLoss", 0);
                row.put("Txid", currentPosition.id);

                double newBalance = currentPosition.stopLoss * currentPosition.volume;
                double closeFee = newBalance * TAKER_FEE;
                double closeNetProfit = newBalance - BALANCE - closeFee;
                BALANCE = BALANCE + closeNetProfit;
                row.put("Fee", closeFee);
                row.put("NetProfit", closeNetProfit);
                row.put("Balance", BALANCE);
                row.put("TradingVolume", currentPosition.volume);
                totalVolume += currentPosition.volume;
                totalAmount += newBalance;
                index++;
                stopLossCount++;
                lossCount++;
                currentPosition = null;
                continue;
            }
            //CloseLong
            if (currentPosition != null && ma5 < ma10) {
                row.put("Ops", "CloseLongOrder");
                row.put("Point", current.getClosePrice().doubleValue());
                row.put("StopLoss", 0);
                row.put("Txid", currentPosition.id);

                double newBalance = current.getClosePrice().doubleValue() * currentPosition.volume;
                double closeFee = newBalance * TAKER_FEE;
                double closeNetProfit = newBalance - BALANCE - closeFee;
                BALANCE = BALANCE + closeNetProfit;

                row.put("Fee", closeFee);
                row.put("NetProfit", closeNetProfit);
                row.put("Balance", BALANCE);
                row.put("TradingVolume", currentPosition.volume);
                if (closeNetProfit > 0) {
                    successCount++;
                    maxNetProfit = Math.max(closeNetProfit, maxNetProfit);
                } else {
                    lossCount++;
                    maxLoss = Math.min(closeNetProfit, maxLoss);
                }
                totalVolume += currentPosition.volume;
                totalAmount += newBalance;

                closeCount++;
                currentPosition = null;
            }
            if (ma10 == ma5) {
                row.put("Ops", "Equals");
            }


            if (!row.containsKey("NetProfit")) {
                row.put("NetProfit", "0");
            }
            index++;

        }

        for (Map<String, Object> row : table.getRows()) {
            if (!row.containsKey("NetProfit")) {
                row.put("NetProfit", "0");
            }
            if (!row.containsKey("Fee")) {
                row.put("Fee", "0");
            }
            fee += Double.parseDouble(String.valueOf(row.get("Fee") == null ? "0" : row.get("Fee")));
            netProfit += Double.parseDouble(String.valueOf(row.get("NetProfit") == null ? "0" : row.get("NetProfit")));
        }

        Bar firstBar = BAR_SERIES.getFirstBar();
        Bar lastBar = BAR_SERIES.getLastBar();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println("==> Date Range:" + formatter.format(firstBar.getBeginTime()) + " ==> " + formatter.format(lastBar.getBeginTime()));


        System.out.println("==> CurrentBalance:" + BALANCE);
        System.out.println("==> StatNetProfit:" + netProfit);
        System.out.println("==> NetProfit:" + (BALANCE - INITIAL_BALANCE));
        System.out.println("==> Fee:" + fee);
        System.out.println();
        System.out.println("==> MaxNetProfit:" + maxNetProfit);
        System.out.println("==> MaxLoss:" + maxLoss);
        System.out.println("==> TotalVolume:" + totalVolume);
        System.out.println("==> TotalAmount:" + totalAmount);
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
