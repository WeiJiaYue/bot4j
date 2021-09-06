package bot.framework;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

/**
 * Created by louisyuu on 2021/9/6 3:27 下午
 */
public class SmaTradingExecutor extends TradingExecutor {
    //Custom params
    public boolean LOSS_LESS = true;
    public int STOP_LOSS_OFFSET = 5;
    public double STOP_LOSS_PERCENTAGE = 0.1;


    //Fields
    public final ClosePriceIndicator closePriceIndicator;
    public final SMAIndicator shortSmaIndicator;
    public final SMAIndicator longSmaIndicator;

    public SmaTradingExecutor(OrderTrace orderTrace, StrategyType strategyType, BarSeriesSource source) {
        super(orderTrace, strategyType, source);
        closePriceIndicator = new ClosePriceIndicator(source.getBarSeries());
        shortSmaIndicator = new SMAIndicator(closePriceIndicator, 5);
        longSmaIndicator = new SMAIndicator(closePriceIndicator, 10);
    }

    /**
     * stop loss 的点位是根据开单价格为基准来确定的
     *
     * @param ops 非LivingStream，ops.marketPrice为lastPrice，也就是为closePrice
     */
    @Override
    public double getStopLoss(Bar latestBar, int latestIdx, TradingOps ops) {
        if (StrategyType.ONLY_LONG.equals(strategyType)) {
            return Helper.getStopLossWhenLong(getSource().getBarSeries(), ops.marketPrice,
                    STOP_LOSS_OFFSET, LOSS_LESS, STOP_LOSS_PERCENTAGE, latestIdx);
        } else {
            return Helper.getStopLossWhenShort(getSource().getBarSeries(), ops.marketPrice,
                    STOP_LOSS_OFFSET, LOSS_LESS, STOP_LOSS_PERCENTAGE, latestIdx);
        }
    }

    /**
     * @param lastPrice 非LivingStream，lastPrice为ClosePrice
     */
    @Override
    public boolean shouldEnter(Bar latestBar, int latestIdx, double lastPrice) {
        double ma5 = shortSmaIndicator.getValue(latestIdx).doubleValue();
        double ma10 = longSmaIndicator.getValue(latestIdx).doubleValue();
        if (StrategyType.ONLY_LONG.equals(strategyType)) {
            if (getCurrentPosition() == null && ma5 > ma10) {
                return true;
            }
        } else {
            if (getCurrentPosition() == null && ma5 < ma10) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param lastPrice 非LivingStream，lastPrice为ClosePrice
     */
    @Override
    public boolean shouldExit(Bar latestBar, int latestIdx, double lastPrice) {
        double ma5 = shortSmaIndicator.getValue(latestIdx).doubleValue();
        double ma10 = longSmaIndicator.getValue(latestIdx).doubleValue();
        if (StrategyType.ONLY_LONG.equals(strategyType)) {
            if (getCurrentPosition() != null && ma5 < ma10) {
                return true;
            }
        } else {
            if (getCurrentPosition() != null && ma5 > ma10) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param lastPrice 非LivingStream，lastPrice为ClosePrice
     *                  <p>
     *                  FIXME 这个地方要注意了，根据回测或者实时数据测试，看是根据k线出来后的closePrice来对比止损好
     *                  还是根据实时的lastPrice对比止损好。
     *                  如果是后者，两步止损法
     *                  </p>
     */
    @Override
    public boolean shouldStopLoss(Bar latestBar, int latestIdx, double lastPrice) {
        if (StrategyType.ONLY_LONG.equals(strategyType)) {
            double comparePrice;
            if (source.isLivingStream()) {
                comparePrice = lastPrice;
            } else {
                comparePrice = latestBar.getLowPrice().doubleValue();
            }
            if (getCurrentPosition() != null && comparePrice < getCurrentPosition().stopLoss) {
                return true;
            }
        } else {
            double comparePrice;
            if (source.isLivingStream()) {
                comparePrice = lastPrice;
            } else {
                comparePrice = latestBar.getHighPrice().doubleValue();
            }

            if (getCurrentPosition() != null && comparePrice > getCurrentPosition().stopLoss) {
                return true;
            }
        }
        return false;
    }


    public SMAIndicator getShortSmaIndicator() {
        return shortSmaIndicator;
    }

    public SMAIndicator getLongSmaIndicator() {
        return longSmaIndicator;
    }
}
