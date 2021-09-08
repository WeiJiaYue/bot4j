package com.deepinblog.bot.start;

import com.deepinblog.bot.framework.BarSeriesSource;
import com.deepinblog.bot.framework.OrderTrace;
import com.deepinblog.bot.framework.TradingExecutor;
import org.ta4j.core.Bar;

/**
 * Created by louisyuu on 2021/9/8 11:08 上午
 */
public class DemoStrategyTradingExecutor extends TradingExecutor {


    public DemoStrategyTradingExecutor(OrderTrace orderTrace, StrategyType strategyType, BarSeriesSource source) {
        super(orderTrace, strategyType, source);

    }

    /**
     * stop loss 的点位是根据开单价格为基准来确定的
     *
     * @param ops 非LivingStream，ops.marketPrice为lastPrice，也就是为closePrice
     */
    @Override
    public double getStopLoss(Bar latestBar, int latestIdx, TradingOps ops) {
        if (latestIdx % 2 == 0) {
        }
        return -1;
    }

    /**
     * @param lastPrice 非LivingStream，lastPrice为ClosePrice
     */
    @Override
    public boolean shouldEnter(Bar latestBar, int latestIdx, double lastPrice) {
        //你的开单逻辑

        if (latestIdx % 2 == 0) {
            return true;
        } else {
            return false;
        }

    }


    /**
     * @param lastPrice 非LivingStream，lastPrice为ClosePrice
     */
    @Override
    public boolean shouldExit(Bar latestBar, int latestIdx, double lastPrice) {
        //你的止损平仓逻辑

        if (latestIdx % 2 != 0) {
            return true;
        } else {
            return false;
        }
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
        //你的止损逻辑
        return false;
    }


}
