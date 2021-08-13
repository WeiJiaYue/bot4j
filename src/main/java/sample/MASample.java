package sample;

import com.binance.client.RequestOptions;
import com.binance.client.SubscriptionClient;
import com.binance.client.SubscriptionListener;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.event.CandlestickEvent;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by louisyuu on 2021/8/12 3:42 下午
 */
public class MASample {


    public static void main(String[] args) throws Exception {


        // Creating a time series (from any provider: CSV, web service, etc.)
        BarSeries series = new BaseBarSeriesBuilder().build();


        // adding open, high, low, close and volume data to the series
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.1, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.2, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.3, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.4, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.5, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.6, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.7, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.8, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 111.91, 1337);
        Thread.sleep(100);
        series.addBar(ZonedDateTime.now(), 105.42, 112.99, 104.01, 112, 1337);
        // Getting the close price of the ticks
        Num firstClosePrice = series.getBar(series.getRemovedBarsCount()).getClosePrice();

        System.out.println("First close price: " + firstClosePrice.doubleValue());
        // Or within an indicator:
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        // Here is the same close price:
        System.out.println(firstClosePrice.isEqual(closePrice.getValue(0))); // equal to firstClosePrice

        // Getting the simple moving average (SMA) of the close price over the last 5 ticks
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        // Here is the 5-ticks-SMA value at the 42nd index

        System.out.println("Begin index :" + series.getBeginIndex());
        System.out.println("End index :" + series.getEndIndex());
        System.out.println("Bar count :" + series.getBarCount());
        System.out.println("RemovedBarsCount :" + series.getRemovedBarsCount());
        System.out.println("FirstBar :" + series.getFirstBar());
        System.out.println("LastBar :" + series.getLastBar());

        // shortSma.getValue(nth) From 1 to start not 0

        for (int i = 1; i < 2; i++) {
            int nth = 6;
            System.out.println("5-ticks-SMA value at the " + nth + "nd index: " + shortSma.getValue(nth).doubleValue());
        }


    }


    static BarSeries series;
    static int sampleCount = 10;
    static String symbol = "btcusdt";
    static CandlestickInterval default_candle_stick_interval = CandlestickInterval.ONE_MINUTE;

    static {
//        series = new BaseBarSeriesBuilder().withMaxBarCount(sampleCount).build();
//        initKline();

    }

    private static void initKline() {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create();
        List<Candlestick> candlestickList = syncRequestClient.getCandlestick(symbol, default_candle_stick_interval, null, null, sampleCount);
        for (Candlestick cs : candlestickList) {
            Date date = new Date(cs.getCloseTime());
            final ZoneId id = ZoneId.systemDefault();
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), id);
            series.addBar(zonedDateTime, cs.getOpen(), cs.getHigh(), cs.getLow(), cs.getClose(), cs.getVolume());
        }
    }

    public static void increaseUpdate() {
        SubscriptionClient client = SubscriptionClient.create();
        client.subscribeCandlestickEvent(symbol, default_candle_stick_interval, (new SubscriptionListener<CandlestickEvent>() {
            @Override
            public void onReceive(CandlestickEvent event) {
                System.out.println(event);
//                client.unsubscribeAll();
            }
        }), null);
    }

}
