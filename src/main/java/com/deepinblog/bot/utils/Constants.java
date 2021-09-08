package com.deepinblog.bot.utils;

import com.deepinblog.bot.utils.excel.ExcelTable;
import com.binance.client.model.enums.CandlestickInterval;

/**
 * Created by louisyuu on 2021/8/20 4:53 下午
 */
public interface Constants {

    double TAKER_FEE = 0.00040;
    String PROJECT_PATH = System.getProperty("user.dir");
    String SRC_PATH = "/src/main/java/com.deepinblog.bot/file/";
    String FILE_PATH = PROJECT_PATH + SRC_PATH;
    int KLINE_LIMITS = 1500;


    static ExcelTable getKLineTable() {
        return new ExcelTable().addColumn("D").addColumn("T").addColumn("O").addColumn("H").addColumn("L").addColumn("C").addColumn("V");
    }

    static String FILE_NAME(String symbol, CandlestickInterval interval) {
        return symbol + "-" + interval.name();
    }
}
