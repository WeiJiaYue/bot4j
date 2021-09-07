package com.binance.client.model.enums;

import java.util.Calendar;

/**
 * 1min, 5min, 15min, 30min, 60min, 1day, 1mon, 1week, 1year
 */
public enum CandlestickInterval {
    ONE_MINUTE("1m", Calendar.MINUTE, true),
    THREE_MINUTES("3m", Calendar.MINUTE, true),
    FIVE_MINUTES("5m", Calendar.MINUTE, true),
    FIFTEEN_MINUTES("15m", Calendar.MINUTE, true),
    HALF_HOURLY("30m", Calendar.MINUTE, true),
    HOURLY("1h", Calendar.HOUR, true),
    TWO_HOURLY("2h", Calendar.HOUR, true),
    FOUR_HOURLY("4h", Calendar.HOUR, true),
    SIX_HOURLY("6h", Calendar.HOUR, true),
    EIGHT_HOURLY("8h", Calendar.HOUR, true),
    TWELVE_HOURLY("12h", Calendar.HOUR, true),
    DAILY("1d", Calendar.DAY_OF_MONTH, false),
    THREE_DAILY("3d", Calendar.DAY_OF_MONTH, false),
    WEEKLY("1w", Calendar.DAY_OF_MONTH, false),
    MONTHLY("1M", Calendar.DAY_OF_MONTH, false);

    private final String code;
    private final int unit;
    private final boolean usePeriod;

    CandlestickInterval(String code, int unit, boolean usePeriod) {
        this.code = code;
        this.unit = unit;
        this.usePeriod = usePeriod;
    }

    public final String val() {
        return code;
    }

    public final int unit() {
        return unit;
    }

    public final boolean isUsePeriod() {
        return usePeriod;
    }

    @Override
    public String toString() {
        return code;
    }
}
