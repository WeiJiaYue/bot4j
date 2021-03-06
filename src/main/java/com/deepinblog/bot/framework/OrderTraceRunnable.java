package com.deepinblog.bot.framework;


import com.deepinblog.bot.utils.DateUtil;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import static com.deepinblog.bot.utils.DateUtil.printHighlight;


/**
 * Created by louisyuu on 2021/8/31 2:59 下午
 */
public class OrderTraceRunnable implements Runnable {


    private final String caller;
    private final OrderTrace orderTrace;
    private final TradingExecutor tradingExecutor;
    private final boolean dump;

    public OrderTraceRunnable(String caller, OrderTrace orderTrace, TradingExecutor tradingExecutor, boolean dump) {
        this.caller = caller;
        this.orderTrace = orderTrace;
        this.tradingExecutor = tradingExecutor;
        this.dump = dump;
    }

    @Override
    public void run() {
        printHighlight("Snapshot by " + caller);
        OrderTrace snapshot = orderTrace.clone();
        if (!(snapshot.orders.isEmpty())) {
            if (dump) {
                snapshot.dump(tradingExecutor);
            }
        }
        BarSeries barSeries = tradingExecutor.getSource().getBarSeries();
        Bar firstBar = barSeries.getFirstBar();
        Bar lastBar = barSeries.getLastBar();
        printHighlight("Date range " + DateUtil.convertToString(firstBar.getEndTime()) + " to " + DateUtil.convertToString(lastBar.getEndTime()));
        printHighlight("Symbol " + tradingExecutor.getSource().symbol() + " with kline interval " + tradingExecutor.getSource().interval().name());
        snapshot.snapshot(caller);


    }


}
