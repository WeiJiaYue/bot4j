package bot.trade;

import bot.BarLivingStream;
import bot.DateUtil;
import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static bot.DateUtil.print;
import static bot.DateUtil.printHighlight;

public class SMALivingTest {

    //Custom params
    public final static boolean LOSS_LESS = true;
    public final static int WARMUP_COUNT = 10;
    public final static double STOP_LOSS_PERCENTAGE = 0.1;
    public final static double TAKER_FEE = 0.00040;
    public final static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    public final static String SYMBOL_FOR_TRADING = "BTCUSDT";
    public final static int HISTORY_KLINE_COUNT = 100;
    public final static double INIT_BALANCE = 100;
    private final static OrderTrace ORDER_TRACE = new OrderTrace(INIT_BALANCE);

    private static OrderRecord currentPosition;

    public static void main(String[] args) throws Exception {
        BarLivingStream livingStream = new BarLivingStream(INTERVAL, SYMBOL_FOR_TRADING, HISTORY_KLINE_COUNT);
        BarSeries barSeries = livingStream.getBarSeries();
        //Enable monitors
        enableScheduledOrderTraceMonitor(barSeries);
        enableShutdownOrderTraceMonitor(barSeries);

        //Indicator
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator sma5Indicator = new SMAIndicator(closePrice, 5);
        SMAIndicator sma10Indicator = new SMAIndicator(closePrice, 10);
        //Run
        livingStream.run();
        print("Living trading started with bar size " + barSeries.getEndIndex() + " at last bar " + barSeries.getLastBar());
        //Living trade handler
        livingStream.setLastBarStream(new LastBarStream() {
            @Override
            public void onLastBar() {
                int lastIndex = barSeries.getEndIndex();
                Bar lastBar = barSeries.getLastBar();
                double lastPrice = livingStream.getLastPrice();
                double ma5 = sma5Indicator.getValue(lastIndex).doubleValue();
                double ma10 = sma10Indicator.getValue(lastIndex).doubleValue();
                print("Taker price " + lastPrice + " at latest kline " + lastBar);
                if (lastIndex < WARMUP_COUNT) {
                    return;
                }
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
            double lastPrice = livingStream.getLastPrice();
            if (currentPosition != null && livingStream.getLastPrice() < currentPosition.stopLoss) {
                int lastIndex = barSeries.getEndIndex();
                Bar lastBar = barSeries.getLastBar();
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
                .profit(order.quantity - ORDER_TRACE.getBalance() - order.fee);
        ORDER_TRACE.balanceChange(order.profit);
        order.balance(ORDER_TRACE.getBalance())
                .bar(lastBar)
                .ma5(ma5)
                .ma10(ma10)
                .time(DateUtil.getCurrentDateTime())
                .timestamp(System.currentTimeMillis());
        ORDER_TRACE.addOrder(order);
        currentPosition = null;
        printHighlight(order.ops + " order :" + order);
        return order;
    }


    public static OrderRecord open(String txid, double openPrice, Bar lastBar, double ma5, double ma10, double stopLoss) {
        OrderRecord order = OrderRecord.build();
        order.txid(txid)
                .ops(OrderRecord.Ops.Long)
                .point(openPrice)
                .stopLoss(stopLoss)
                .volume(ORDER_TRACE.BALANCE / order.point)
                .fee(ORDER_TRACE.BALANCE * TAKER_FEE)
                .quantity(ORDER_TRACE.BALANCE)
                .profit(-order.fee)
                .balance(ORDER_TRACE.BALANCE -= order.fee)
                .bar(lastBar)
                .ma5(ma5)
                .ma10(ma10)
                .time(DateUtil.getCurrentDateTime())
                .timestamp(System.currentTimeMillis());
        ORDER_TRACE.addOrder(order);
        currentPosition = order;
        printHighlight(order.ops + " order :" + order);
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


    public static void enableShutdownOrderTraceMonitor(BarSeries barSeries) {
        Runtime.getRuntime().addShutdownHook(new Thread(new OrderTraceRunnable("ShutdownMonitor", ORDER_TRACE, barSeries)));
    }

    public static void enableScheduledOrderTraceMonitor(BarSeries barSeries) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                new OrderTraceRunnable("ScheduledMonitor", ORDER_TRACE, barSeries),
                10,
                10,
                TimeUnit.SECONDS);
    }


}
