package bot.mock;

import bot.excel.ExcelTable;
import bot.trade.OrderTrace;
import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.Bar;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.Map;

public abstract class SMABackTest extends BackTest {

    //Custom params
    public boolean LOSS_LESS = true;
    public int STOP_LOSS_OFFSET = 5;
    public double STOP_LOSS_PERCENTAGE = 0.1;


    //Fields
    public final ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(BAR_SERIES);
    public final SMAIndicator shortSmaIndicator = new SMAIndicator(closePriceIndicator, 5);
    public final SMAIndicator longSmaIndicator = new SMAIndicator(closePriceIndicator, 10);

    public SMABackTest(String filepath, String filename, OrderTrace orderTrace, StrategyType strategyType) {
        super(filepath, filename, orderTrace, strategyType);
    }


    @Override
    protected ExcelTable getExcelTable() {
        ExcelTable table = super.getExcelTable();
        table.addColumn("MA5")
                .addColumn("MA10")
                .addColumn("Diff")
                .addColumn("Ratio")
                .addColumn("LastPrice")
                .addColumn("Balance")
                .addColumn("Txid")
                .addColumn("Ops")
                .addColumn("Time")
                .addColumn("Point")
                .addColumn("StopLoss")
                .addColumn("TV")
                .addColumn("Quantity")
                .addColumn("Fee")
                .addColumn("Profit")
                .addColumn("OrderDetail");
        return table;

    }

}
