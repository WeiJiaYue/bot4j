package bot.framework;

import bot.utils.Constants;
import bot.utils.DateUtil;
import bot.utils.excel.ExcelProcessor;
import bot.utils.excel.ExcelTable;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.market.Candlestick;

import java.util.*;

/**
 * Created by louisyuu on 2021/8/13 5:34 下午
 */
public class BarSeriesToExcel extends ExcelProcessor {

    //Customs
    public final static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    public final static String SYMBOL = "BTCUSDT";
    public final static int KLINE_LIMITS = 1500;
    //Use period for init klines
    public final static int AMOUNT = 500;
    public final static Date STOP_DATE = DateUtil.getHistoricalDate("2021", "09", "05", "08");

    public final static String FILE_NAME = SYMBOL + "-" + INTERVAL.name();


    public static void main(String[] args) throws Exception {
        BarSeriesToExcel processor = new BarSeriesToExcel(Constants.FILE_PATH);
        processor.setNewFileName(FILE_NAME);
        processor.process();
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
        List<Candlestick> candlesticks;
        if (INTERVAL.isUsePeriod()) {
            candlesticks = TradingHelper.getCandlesticks(SYMBOL, INTERVAL, INTERVAL.unit(), AMOUNT, KLINE_LIMITS, new Date(), STOP_DATE);
        } else {
            candlesticks = TradingHelper.getCandlesticks(SYMBOL, INTERVAL, KLINE_LIMITS);
        }
        for (Candlestick candlestick : candlesticks) {
            Map<String, Object> row = table.createEmptyRow();
            row.put("D", DateUtil.convertToString(new Date(candlestick.getCloseTime())));
            row.put("T", candlestick.getCloseTime());
            row.put("O", String.valueOf(candlestick.getOpen()));
            row.put("H", String.valueOf(candlestick.getHigh()));
            row.put("L", String.valueOf(candlestick.getLow()));
            row.put("C", String.valueOf(candlestick.getClose()));
            row.put("V", String.valueOf(candlestick.getVolume()));
            table.addRow(row);
        }
    }


}
