package com.deepinblog.bot.framework;

import com.deepinblog.bot.utils.DateUtil;
import com.alibaba.fastjson.JSON;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.OrderBook;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.deepinblog.bot.utils.Constants.TAKER_FEE;
import static com.deepinblog.bot.utils.DateUtil.print;
import static com.deepinblog.bot.utils.DateUtil.printHighlight;

/**
 * Created by louisyuu on 2021/9/3 2:38 下午
 */
public class TradingHelper {

    static SyncRequestClient restClient = SyncRequestClient.create();


    public static OrderRecord open(boolean isStream, OrderRecord.Ops ops, OrderTrace orderTrace,
                                   String txid, double openPrice, double stopLoss, double lastPrice,
                                   ZonedDateTime endTime) {
        OrderRecord order = OrderRecord.build();
        order.txid(txid)
                .ops(ops)
                .point(openPrice)
                .stopLoss(stopLoss)
                .lastPrice(lastPrice)
                .volume(orderTrace.balance / order.point)
                .fee(orderTrace.balance * TAKER_FEE)
                .quantity(orderTrace.balance)
                .profit(-order.fee)
                .balance(orderTrace.balance -= order.fee)
                .bar(endTime)
                .time(DateUtil.getCurrentDateTime())
                .timestamp(System.currentTimeMillis());
        orderTrace.addOrder(order);
        if (isStream) {
            printHighlight(order.ops + " :" + order);
        }
        return order;
    }


    public static OrderRecord close(boolean isStream, OrderRecord.Ops ops, OrderTrace orderTrace, OrderRecord currentPosition,
                                    double closePrice, double lastPrice, ZonedDateTime endTime) {
        OrderRecord order = OrderRecord.build();
        order.txid(currentPosition.txid)
                .ops(ops)
                .point(closePrice)
                .lastPrice(lastPrice)
                .stopLoss(-1)
                .volume(currentPosition.volume)
                .quantity(order.point * order.volume)
                .fee(order.quantity * TAKER_FEE);

        if (OrderRecord.Ops.CloseLong.equals(ops) || OrderRecord.Ops.StopLossLong.equals(ops)) {
            order.profit(order.quantity - orderTrace.balance - order.fee);
        } else if (OrderRecord.Ops.CloseShort.equals(ops) || OrderRecord.Ops.StopLossShort.equals(ops)) {
            order.profit(orderTrace.balance - order.quantity - order.fee);
        } else {
            throw new IllegalArgumentException("Wrong ops " + ops);
        }
        order.balance(orderTrace.balance += order.profit)
                .bar(endTime)
                .time(DateUtil.getCurrentDateTime())
                .timestamp(System.currentTimeMillis());
        orderTrace.addOrder(order);
        if (isStream) {
            printHighlight(order.ops + " :" + order);
        }
        return order;
    }


    public static double getStopLossWhenLong(BarSeries barSeries, double openPrice,
                                             int stopLossOffset, boolean lossLess, double stopLossPercentage, int index) {
        int stopLossIdx = index;
        Bar stopLossBar = barSeries.getBar(--stopLossIdx);
        double stopLoss = stopLossBar.getLowPrice().doubleValue();
        //default stopLossOffset is 5
        for (int i = stopLossIdx; i > stopLossIdx - stopLossOffset; i--) {
            Bar pre = barSeries.getBar(i);
            double other = pre.getLowPrice().doubleValue();
            stopLoss = Math.min(stopLoss, other);
        }
        if (lossLess) {
            stopLoss = Math.max(stopLoss, openPrice * (1 - stopLossPercentage));
        } else {
            stopLoss = Math.min(stopLoss, openPrice * (1 - stopLossPercentage));
        }
        return stopLoss;
    }

