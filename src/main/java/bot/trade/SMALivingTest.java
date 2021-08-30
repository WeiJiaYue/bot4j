package bot.trade;

import bot.BarSeriesStream;
import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

public class SMALivingTest {

    //Custom params
    public final static boolean LOSS_LESS = true;
    public final static int WARMUP_COUNT = 10;
    public final static double STOP_LOSS_PERCENTAGE = 0.1;
    public final static double TAKER_FEE = 0.00040;
    public final static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    public final static String SYMBOL_FOR_TRADING = "BTCUSDT";
    public final static int HISTORY_KLINE_COUNT = 1000;
    public static double BALANCE = 10000;
    private final static Stats STATS = new Stats();

    private static OrderRecord currentPosition;

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println(STATS);
        }));
        BarSeriesStream barSeriesStream = new BarSeriesStream(INTERVAL, SYMBOL_FOR_TRADING, HISTORY_KLINE_COUNT);
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
                double stopLoss = getStopLossWhenLong(BAR_SERIALS, barSeriesStream.getLastPrice(), lastIndex);
                openLong(barSeriesStream, BAR_SERIALS, stopLoss);
            }
            //CloseLong
            else if (currentPosition != null && ma5 < ma10) {
                closeLong(OrderRecord.Ops.CloseLong, barSeriesStream);
            }
        });
        barSeriesStream.setLivingStream(event -> {
            if (currentPosition != null && barSeriesStream.getLastPrice() < currentPosition.stopLoss) {
                closeLong(OrderRecord.Ops.StopLossLong, barSeriesStream);
            }
        });

        barSeriesStream.run();
    }


    public static OrderRecord closeLong(OrderRecord.Ops ops, BarSeriesStream barSeriesStream) {
        OrderRecord order = currentPosition;
        order.txid(currentPosition.txid)
                .ops(ops)
                .point(barSeriesStream.getLastPrice())
                .stopLoss(-1)
                .volume(currentPosition.volume)
                .quantity(order.point * order.volume)
                .fee(order.quantity * TAKER_FEE)
                .profit(order.quantity - BALANCE - order.fee)
                .balance(BALANCE += order.profit);
        STATS.addOrder(order);
        currentPosition = null;
        return order;
    }


    public static OrderRecord openLong(BarSeriesStream barSeriesStream, BarSeries BAR_SERIALS, double stopLoss) {
        OrderRecord order = OrderRecord.build();
        order.txid(String.valueOf(BAR_SERIALS.getEndIndex()))
                .ops(OrderRecord.Ops.Long)
                .point(barSeriesStream.getLastPrice())
                .stopLoss(stopLoss)
                .volume(BALANCE / order.point)
                .fee(BALANCE * TAKER_FEE)
                .quantity(BALANCE - order.fee)
                .profit(-order.fee)
                .balance(BALANCE -= order.fee);
        STATS.addOrder(order);
        currentPosition = order;
        return order;
    }


    public static double getStopLossWhenLong(BarSeries barSeries, double point, int index) {
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


}
