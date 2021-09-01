package bot.trade;

import bot.BarLivingStream;
import bot.DateUtil;
import com.alibaba.fastjson.JSON;
import com.binance.client.model.enums.CandlestickInterval;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.Scanner;
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

        //Indicator
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator sma5Indicator = new SMAIndicator(closePrice, 5);
        SMAIndicator sma10Indicator = new SMAIndicator(closePrice, 10);
        livingStream.setSma5Indicator(sma5Indicator);
        livingStream.setSma10Indicator(sma10Indicator);
        //Run
        livingStream.run();
        print("Living trading started with bar size " + barSeries.getEndIndex());
        print("Living trading started at last bar " + barSeries.getLastBar());
        //Living trade handler
        livingStream.setLastBarStream(new LastBarStream() {
            @Override
            public void onLastBar() {
                int lastIndex = barSeries.getEndIndex();
                Bar lastBar = barSeries.getLastBar();
//                double lastPrice = livingStream.getLastPrice();
                double ma5 = sma5Indicator.getValue(lastIndex).doubleValue();
                double ma10 = sma10Indicator.getValue(lastIndex).doubleValue();
                print("Latest bar " + lastBar);
                if (lastIndex < WARMUP_COUNT) {
                    return;
                }
                //Long when crossover
                if (ma5 > ma10 && currentPosition == null) {
                    double longMarketPrice = GetOrderBook.getLongMarketPrice(SYMBOL_FOR_TRADING);
                    double stopLoss = getStopLossWhenLong(barSeries, longMarketPrice, lastIndex);
                    open(livingStream, String.valueOf(lastIndex), longMarketPrice, lastBar, ma5, ma10, stopLoss);
                }
                //CloseLong
                else if (currentPosition != null && ma5 < ma10) {
                    double shortMarketPrice = GetOrderBook.getShortMarketPrice(SYMBOL_FOR_TRADING);
                    close(livingStream, OrderRecord.Ops.CloseLong, shortMarketPrice, lastBar, ma5, ma10);
                }
            }
        });
        livingStream.setLivingStream(event -> {
            double lastPrice = livingStream.getLastPrice();
            if (currentPosition != null && lastPrice < currentPosition.stopLoss) {
                double shortMarketPrice = GetOrderBook.getShortMarketPrice(SYMBOL_FOR_TRADING);
                int lastIndex = barSeries.getEndIndex();
                Bar lastBar = barSeries.getLastBar();
                double ma5 = sma5Indicator.getValue(lastIndex).doubleValue();
                double ma10 = sma10Indicator.getValue(lastIndex).doubleValue();
                close(livingStream, OrderRecord.Ops.StopLossLong, shortMarketPrice, lastBar, ma5, ma10);
            }
        });
        //Enable monitors
        enableShutdownOrderTraceMonitor(livingStream, barSeries);
        enableCLIMonitor(livingStream, barSeries);

    }


    public static OrderRecord close(BarLivingStream livingStream, OrderRecord.Ops ops, double closePrice, Bar lastBar, double ma5, double ma10) {
        OrderRecord order = OrderRecord.build();
        order.txid(currentPosition.txid)
                .ops(ops)
                .point(closePrice)
                .lastPrice(livingStream.getLastPrice())
                .stopLoss(-1)
                .volume(currentPosition.volume)
                .quantity(order.point * order.volume)
                .fee(order.quantity * TAKER_FEE)
                .profit(order.quantity - ORDER_TRACE.balance - order.fee)
                .balance(ORDER_TRACE.balance += order.profit)
                .bar(lastBar.getEndTime())
                .ma5(ma5)
                .ma10(ma10)
                .time(DateUtil.getCurrentDateTime())
                .timestamp(System.currentTimeMillis());
        ORDER_TRACE.addOrder(order);
        currentPosition = null;
        printHighlight(order.ops + " order :" + order);
        return order;
    }


    public static OrderRecord open(BarLivingStream livingStream, String txid, double openPrice, Bar lastBar, double ma5, double ma10, double stopLoss) {
        OrderRecord order = OrderRecord.build();
        order.txid(txid)
                .ops(OrderRecord.Ops.Long)
                .point(openPrice)
                .lastPrice(livingStream.getLastPrice())
                .stopLoss(stopLoss)
                .volume(ORDER_TRACE.balance / order.point)
                .fee(ORDER_TRACE.balance * TAKER_FEE)
                .quantity(ORDER_TRACE.balance)
                .profit(-order.fee)
                .balance(ORDER_TRACE.balance -= order.fee)
                .bar(lastBar.getEndTime())
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


    public static void enableShutdownOrderTraceMonitor(BarLivingStream livingStream, BarSeries barSeries) {
        Runtime.getRuntime().addShutdownHook(new Thread(
                new OrderTraceRunnable("ShutdownMonitor", ORDER_TRACE, livingStream, barSeries, true)));
    }

    public static void enableScheduledOrderTraceMonitor(BarLivingStream livingStream, BarSeries barSeries) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                new OrderTraceRunnable("ScheduledMonitor", ORDER_TRACE, livingStream, barSeries, false),
                10,
                10,
                TimeUnit.SECONDS);
    }


    public static void enableCLIMonitor(BarLivingStream livingStream, BarSeries barSeries) {
        Scanner scan = new Scanner(System.in);    //构造Scanner类的对象scan，接收从控制台输入的信息
        while (scan.hasNextLine()) {
            try {
                String instruction = scan.nextLine();
                if (StringUtils.isBlank(instruction)) {
                    continue;
                }
                if ("help".equalsIgnoreCase(instruction)) {
                    System.err.println("Current supported instructions are:");
                    System.err.println("Snapshot");
                    System.err.println("SnapshotDump");
                    System.err.println("PeekSize");
                    System.err.println("PeekOrders");
                } else if ("Snapshot".equalsIgnoreCase(instruction)) {
                    new Thread(new OrderTraceRunnable("CLIMonitor", ORDER_TRACE, livingStream, barSeries, false)).start();
                } else if ("SnapshotDump".equalsIgnoreCase(instruction)) {
                    new Thread(new OrderTraceRunnable("CLIMonitor", ORDER_TRACE, livingStream, barSeries, true)).start();
                } else if ("PeekSize".equalsIgnoreCase(instruction)) {
                    printHighlight("Current trading order size : :" + ORDER_TRACE.getOrders().size());
                } else if ("PeekOrders".equalsIgnoreCase(instruction)) {
                    printHighlight("Current trading orders :" + JSON.toJSONString(ORDER_TRACE.getOrders()));
                } else {
                    System.err.println("Wrong instruction!!!");
                }
            } catch (Exception e) {
                printHighlight("CLI exception!!!");
                e.printStackTrace();
            }
        }
    }


}
