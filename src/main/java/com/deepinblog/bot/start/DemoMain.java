package com.deepinblog.bot.start;

import com.binance.client.model.enums.CandlestickInterval;
import com.deepinblog.bot.framework.*;
import com.deepinblog.bot.utils.DateUtil;

import java.util.Date;

/**
 * Created by louisyuu on 2021/9/8 11:12 上午
 */
public class DemoMain {
    //Customs
    public static String SYMBOL = "ETHUSDT";
    public static CandlestickInterval INTERVAL = CandlestickInterval.HOURLY;
    //Init balance
    public final static OrderTrace ORDER_TRACE = new OrderTrace(1000);
    public final static TradingExecutor.StrategyType STRATEGY_TYPE = TradingExecutor.StrategyType.ONLY_LONG;


    //
    public static int KLINE_LIMITS = 1500;
    public final static Date STOP_DATE = DateUtil.getHistoricalDate("2021", "09", "07", "18");
    public final static int SHIFT_AMOUNT = 500;

    public static void main(String[] args) {
        GenericBarSeriesSource barSeriesSource = new BarSeriesFromRest(SYMBOL, INTERVAL, KLINE_LIMITS);
        barSeriesSource.setStopDate(STOP_DATE);
        barSeriesSource.setShiftAmount(SHIFT_AMOUNT);
        //Enable
        barSeriesSource.enableSource();
        DemoStrategyTradingExecutor executor
                = new DemoStrategyTradingExecutor(ORDER_TRACE, STRATEGY_TYPE, barSeriesSource);
        executor.execute();

        TradingHelper.enableShutdownOrderTraceMonitor(executor, ORDER_TRACE, false);
    }
}
