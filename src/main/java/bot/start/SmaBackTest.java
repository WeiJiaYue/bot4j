package bot.start;

import bot.framework.*;
import bot.utils.Constants;
import bot.utils.DateUtil;
import com.binance.client.model.enums.CandlestickInterval;

import java.util.Date;

/**
 * Created by louisyuu on 2021/9/6 4:31 下午
 */
public class SmaBackTest extends SmaTradingExecutor {

    //Customs
    public static CandlestickInterval INTERVAL = CandlestickInterval.HOURLY;
    public static String SYMBOL = "BTCUSDT";
    public static int HISTORICAL_KLINES = 1500;
    //Init balance
    public final static OrderTrace ORDER_TRACE = new OrderTrace(1000);
    public final static StrategyType STRATEGY_TYPE = StrategyType.ONLY_LONG;
    public final static Date STOP_DATE = DateUtil.getHistoricalDate("2021", "09", "01", "08");
    public final static int SHIFT_AMOUNT = 500;


    public SmaBackTest(OrderTrace orderTrace, StrategyType strategyType, BarSeriesSource source) {
        super(orderTrace, strategyType, source);
    }


    public static void main(String[] args) {
        backTestWithRestSource();

        //
//        backTestWithExcelSource();
    }


    public static void backTestWithRestSource() {
        GenericBarSeriesSource barSeriesSource = new BarSeriesFromRest(SYMBOL, INTERVAL, HISTORICAL_KLINES);
        barSeriesSource.setStopDate(STOP_DATE);
        barSeriesSource.setShiftAmount(SHIFT_AMOUNT);
        //Enable
        barSeriesSource.enableSource();
        SmaTradingExecutor executor
                = new SmaTradingExecutor(ORDER_TRACE, STRATEGY_TYPE, barSeriesSource);
        executor.execute();

        TradingHelper.enableShutdownOrderTraceMonitor(executor, ORDER_TRACE, false);
    }


    public static void backTestWithExcelSource() {
        GenericBarSeriesSource barSeriesSource = new BarSeriesFromExcel(Constants.FILE_PATH, Constants.FILE_NAME(SYMBOL, INTERVAL)+".xls",
                SYMBOL, INTERVAL, HISTORICAL_KLINES);
        //Enable
        barSeriesSource.enableSource();
        SmaTradingExecutor executor
                = new SmaTradingExecutor(ORDER_TRACE, STRATEGY_TYPE, barSeriesSource);
        executor.execute();

        TradingHelper.enableShutdownOrderTraceMonitor(executor, ORDER_TRACE, false);
    }


}
