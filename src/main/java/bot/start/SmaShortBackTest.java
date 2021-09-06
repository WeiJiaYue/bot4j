package bot.start;

import bot.framework.*;
import com.binance.client.model.enums.CandlestickInterval;

/**
 * Created by louisyuu on 2021/9/6 4:31 下午
 */
public class SmaShortBackTest extends SmaTradingExecutor {

    public static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    public static String SYMBOL = "AXSUSDT";
    public static int HISTORICAL_KLINES = 1000;
    //Init balance
    public final static OrderTrace ORDER_TRACE = new OrderTrace(1000);
    public final static StrategyType STRATEGY_TYPE = StrategyType.ONLY_SHORT;


    public SmaShortBackTest(OrderTrace orderTrace, StrategyType strategyType, BarSeriesSource source) {
        super(orderTrace, strategyType, source);
    }


    public static void main(String[] args) {

        SmaTradingExecutor executor
                = new SmaTradingExecutor(ORDER_TRACE, STRATEGY_TYPE, new BarSeriesFromRest(SYMBOL, INTERVAL, HISTORICAL_KLINES));

        executor.execute();

        TradingHelper.enableShutdownOrderTraceMonitor(executor, ORDER_TRACE,false);
    }


}
