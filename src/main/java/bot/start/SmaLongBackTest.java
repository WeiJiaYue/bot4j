package bot.start;

import bot.framework.*;
import com.binance.client.model.enums.CandlestickInterval;

/**
 * Created by louisyuu on 2021/9/6 4:31 下午
 */
public class SmaLongBackTest extends SmaTradingExecutor {

    public static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    public static String SYMBOL = "BTCUSDT";
    public static int HISTORICAL_KLINES = 1000;
    //Init balance
    public final static OrderTrace ORDER_TRACE = new OrderTrace(1000);
    public final static StrategyType STRATEGY_TYPE = StrategyType.ONLY_LONG;


    public SmaLongBackTest(OrderTrace orderTrace, StrategyType strategyType, BarSeriesSource source) {
        super(orderTrace, strategyType, source);
    }


    public static void main(String[] args) {
        BarSeriesFromRest barSeriesFromRest = new BarSeriesFromRest(SYMBOL, INTERVAL, HISTORICAL_KLINES);


        SmaTradingExecutor executor = new SmaTradingExecutor(ORDER_TRACE, STRATEGY_TYPE, barSeriesFromRest);
        Helper.enableShutdownOrderTraceMonitor(executor, ORDER_TRACE);
     }


}
