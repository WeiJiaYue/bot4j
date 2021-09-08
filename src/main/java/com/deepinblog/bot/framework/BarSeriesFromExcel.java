package com.deepinblog.bot.framework;

import com.deepinblog.bot.utils.excel.ExcelProcessor;
import com.deepinblog.bot.utils.excel.ExcelTable;
import com.binance.client.model.enums.CandlestickInterval;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

/**
 * Created by louisyuu on 2021/9/6 3:09 下午
 */
public class BarSeriesFromExcel extends GenericBarSeriesSource {

    private final String filepath;
    private final String filename;

    public BarSeriesFromExcel(String filepath, String filename,
                              String symbol, CandlestickInterval interval, int initKLineCount) {
        super(symbol, interval, initKLineCount);
        this.filepath = filepath;
        this.filename = filename;
    }


    @Override
    public boolean isLivingStream() {
        return false;
    }

    @Override
    public void enableSource() {
        new ExcelProcessor(filepath, filename) {
            @Override
            public void doProcess(ExcelTable table) throws Exception {
                for (Map<String, Object> row : table.getRows()) {
                    BigDecimal open = new BigDecimal(String.valueOf(row.get("O")));
                    BigDecimal high = new BigDecimal(String.valueOf(row.get("H")));
                    BigDecimal low = new BigDecimal(String.valueOf(row.get("L")));
                    BigDecimal close = new BigDecimal(String.valueOf(row.get("C")));
                    BigDecimal volume = new BigDecimal(String.valueOf(row.get("V")));
                    String timestamp = String.valueOf(row.get("T"));
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(Long.parseLong(timestamp)).toInstant(), ZoneId.systemDefault());
                    barSeries.addBar(zonedDateTime, open, high, low, close, volume);
                }
            }
        }.process();
        setStandby(true);
    }
}
