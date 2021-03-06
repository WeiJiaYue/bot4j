package com.deepinblog.bot.framework;

import com.binance.client.SubscriptionClient;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import static com.deepinblog.bot.utils.DateUtil.print;

/**
 * Created by louisyuu on 2021/9/6 3:11 下午
 */
public abstract class GenericBarSeriesSource implements BarSeriesSource {


    protected final BarSeries barSeries;
    protected final String symbol;
    protected final CandlestickInterval interval;
    protected final int initKLineCount;
    protected final SubscriptionClient websocketClient = SubscriptionClient.create();
    protected final SyncRequestClient restClient = SyncRequestClient.create();
    protected Date stopDate;
    private int shiftAmount;
    protected volatile boolean standby;


    public GenericBarSeriesSource(String symbol, CandlestickInterval interval, int initKLineCount) {
        this(new BaseBarSeriesBuilder().build(), symbol, interval, initKLineCount);
    }

    public GenericBarSeriesSource(BarSeries barSeries, String symbol, CandlestickInterval interval, int initKLineCount) {
        this.barSeries = barSeries;
        this.interval = interval;
        this.initKLineCount = initKLineCount;
        this.symbol = symbol;
    }


    @Override
    public BarSeries getBarSeries() {
        return barSeries;
    }

    @Override
    public String symbol() {
        return this.symbol;
    }

    @Override
    public CandlestickInterval interval() {
        return this.interval;
    }

    @Override
    public boolean isStandby() {
        return standby;
    }

    public SubscriptionClient getWebsocketClient() {
        return websocketClient;
    }

    public SyncRequestClient getRestClient() {
        return restClient;
    }


    public void initialHistoricalKLines() {
        System.out.println();
        print("Get KLines from exchange......");
        List<Candlestick> candlesticks;
        if (isLivingStream()) {
            print(this.getClass().getSimpleName() + " is using kline limits for initializing klines......");
            candlesticks = TradingHelper.getCandlesticks(symbol, interval, initKLineCount);
        } else {
            if (interval.isUsePeriod()) {
                print(this.getClass().getSimpleName() + " is using periods for initializing klines......");
                candlesticks = TradingHelper.getCandlesticks(symbol, interval, interval.unit(), shiftAmount, initKLineCount, new Date(), stopDate);
            } else {
                print(this.getClass().getSimpleName() + " is using kline limits for initializing klines......");
                candlesticks = TradingHelper.getCandlesticks(symbol, interval, initKLineCount);
            }
        }
        print("BarSeries initialization is starting......");
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


    public List<Candlestick> getCandlestick() {
        return restClient.getCandlestick(this.symbol, this.interval, null, null, this.initKLineCount);
    }


    public String getSymbol() {
        return symbol;
    }

    public CandlestickInterval getInterval() {
        return interval;
    }

    public int getInitKLineCount() {
        return initKLineCount;
    }

    public void setStandby(boolean standby) {
        this.standby = standby;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    public int getShiftAmount() {
        return shiftAmount;
    }

    public void setShiftAmount(int shiftAmount) {
        this.shiftAmount = shiftAmount;
    }
}
