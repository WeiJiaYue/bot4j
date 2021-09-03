package bot.mock;

import bot.DateUtil;
import bot.trade.OrderRecord;
import bot.trade.OrderTrace;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.time.ZonedDateTime;

import static bot.Constants.TAKER_FEE;
import static bot.DateUtil.printHighlight;

/**
 * Created by louisyuu on 2021/9/3 2:38 下午
 */
public class OrderHelpers {


    public static OrderRecord open(OrderRecord.Ops ops, OrderTrace orderTrace,
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
        printHighlight(order.ops + " order:" + order);
        return order;
    }


    public static OrderRecord close(OrderRecord.Ops ops, OrderTrace orderTrace, OrderRecord currentPosition,
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
        printHighlight(order.ops + " order :" + order);
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
            double other = pre.getLowPrice().doubleValue();
            stopLoss = Math.max(stopLoss, other);
        }
        if (lossLess) {
            stopLoss = Math.min(stopLoss, openPrice * (1 + stopLossPercentage));
        } else {
            stopLoss = Math.max(stopLoss, openPrice * (1 + stopLossPercentage));
        }
        return stopLoss;
    }


}
