package bot;

import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.ArrayList;
import java.util.List;

public class SMAStrategy {
    public final static BarSeries BAR_SERIES = BarSeriesStream.BAR_SERIES;
    public final static List<Double> SMA5_SERIES = new ArrayList<>();
    public final static List<Double> SMA10_SERIES = new ArrayList<>();

    //Custom params
    public final static boolean LOSS_LESS = false;
    public final static int WARMUP_COUNT = 10;
    public final static double STOP_LOSS_PERCENTAGE = 0.1;
    public final static double TAKER_FEE = 0.00040;
    public final static CandlestickInterval interval = CandlestickInterval.ONE_MINUTE;
    public static double BALANCE = 10000;
    public static double INITIAL_BALANCE = 10000;
    private static Record currentPosition;


    public static void main(String[] args) throws Exception {

        new Thread(() -> BarSeriesStream.main(args)).start();

        while(true){
            if(!BarSeriesStream.LIVING){
                System.out.println("==> Waiting for living stream");
                Thread.sleep(1000);
                continue;
            }
            System.out.println("==> Living stream already prepared");

            ClosePriceIndicator closePrice = new ClosePriceIndicator(BAR_SERIES);
            SMAIndicator sma5Indicator = new SMAIndicator(closePrice, 5);
            SMAIndicator sma10Indicator = new SMAIndicator(closePrice, 10);

            double ma5 = sma5Indicator.getValue(BAR_SERIES.getEndIndex()).doubleValue();
            double ma10 = sma10Indicator.getValue(BAR_SERIES.getEndIndex()).doubleValue();

            //crossover
            if (ma5 > ma10 && currentPosition == null) {
                double stopLoss = getStopLoss(current.getOpenPrice().doubleValue(), index);
                currentPosition = new Record();
                currentPosition.ops = "OpenLongOrder";
                currentPosition.point = current.getOpenPrice().doubleValue();
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
                row.put("Point", current.getOpenPrice().doubleValue());
                row.put("StopLoss", 0);
                row.put("Txid", currentPosition.id);

                double newBalance = current.getOpenPrice().doubleValue() * currentPosition.volume;
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


    }


    static class Record {
        String ops;
        double point;
        double stopLoss;
        String id;
        double volume;


    }

    private static double getStopLoss(double point, int index) {
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



}
