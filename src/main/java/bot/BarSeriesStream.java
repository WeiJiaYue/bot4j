package bot;

import com.binance.client.SubscriptionClient;
import com.binance.client.SubscriptionErrorHandler;
import com.binance.client.SubscriptionListener;
import com.binance.client.SyncRequestClient;
import com.binance.client.exception.BinanceApiException;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.event.CandlestickEvent;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by louisyuu on 2021/8/20 1:08 下午
 */
public class BarSeriesStream {
    public final static BarSeries BAR_SERIES = new BaseBarSeriesBuilder().build();
    public final static List<Double> SMA5_SERIES = new ArrayList<>();
    public final static List<Double> SMA10_SERIES = new ArrayList<>();

    //Custom
    private final static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    private final static String SYMBOL = "BTCUSDT";
    private final static int KLINE_COUNT = 1000;

    static {
        load();
        living();
    }


    public static void load() {
        List<Candlestick> candlesticks = getCandlestick();
        for (Candlestick candlestick : candlesticks) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(candlestick.getCloseTime()).toInstant(), ZoneId.systemDefault());
            BAR_SERIES.addBar(zonedDateTime, candlestick.getOpen(), candlestick.getHigh(), candlestick.getLow(), candlestick.getClose(), candlestick.getVolume());
        }
    }


    private static List<Candlestick> getCandlestick() {
        SyncRequestClient syncRequestClient = SyncRequestClient.create();
        return syncRequestClient.getCandlestick(SYMBOL, INTERVAL, null, null, KLINE_COUNT);
    }


    public static void living() {
        SubscriptionClient client = SubscriptionClient.create();
        client.subscribeCandlestickEvent(SYMBOL, INTERVAL, (event -> {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(event.getCloseTime()).toInstant(), ZoneId.systemDefault());
            BAR_SERIES.addBar(zonedDateTime, event.getOpen(), event.getHigh(), event.getLow(), event.getClose(), event.getVolume());

        }), exception -> {
            System.out.println("==> Websocket error");
            exception.printStackTrace();
        });
    }


}
