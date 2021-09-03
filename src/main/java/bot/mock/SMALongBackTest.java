package bot.mock;

import bot.Constants;
import bot.excel.ExcelTable;
import bot.trade.OrderTrace;
import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.Bar;
import java.util.Map;

public class SMALongBackTest extends SMABackTest {

    public static CandlestickInterval INTERVAL = CandlestickInterval.ONE_MINUTE;
    public static String SYMBOL = "BTCUSDT";
    //Init balance
    public final static OrderTrace ORDER_TRACE = new OrderTrace(1000);

    public static void main(String[] args) throws Exception {
        String filename = SYMBOL + "-" + INTERVAL.val();
        SMALongBackTest processor = new SMALongBackTest(Constants.FILE_PATH, filename + ".xls", ORDER_TRACE);
        processor.setNeededGenerateNewExcel(true);
        processor.setNewFileName(filename + "-BACKTEST");
        processor.process();
    }


    public SMALongBackTest(String filepath, String filename, OrderTrace orderTrace) {
        super(filepath, filename, orderTrace, StrategyType.ONLY_LONG);
    }

    @Override
    public boolean shouldEnter(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx) {
        double ma5 = shortSmaIndicator.getValue(currentIdx).doubleValue();
        double ma10 = longSmaIndicator.getValue(currentIdx).doubleValue();

        //Enter when crossover
        if (ma5 > ma10 && getCurrentPosition() == null) {
            return true;
        }
        table.updateRow(currentRow, "SMA5", ma5);
        table.updateRow(currentRow, "SMA10", ma10);
        table.updateRow(currentRow, "Ratio", ma10 / ma5);
        return false;
    }

    @Override
    public boolean shouldExit(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx) {
        double ma5 = shortSmaIndicator.getValue(currentIdx).doubleValue();
        double ma10 = longSmaIndicator.getValue(currentIdx).doubleValue();
        if (getCurrentPosition() != null && ma5 < ma10) {
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldStopLoss(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx) {
        if (getCurrentPosition() != null && currentBar.getLowPrice().doubleValue() < getCurrentPosition().stopLoss) {
            return true;
        }
        return false;
    }

    @Override
    public double getStopLoss(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx) {
        double closePriceAsOpenPrice = currentBar.getClosePrice().doubleValue();
        return OrderHelpers.getStopLossWhenLong(BAR_SERIES, closePriceAsOpenPrice,
                STOP_LOSS_OFFSET, LOSS_LESS, STOP_LOSS_PERCENTAGE, currentIdx);
    }
}
