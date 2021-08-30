package bot.trade;

import bot.BarSeriesStream;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.event.CandlestickEvent;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;


public class SMAStrategy {

    //Custom params
    public final static boolean LOSS_LESS = true;
    public final static int WARMUP_COUNT = 10;
    public final static double STOP_LOSS_PERCENTAGE = 0.1;
    public final static double TAKER_FEE = 0.00040;
    public final static CandlestickInterval interval = CandlestickInterval.ONE_MINUTE;
    public final static String symbol = "BTCUSDT";
    public final static int initKLineCount = 1000;
    public static double BALANCE = 10000;
    public static double INITIAL_BALANCE = 10000;
    private static Record currentPosition;
    private static volatile boolean firstCrossOver;
    private final static Stats stats = new Stats();


    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println(stats);
        }));

        BarSeriesStream barSeriesStream = new BarSeriesStream(interval, symbol, initKLineCount);


        barSeriesStream.setLastBarStream(BAR_SERIALS -> {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(BAR_SERIALS);
            SMAIndicator sma5Indicator = new SMAIndicator(closePrice, 5);
            SMAIndicator sma10Indicator = new SMAIndicator(closePrice, 10);
            int lastIndex = BAR_SERIALS.getEndIndex();

            double ma5 = sma5Indicator.getValue(lastIndex).doubleValue();
            double ma10 = sma10Indicator.getValue(lastIndex).doubleValue();
            if (lastIndex < WARMUP_COUNT) {
                return;
            }
            System.out.println();
            System.out.println("==> Cyclic run strategy");

            //crossover
            if (ma5 > ma10 && currentPosition == null) {
                double stopLoss = getStopLoss(BAR_SERIALS, barSeriesStream.getLastPrice(), lastIndex);
                currentPosition = new Record();
                currentPosition.ops = "OpenLongOrder";
                currentPosition.point = barSeriesStream.getLastPrice();
                currentPosition.stopLoss = stopLoss;
                currentPosition.id = String.valueOf(BAR_SERIALS.getEndIndex());
                currentPosition.volume = BALANCE / currentPosition.point;
                double fee = BALANCE * TAKER_FEE;
                BALANCE -= fee;
                stats.openCount++;
                //
                stats.volume += currentPosition.volume;
                stats.quantity += BALANCE;
                stats.fee += fee;
                stats.profit = stats.profit - fee;

                System.out.println("==> " + currentPosition.ops + ",OpenPrice:" + currentPosition.point
                        + ",StopLoss:" + currentPosition.stopLoss + ",Volume:" + currentPosition.volume
                        + "Txid:" + currentPosition.id + ",Fee:" + fee);

                System.out.println(stats);

            }
            //CloseLong
            else if (currentPosition != null && ma5 < ma10) {
                double quantity = barSeriesStream.getLastPrice() * currentPosition.volume;
                double fee = quantity * TAKER_FEE;
                double profit = quantity - BALANCE - fee;
                BALANCE = BALANCE + profit;
                if (profit > 0) {
                    stats.successCount++;
                    stats.maxProfit = Math.max(profit, stats.maxProfit);
                } else {
                    stats.lossCount++;
                    stats.maxLoss = Math.min(profit, stats.maxLoss);
                }
                stats.closeCount++;
                //
                stats.volume += currentPosition.volume;
                stats.quantity += quantity;
                stats.fee += fee;
                stats.profit = stats.profit - fee;

                System.out.println("==> " + "CloseLong" + ",OpenPrice:" + barSeriesStream.getLastPrice()
                        + ",StopLoss:" + "Nil" + ",Volume:" + currentPosition.volume
                        + "Txid:" + currentPosition.id + ",Fee:" + fee);

                System.out.println(stats);


                currentPosition = null;
            }
        });


        barSeriesStream.setLivingStream(new LivingStream() {
            @Override
            public void onLiving(CandlestickEvent event) {
                if (currentPosition != null && barSeriesStream.getLastPrice() < currentPosition.stopLoss) {
                    double quantity = barSeriesStream.getLastPrice() * currentPosition.volume;
                    double fee = quantity * TAKER_FEE;
                    double profit = quantity - BALANCE - fee;
                    BALANCE = BALANCE + profit;
                    stats.closeCount++;
                    stats.stopLossCount++;
                    //
                    stats.volume += currentPosition.volume;
                    stats.quantity += quantity;
                    stats.fee += fee;
                    stats.profit = stats.profit - fee;

                    System.out.println("==> " + "StopLoss" + ",OpenPrice:" + barSeriesStream.getLastPrice()
                            + ",StopLoss:" + "Nil" + ",Volume:" + currentPosition.volume
                            + "Txid:" + currentPosition.id + ",Fee:" + fee);
                    System.out.println(stats);

                    currentPosition = null;
                }
            }
        });

        barSeriesStream.run();
    }



    private void openLongOrder(){

    }


    private static double getStopLoss(BarSeries barSeries, double point, int index) {
        int stopLossIdx = index;
        Bar stopLossBar = barSeries.getBar(--stopLossIdx);
        double stopLoss = stopLossBar.getLowPrice().doubleValue();
        for (int i = stopLossIdx; i > stopLossIdx - 5; i--) {
            Bar pre = barSeries.getBar(i);
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


    static class Record {
        String ops;
        double point;
        double stopLoss;
        String id;
        double volume;
    }
}
