package bot;

import bot.trade.LivingStream;
import bot.trade.LastBarStream;
import com.binance.client.SubscriptionClient;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import com.binance.client.model.market.SymbolPrice;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * Created by louisyuu on 2021/8/20 1:08 下午
 */
public class BarLivingStream {


    private final BarSeries barSeries;
    private final CandlestickInterval interval;
    private final int initKLineCount;
    private final String symbol;


    private LastBarStream lastBarStream;
    private LivingStream livingStream;
    private volatile double lastPrice;

    public BarLivingStream(CandlestickInterval interval, String symbol, int initKLineCount) {
        this(new BaseBarSeriesBuilder().build(), symbol, interval, initKLineCount);
    }

    public BarLivingStream(BarSeries barSeries, String symbol, CandlestickInterval interval, int initKLineCount) {
        this.barSeries = barSeries;
        this.interval = interval;
        this.initKLineCount = initKLineCount;
        this.symbol = symbol;
    }

    public void run() {
        initialHistoryKLines();
        livingStreamKLines();
    }


    public static void main(String[] args) {
        BarLivingStream bar = new BarLivingStream(CandlestickInterval.ONE_MINUTE, "BTCUSDT", 1000);
        bar.run();

    }

    private void livingStreamKLines() {
        SubscriptionClient client = SubscriptionClient.create();
        client.subscribeCandlestickEvent(this.symbol.toLowerCase(),
                this.interval,
                event -> {
                    List<SymbolPrice> btcusdt = SyncRequestClient.create().getSymbolPriceTicker("BTCUSDT");
                    this.lastPrice = Double.parseDouble(String.valueOf(btcusdt.get(0).getPrice()));
                    if (livingStream != null) {
                        livingStream.onLiving(event);
                    }

                    Date closeDateTime = new Date(event.getCloseTime());
                    ZonedDateTime closeZoneTime = ZonedDateTime.ofInstant(closeDateTime.toInstant(), ZoneId.systemDefault());
                    //最新的一条k线还没有出来完整，先不放入BarSerial。等待周期内的k先出完毕再放入BarSerials
                    if (event.getEventTime() >= event.getCloseTime()) {
                        this.barSeries.addBar(closeZoneTime, event.getOpen(), event.getHigh(), event.getLow(), event.getClose(), event.getVolume());
                        if (lastBarStream != null) {
                            lastBarStream.onLastBar();
                        }
                    }
                },
                exception -> {
                    System.out.println("==> Websocket error");
                    exception.printStackTrace();
                });
    }


    private void initialHistoryKLines() {
        List<Candlestick> candlesticks = getCandlestick();
        System.out.println();
        System.out.println("==> Start initial kline");
        for (int i = 0; i < candlesticks.size(); i++) {
            Candlestick candlestick = candlesticks.get(i);
            Date closeDateTime = new Date(candlestick.getCloseTime());
            ZonedDateTime closeZoneTime = ZonedDateTime.ofInstant(closeDateTime.toInstant(), ZoneId.systemDefault());
            //最新的一条还没有出来完整，先不放入BarSerial
            if (closeDateTime.before(new Date())) {
                this.barSeries.addBar(closeZoneTime,
                        candlestick.getOpen(),
                        candlestick.getHigh(),
                        candlestick.getLow(),
                        candlestick.getClose(),
                        candlestick.getVolume());
            }
        }
    }


    private List<Candlestick> getCandlestick() {
        SyncRequestClient syncRequestClient = SyncRequestClient.create();
        return syncRequestClient.getCandlestick(this.symbol, this.interval, null, null, this.initKLineCount);
    }


    //Getters & Setters
    //Getters & Setters

    public BarSeries getBarSeries() {
        return barSeries;
    }

    public CandlestickInterval getInterval() {
        return interval;
    }

    public int getInitKLineCount() {
        return initKLineCount;
    }

    public String getSymbol() {
        return symbol;
    }

    public LastBarStream getLastBarStream() {
        return lastBarStream;
    }

    public void setLastBarStream(LastBarStream lastBarStream) {
        this.lastBarStream = lastBarStream;
    }

    public LivingStream getLivingStream() {
        return livingStream;
    }

    public void setLivingStream(LivingStream livingStream) {
        this.livingStream = livingStream;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }
}