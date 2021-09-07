package bot.framework;

import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.SymbolPrice;
import org.ta4j.core.BarSeries;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static bot.utils.DateUtil.printHighlight;

/**
 * Created by louisyuu on 2021/8/20 1:08 下午
 */
public class BarSeriesFromStream extends GenericBarSeriesSource {


    private LatestBarListener latestBarListener;
    private LivingListener livingListener;

    public BarSeriesFromStream(String symbol, CandlestickInterval interval, int initKLineCount) {
        super(symbol, interval, initKLineCount);
    }

    public BarSeriesFromStream(BarSeries barSeries, String symbol, CandlestickInterval interval, int initKLineCount) {
        super(barSeries, symbol, interval, initKLineCount);
    }

    @Override
    public boolean isLivingStream() {
        return true;
    }

    @Override
    public void enableSource() {
        initialHistoricalKLines();
        streamKLines();
    }

    public void addLatestBarListener(LatestBarListener latestBarListener) {
        this.latestBarListener = latestBarListener;
    }

    public void addLivingListener(LivingListener livingListener) {
        this.livingListener = livingListener;
    }

    public void streamKLines() {
        websocketClient.subscribeCandlestickEvent(this.symbol.toLowerCase(),
                this.interval,
                event -> {
                    List<SymbolPrice> tickers = restClient.getSymbolPriceTicker(symbol.toUpperCase());
                    double lastPrice = Double.parseDouble(String.valueOf(tickers.get(0).getPrice()));
                    if (livingListener != null) {
                        livingListener.onLiving(barSeries.getLastBar(), barSeries.getEndIndex(), lastPrice);
                    }
                    //最新的一条k线还没有出来完整，先不放入BarSerial。等待周期内的k先出完毕再放入BarSerials
                    if (event.getEventTime() >= event.getCloseTime()) {
                        /**
                         * 事件时间一超过收盘价的时间，就会立马将K线信息加入BarSeries。延时为几毫秒到几十毫秒之间
                         * EventDate:2021-09-02 16:19:00:052
                         * CloseDate:2021-09-02 16:18:59:999
                         */
                        Date closeDateTime = new Date(event.getCloseTime());
                        ZonedDateTime closeZoneTime = ZonedDateTime.ofInstant(closeDateTime.toInstant(), ZoneId.systemDefault());
                        this.barSeries.addBar(closeZoneTime, event.getOpen(), event.getHigh(), event.getLow(), event.getClose(), event.getVolume());
                        if (latestBarListener != null) {
                            latestBarListener.onLatestBar(barSeries.getLastBar(), barSeries.getEndIndex(), lastPrice);
                        }
                    }
                    standby = true;
                },
                exception -> {
                    if (exception.getMessage().startsWith("Cannot add a bar with end time")) {
                        printHighlight("Duplicate kline event message");
                    } else {
                        printHighlight("Websocket error");
                        exception.printStackTrace();
                    }
                });
    }


}
