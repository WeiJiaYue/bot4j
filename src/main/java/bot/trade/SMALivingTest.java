package bot.trade;

import bot.BarLivingStream;
import bot.DateUtil;
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
    public final static int HISTORY_KLINE_COUNT = 100;
    public static double BALANCE = 10000;
    private final static Stats STATS = new Stats();

    private static OrderRecord currentPosition;

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println(STATS);
        }));
        BarLivingStream livingStream = new BarLivingStream(INTERVAL, SYMBOL_FOR_TRADING, HISTORY_KLINE_COUNT);

        BarSeries barSeries = livingStream.getBarSeries();

        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator sma5Indicator = new SMAIndicator(closePrice, 5);
        SMAIndicator sma10Indicator = new SMAIndicator(closePrice, 10);
        livingStream.run();
        System.out.println();
        System.out.println("==> Started at " + DateUtil.getCurrentDateTime() + " with bar size " + barSeries.getEndIndex());
        System.out.println("==> Start current bar :" + barSeries.getLastBar());

        livingStream.setLastBarStream(new LastBarStream() {
            @Override
            public void onLastBar() {


                int lastIndex = barSeries.getEndIndex();
                Bar lastBar = barSeries.getLastBar();
                double lastPrice = livingStream.getLastPrice();
                double ma5 = sma5Indicator.getValue(lastIndex).doubleValue();
                double ma10 = sma10Indicator.getValue(lastIndex).doubleValue();

                System.out.println("==> Current Bar :" + lastBar);


                //Long when crossover
                if (ma5 > ma10 && currentPosition == null) {
                    double stopLoss = getStopLossWhenLong(barSeries, lastPrice, lastIndex);
                    open(String.valueOf(lastIndex), lastPrice, lastBar, ma5, ma10, stopLoss);
                }
                //CloseLong
                else if (currentPosition != null && ma5 < ma10) {
                    close(OrderRecord.Ops.CloseLong, lastPrice, lastBar, ma5, ma10);
                }
            }
        });
        livingStream.setLivingStream(event -> {
            if (currentPosition != null && livingStream.getLastPrice() < currentPosition.stopLoss) {
                int lastIndex = barSeries.getEndIndex();
                Bar lastBar = barSeries.getLastBar();
                double lastPrice = livingStream.getLastPrice();
                double ma5 = sma5Indicator.getValue(lastIndex).doubleValue();
                double ma10 = sma10Indicator.getValue(lastIndex).doubleValue();
                close(OrderRecord.Ops.StopLossLong, lastPrice, lastBar, ma5, ma10);
            }
        });


    }


    public static OrderRecord close(OrderRecord.Ops ops, double closePrice, Bar lastBar, double ma5, double ma10) {
        OrderRecord order = currentPosition;
        order.txid(currentPosition.txid)
                .ops(ops)
                .point(closePrice)
                .stopLoss(-1)
                .volume(currentPosition.volume)
                .quantity(order.point * order.volume)
                .fee(order.quantity * TAKER_FEE)
                .profit(order.quantity - BALANCE - order.fee)
                .balance(BALANCE += order.profit)
                .bar(lastBar)
                .ma5(ma5)
                .ma10(ma10);
        STATS.addOrder(order);
        currentPosition = null;

        System.out.println("==> Close by " + ops + " in order :" + order);
        return order;
    }


    public static OrderRecord open(String txid, double openPrice, Bar lastBar, double ma5, double ma10, double stopLoss) {
        OrderRecord order = OrderRecord.build();
        order.txid(txid)
                .ops(OrderRecord.Ops.Long)
                .point(openPrice)
                .stopLoss(stopLoss)
                .volume(BALANCE / order.point)
                .fee(BALANCE * TAKER_FEE)
                .quantity(BALANCE)
                .profit(-order.fee)
                .balance(BALANCE -= order.fee)
                .bar(lastBar)
                .ma5(ma5)
                .ma10(ma10);
        STATS.addOrder(order);
        currentPosition = order;
        System.out.println("==> Open by " + order.ops + " in order :" + order);
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
