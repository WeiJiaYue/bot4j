package bot.start;

import bot.framework.*;
import com.binance.client.model.enums.CandlestickInterval;

/**
 * Created by louisyuu on 2021/9/6 4:31 下午
 */
public class SmaLongLivingTest extends SmaTradingExecutor {

    public static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    public static String SYMBOL = "SUSHIUSDT";
    public static int HISTORICAL_KLINES = 100;
    //Init balance
    public final static OrderTrace ORDER_TRACE = new OrderTrace(1000);
    public final static TradingExecutor.StrategyType STRATEGY_TYPE = TradingExecutor.StrategyType.ONLY_LONG;


    public SmaLongLivingTest(OrderTrace orderTrace, TradingExecutor.StrategyType strategyType, BarSeriesSource source) {
        super(orderTrace, strategyType, source);
    }


    public static void main(String[] args) {
        SmaTradingExecutor executor = new SmaTradingExecutor(ORDER_TRACE, STRATEGY_TYPE, new BarSeriesFromStream(SYMBOL, INTERVAL, HISTORICAL_KLINES));

        executor.execute();


        TradingHelper.enableShutdownOrderTraceMonitor(executor, ORDER_TRACE);
        TradingHelper.enableCLIMonitor(executor, ORDER_TRACE);
    }


}
