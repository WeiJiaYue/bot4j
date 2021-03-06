package com.deepinblog.bot.framework;

import com.deepinblog.bot.start.DemoMain;
import com.deepinblog.bot.utils.Constants;
import com.deepinblog.bot.utils.DateUtil;
import com.deepinblog.bot.utils.excel.ExcelProcessor;
import com.deepinblog.bot.utils.excel.ExcelTable;
import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.sql.Timestamp;
import java.util.*;

import static com.deepinblog.bot.utils.DateUtil.printHighlight;

/**
 * Created by louisyuu on 2021/8/13 5:34 下午
 */
public class BarSeriesToExcel extends ExcelProcessor {

    //Customs
    public final static CandlestickInterval INTERVAL = DemoMain.INTERVAL;
    public final static String SYMBOL = DemoMain.SYMBOL;
    public final static int KLINE_LIMITS = DemoMain.KLINE_LIMITS;
    //Use period for init klines
    public final static Date STOP_DATE = DemoMain.STOP_DATE;
    public final static int SHIFT_AMOUNT = DemoMain.SHIFT_AMOUNT;


    public static void main(String[] args) throws Exception {
        BarSeriesToExcel processor = new BarSeriesToExcel(Constants.FILE_PATH);
        processor.setNewFileName(Constants.FILE_NAME(SYMBOL, INTERVAL));
        processor.process();
        DateUtil.printHighlight("BarSeriesToExcel successfully");
    }


    public BarSeriesToExcel(String filepath) {
        super(filepath);
    }

    @Override
    protected ExcelTable getExcelTable() {
        return Constants.getKLineTable();
    }


    @Override
    public void doProcess(ExcelTable table) {
        GenericBarSeriesSource barSeriesSource = new BarSeriesFromRest(SYMBOL, INTERVAL, KLINE_LIMITS);
        barSeriesSource.setStopDate(STOP_DATE);
        barSeriesSource.setShiftAmount(SHIFT_AMOUNT);
        barSeriesSource.enableSource();

        BarSeries barSeries = barSeriesSource.getBarSeries();
        for (int i = 0; i <= barSeries.getEndIndex(); i++) {
            Bar bar = barSeries.getBar(i);
            Map<String, Object> row = table.createEmptyRow();
            row.put("D", DateUtil.convertToString(bar.getEndTime()));
            row.put("T", Timestamp.from(bar.getEndTime().toInstant()).getTime());
            row.put("O", String.valueOf(bar.getOpenPrice()));
            row.put("H", String.valueOf(bar.getHighPrice()));
            row.put("L", String.valueOf(bar.getLowPrice()));
            row.put("C", String.valueOf(bar.getClosePrice()));
            row.put("V", String.valueOf(bar.getVolume()));
            table.addRow(row);
        }
        Bar firstBar = barSeries.getFirstBar();
        Bar lastBar = barSeries.getLastBar();
        printHighlight("Date range " + DateUtil.convertToString(firstBar.getEndTime()) + " to " + DateUtil.convertToString(lastBar.getEndTime()));
        printHighlight("Symbol " + SYMBOL + " with kline interval " + INTERVAL.name());
    }


}
