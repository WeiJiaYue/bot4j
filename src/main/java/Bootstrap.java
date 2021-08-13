import com.binance.client.SubscriptionClient;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion;
import org.ta4j.core.analysis.criteria.pnl.ProfitLossCriterion;
import org.ta4j.core.analysis.criteria.pnl.ProfitLossPercentageCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by louisyuu on 2021/8/12 3:42 下午
 */
public class Bootstrap {
    // Creating a time series (from any provider: CSV, web service, etc.)
    static BarSeries series = new BaseBarSeriesBuilder().build();

    static int sampleCount = 1000;
    static String symbol = "btcusdt";
    static CandlestickInterval default_candle_stick_interval = CandlestickInterval.ONE_MINUTE;

    static {
        initialization();
//        livingStream();
    }




    private static void initialization() {
        SyncRequestClient syncRequestClient = SyncRequestClient.create();
        List<Candlestick> candlestickList = syncRequestClient.getCandlestick(symbol,
                default_candle_stick_interval, null, null, sampleCount);
        for (Candlestick cs : candlestickList) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(cs.getCloseTime()).toInstant(), ZoneId.systemDefault());
            series.addBar(zonedDateTime, cs.getOpen(), cs.getHigh(), cs.getLow(), cs.getClose(), cs.getVolume());
        }
    }

    public static void livingStream() {
        SubscriptionClient client = SubscriptionClient.create();
        client.subscribeCandlestickEvent(symbol, default_candle_stick_interval, (event -> {
            System.out.println(event);
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(event.getCloseTime()).toInstant(), ZoneId.systemDefault());
            series.addBar(zonedDateTime, event.getOpen(), event.getHigh(), event.getLow(), event.getClose(), event.getVolume());
//                client.unsubscribeAll();
        }), null);
    }

    public static void main(String[] args) throws Exception {


        //within an indicator:
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // Getting the simple moving average (SMA) of the close price over the last 5 ticks
        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator sma10 = new SMAIndicator(closePrice, 10);

        Rule entryRule = new CrossedUpIndicatorRule(sma5, sma10);
        Rule exitRule = new CrossedDownIndicatorRule(sma5, sma10);

        Strategy myStrategy = new BaseStrategy(entryRule, exitRule);

        BarSeriesManager seriesManager = new BarSeriesManager(series);

        TradingRecord tradingRecord = seriesManager.run(myStrategy);

        AnalysisCriterion criterion = new ProfitLossPercentageCriterion();
        System.out.println("==========");
        System.out.println("test:"+criterion.calculate(series, tradingRecord));

    }


}
