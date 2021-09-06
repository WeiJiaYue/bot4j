package bot.framework;

import bot.utils.excel.ExcelTable;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import static bot.utils.DateUtil.print;

/**
 * Created by louisyuu on 2021/9/6 10:17 上午
 */
public abstract class TradingExecutor {

    protected final OrderTrace orderTrace;
    protected final StrategyType strategyType;
    protected int warmupCount = 10;
    protected volatile OrderRecord currentPosition;
    protected final BarSeriesSource source;
    protected ExcelTable table;


    public TradingExecutor(OrderTrace orderTrace, StrategyType strategyType, BarSeriesSource source) {
        this.orderTrace = orderTrace;
        this.strategyType = strategyType;
        this.source = source;

        execute();
    }


    public void execute() {
        //Init bar series
        source.process();
        while (!source.isStandby()) {
            print("Waiting for bar series source preparing");
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {

            }
        }
        print(source.getClass().getSimpleName() + " is standby......");

        //Back test
        if (!source.isLivingStream()) {
            BarSeries barSeries = source.getBarSeries();
            for (int i = 0; i <= barSeries.getEndIndex(); i++) {
                if (i < getWarmupCount()) {
                    continue;
                }
                Bar currentBar = barSeries.getBar(i);
                double closePriceAsLastPrice = currentBar.getClosePrice().doubleValue();
                doExecute(barSeries.getBar(i), i, closePriceAsLastPrice);
            }
        } else { //Real time trading
            BarSeriesFromStream stream = (BarSeriesFromStream) source;
            stream.addLatestBarListener(this::doExecute);
            stream.addLivingListener(this::doExecute);
        }
    }


    public void doExecute(Bar latestBar, int latestIdx, double lastPrice) {
        preRun(latestBar, latestIdx, lastPrice);
        if (shouldEnter(latestBar, latestIdx, lastPrice)) {
            TradingOps ops = getOps("open", lastPrice);

            double stopLoss = getStopLoss(latestBar, latestIdx, ops);

            OrderRecord order = Helper.open(ops.ops, orderTrace, String.valueOf(latestIdx),
                    ops.marketPrice, stopLoss, lastPrice, latestBar.getEndTime());
            //Open position
            setCurrentPosition(order);
        } else if (shouldExit(latestBar, latestIdx, lastPrice)) {
            TradingOps ops = getOps("close", lastPrice);
            Helper.close(ops.ops, orderTrace, getCurrentPosition(), ops.marketPrice, lastPrice, latestBar.getEndTime());
            //Close position
            setCurrentPosition(null);
        } else if (shouldStopLoss(latestBar, latestIdx, lastPrice)) {
            TradingOps ops = getOps("stopLoss", lastPrice);
            Helper.close(ops.ops, orderTrace, getCurrentPosition(), ops.marketPrice, lastPrice, latestBar.getEndTime());
            //Close position
            setCurrentPosition(null);
        }
        postRun(latestBar, latestIdx, lastPrice);
    }


    /**
     * stop loss 的点位是根据开单价格为基准来确定的
     *
     * @param ops 非LivingStream，ops.marketPrice为lastPrice，也就是为closePrice
     */
    public abstract double getStopLoss(Bar latestBar, int latestIdx, TradingOps ops);


    public TradingOps getOps(String direction, double marketPrice) {
        //开单
        if ("open".equals(direction)) {
            if (StrategyType.ONLY_LONG.equals(strategyType)) {
                return new TradingOps(OrderRecord.Ops.Long, getMarketPrice(OrderRecord.Ops.Long, marketPrice));
            } else {
                return new TradingOps(OrderRecord.Ops.Short, getMarketPrice(OrderRecord.Ops.Short, marketPrice));
            }
        } else if ("close".equals(direction)) {
            if (StrategyType.ONLY_LONG.equals(strategyType)) {
                return new TradingOps(OrderRecord.Ops.CloseLong, getMarketPrice(OrderRecord.Ops.CloseLong, marketPrice));
            } else {
                return new TradingOps(OrderRecord.Ops.CloseShort, getMarketPrice(OrderRecord.Ops.CloseShort, marketPrice));
            }
        } else if ("stopLoss".equals(direction)) {
            if (StrategyType.ONLY_LONG.equals(strategyType)) {
                return new TradingOps(OrderRecord.Ops.StopLossLong, getMarketPrice(OrderRecord.Ops.StopLossLong, marketPrice));
            } else {
                return new TradingOps(OrderRecord.Ops.StopLossShort, getMarketPrice(OrderRecord.Ops.StopLossShort, marketPrice));
            }
        } else {
            throw new IllegalArgumentException("Wrong direction " + direction);
        }
    }

    public double getMarketPrice(OrderRecord.Ops ops, double marketPrice) {
        if (!source.isLivingStream()) {
            return marketPrice;
        }
        if (OrderRecord.Ops.Long.equals(ops)) {
            return Helper.getLongMarketPrice(source.symbol());
        } else if (OrderRecord.Ops.Short.equals(ops)) {
            return Helper.getShortMarketPrice(source.symbol());
        } else if (OrderRecord.Ops.CloseLong.equals(ops)) {
            return Helper.getShortMarketPrice(source.symbol());
        } else if (OrderRecord.Ops.CloseShort.equals(ops)) {
            return Helper.getLongMarketPrice(source.symbol());
        } else if (OrderRecord.Ops.StopLossLong.equals(ops)) {
            return Helper.getShortMarketPrice(source.symbol());
        } else if (OrderRecord.Ops.StopLossShort.equals(ops)) {
            return Helper.getLongMarketPrice(source.symbol());
        } else {
            throw new IllegalArgumentException("Wrong ops " + ops);
        }
    }


    //----------------------------------------------------------------------------//
    //----------------------------Abstract methods--------------------------------//
    //----------------------------------------------------------------------------//

    /**
     * @param lastPrice 非LivingStream，lastPrice为ClosePrice
     */
    public abstract boolean shouldEnter(Bar latestBar, int latestIdx, double lastPrice);

    /**
     * @param lastPrice 非LivingStream，lastPrice为ClosePrice
     */
    public abstract boolean shouldExit(Bar latestBar, int latestIdx, double lastPrice);

    /**
     * @param lastPrice 非LivingStream，lastPrice为ClosePrice
     */
    public abstract boolean shouldStopLoss(Bar latestBar, int latestIdx, double lastPrice);

    public void preRun(Bar latestBar, int latestIdx, double lastPrice) {
        //Empty
    }

    public void postRun(Bar latestBar, int latestIdx, double lastPrice) {
        //Empty
    }


    //----------------------------------------------------------------------------//
    //----------------------------Getters & Setters-------------------------------//
    //----------------------------------------------------------------------------//

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


    public OrderTrace getOrderTrace() {
        return orderTrace;
    }

    public StrategyType getStrategyType() {
        return strategyType;
    }

    public BarSeriesSource getSource() {
        return source;
    }

    public ExcelTable getTable() {
        return table;
    }

    //----------------------------------------------------------------------------//
    //----------------------------Helper classes----------------------------------//
    //----------------------------------------------------------------------------//
    public enum StrategyType {
        ONLY_LONG, ONLY_SHORT,
        //Unsupported
        //BOTH_SIDES
    }


    public static class TradingOps {
        public OrderRecord.Ops ops;
        public double marketPrice;

        public TradingOps(OrderRecord.Ops ops, double marketPrice) {
            this.ops = ops;
            this.marketPrice = marketPrice;
        }
    }


}
