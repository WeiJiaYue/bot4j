package com.deepinblog.bot.framework;

import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.BarSeries;

/**
 * Created by louisyuu on 2021/9/6 3:09 下午
 */
public class BarSeriesFromRest extends GenericBarSeriesSource {

    public BarSeriesFromRest(String symbol, CandlestickInterval interval, int initKLineCount) {
        super(symbol, interval, initKLineCount);
    }

    public BarSeriesFromRest(BarSeries barSeries, String symbol, CandlestickInterval interval, int initKLineCount) {
        super(barSeries, symbol, interval, initKLineCount);
    }

    @Override
    public boolean isLivingStream() {
        return false;
    }

    @Override
    public void enableSource() {
        initialHistoricalKLines();
        setStandby(true);
    }
}