    public static double getStopLossWhenShort(BarSeries barSeries, double openPrice,
                                              int stopLossOffset, boolean lossLess, double stopLossPercentage, int index) {
        int stopLossIdx = index;
        Bar stopLossBar = barSeries.getBar(--stopLossIdx);
        double stopLoss = stopLossBar.getLowPrice().doubleValue();
        //default stopLossOffset is 5
        for (int i = stopLossIdx; i > stopLossIdx - stopLossOffset; i--) {
            Bar pre = barSeries.getBar(i);
            double other = pre.getHighPrice().doubleValue();
            stopLoss = Math.max(stopLoss, other);
        }
        if (lossLess) {
            stopLoss = Math.min(stopLoss, openPrice * (1 + stopLossPercentage));
        } else {
            stopLoss = Math.max(stopLoss, openPrice * (1 + stopLossPercentage));
        }
        return stopLoss;
    }


    public static void enableShutdownOrderTraceMonitor(TradingExecutor smaTradingExecutor, OrderTrace orderTrace) {
        enableShutdownOrderTraceMonitor(smaTradingExecutor, orderTrace, true);
    }


    public static void enableShutdownOrderTraceMonitor(TradingExecutor smaTradingExecutor, OrderTrace orderTrace, boolean dump) {
        Runtime.getRuntime().addShutdownHook(new Thread(
                new OrderTraceRunnable("ShutdownMonitor", orderTrace, smaTradingExecutor, dump)));
    }


    public static void enableScheduledOrderTraceMonitor(TradingExecutor tradingExecutor, OrderTrace orderTrace) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                new OrderTraceRunnable("ScheduledMonitor", orderTrace, tradingExecutor, false),
                10,
                10,
                TimeUnit.SECONDS);
    }


    public static void enableCLIMonitor(TradingExecutor tradingExecutor, OrderTrace orderTrace) {
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
                    new Thread(new OrderTraceRunnable("CLIMonitor", orderTrace, tradingExecutor, false)).start();
                } else if ("SnapshotDump".equalsIgnoreCase(instruction)) {
                    new Thread(new OrderTraceRunnable("CLIMonitor", orderTrace, tradingExecutor, true)).start();
                } else if ("PeekSize".equalsIgnoreCase(instruction)) {
                    printHighlight("Current trading order size :" + orderTrace.getOrders().size());
                } else if ("PeekOrders".equalsIgnoreCase(instruction)) {
                    printHighlight("Current trading orders :" + JSON.toJSONString(orderTrace.getOrders()));
                } else {
                    System.err.println("Wrong instruction!!!");
                }
            } catch (Exception e) {
                printHighlight("CLI exception!!!");
                e.printStackTrace();
            }
        }
    }


    public static double getLongMarketPrice(String symbol) {
        OrderBook book = restClient.getOrderBook(symbol.toUpperCase(), null);
        return Double.parseDouble(String.valueOf(book.getAsks().get(0).getPrice()));
    }

    public static double getShortMarketPrice(String symbol) {
        OrderBook book = restClient.getOrderBook(symbol.toUpperCase(), null);
        return Double.parseDouble(String.valueOf(book.getBids().get(0).getPrice()));
    }


    public static List<Candlestick> getCandlesticks(String symbol, CandlestickInterval interval,
                                                    int unit, int amount, int klineLimits, Date baseDate, Date stopDate) {

        List<DateUtil.HistoricalDateTime> historicalDateTimes = DateUtil.getHistoricalDateTimes(unit, amount, baseDate, stopDate);
        print("Start to call kline api " + historicalDateTimes.size() + " times in a loop for initializing klines......");
        List<Candlestick> results = new ArrayList<>();
        for (DateUtil.HistoricalDateTime date : historicalDateTimes) {
            List<Candlestick> candlestick = restClient.getCandlestick(
                    symbol,
                    interval,
                    date.timestamp,
                    date.baseTimestamp,
                    klineLimits);

            results.addAll(candlestick);
        }
        return results;
    }


    public static List<Candlestick> getCandlesticks(String symbol, CandlestickInterval interval, int klineLimits) {
        return restClient.getCandlestick(
                symbol,
                interval,
                null,
                null,
                klineLimits);
    }

}
