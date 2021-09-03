package bot.mock;

import bot.excel.ExcelProcessor;
import bot.excel.ExcelTable;
import bot.trade.OrderRecord;
import bot.trade.OrderTrace;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

public abstract class BackTest extends ExcelProcessor {
    public final static BarSeries BAR_SERIES = new BaseBarSeriesBuilder().build();

    private final OrderTrace orderTrace;
    private final StrategyType strategyType;
    private int warmupCount = 10;
    private volatile OrderRecord currentPosition;


    public BackTest(String filepath, String filename, OrderTrace orderTrace, StrategyType strategyType) {
        super(filepath, filename);
        this.orderTrace = orderTrace;
        this.strategyType = strategyType;
    }


    @Override
    public void doProcess(ExcelTable table) throws Exception {
        for (int i = 0; i < table.getRows().size(); i++) {
            Map<String, Object> row = table.getRow(i);
            loadBarSeriesFromExcel(row);
            if (i < getWarmupCount()) {
                continue;
            }
            runWithinHistoricalKLines(table, row, BAR_SERIES.getBar(i), i);
        }
    }


    public void runWithinHistoricalKLines(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx) {

        preRun(table, currentRow, currentBar, currentIdx);

        double closePrice = currentBar.getClosePrice().doubleValue();

        if (shouldEnter(table, currentRow, currentBar, currentIdx)) {
            double stopLoss = getStopLoss(table, currentRow, currentBar, currentIdx);
            OrderRecord order = OrderHelpers.open(getOps("open"), orderTrace, String.valueOf(currentIdx),
                    closePrice, stopLoss, closePrice, currentBar.getEndTime());

            //Open position
            setCurrentPosition(order);
        } else if (shouldExit(table, currentRow, currentBar, currentIdx)) {
            OrderHelpers.close(getOps("close"), orderTrace, getCurrentPosition(), closePrice, closePrice, currentBar.getEndTime());
            //Close position
            setCurrentPosition(null);
        } else if (shouldStopLoss(table, currentRow, currentBar, currentIdx)) {
            OrderHelpers.close(getOps("stopLoss"), orderTrace, getCurrentPosition(), closePrice, closePrice, currentBar.getEndTime());
            //Close position
            setCurrentPosition(null);
        }

        postRun(table, currentRow, currentBar, currentIdx);

    }


    public abstract boolean shouldEnter(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx);

    public abstract boolean shouldExit(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx);

    public abstract boolean shouldStopLoss(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx);

    public abstract double getStopLoss(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx);


    public void preRun(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx) {

    }

    public void postRun(ExcelTable table, Map<String, Object> currentRow, Bar currentBar, int currentIdx) {

    }
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////


    public void loadBarSeriesFromExcel(Map<String, Object> row) {
        BigDecimal open = new BigDecimal(String.valueOf(row.get("Open")));
        BigDecimal high = new BigDecimal(String.valueOf(row.get("High")));
        BigDecimal low = new BigDecimal(String.valueOf(row.get("Low")));
        BigDecimal close = new BigDecimal(String.valueOf(row.get("Close")));
        BigDecimal volume = new BigDecimal(String.valueOf(row.get("Volume")));
        String timestamp = String.valueOf(row.get("Timestamp"));
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(new Date(Long.parseLong(timestamp)).toInstant(), ZoneId.systemDefault());
        BAR_SERIES.addBar(zonedDateTime, open, high, low, close, volume);
    }

    public OrderRecord.Ops getOps(String direction) {
        OrderRecord.Ops ops;
        //开单
        if ("open".equals(direction)) {
            if (StrategyType.ONLY_LONG.equals(strategyType)) {
                ops = OrderRecord.Ops.Long;
            } else {
                ops = OrderRecord.Ops.Short;
            }
        } else if ("close".equals(direction)) {
            if (StrategyType.ONLY_LONG.equals(strategyType)) {
                ops = OrderRecord.Ops.CloseLong;
            } else {
                ops = OrderRecord.Ops.CloseShort;
            }
        } else if ("stopLoss".equals(direction)) {
            if (StrategyType.ONLY_LONG.equals(strategyType)) {
                ops = OrderRecord.Ops.StopLossLong;
            } else {
                ops = OrderRecord.Ops.StopLossShort;
            }
        } else {
            throw new IllegalArgumentException("Wrong direction " + direction);
        }

        return ops;

    }


    public static BarSeries getBarSeries() {
        return BAR_SERIES;
    }

    public int getWarmupCount() {
        return warmupCount;
    }

    public void setWarmupCount(int warmupCount) {
        this.warmupCount = warmupCount;
    }


    public synchronized OrderRecord getCurrentPosition() {
        return currentPosition;
    }

    public synchronized void setCurrentPosition(OrderRecord currentPosition) {
        this.currentPosition = currentPosition;
    }

    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    public enum StrategyType {
        ONLY_LONG, ONLY_SHORT,

        //BOTH_SIDES
    }
}
