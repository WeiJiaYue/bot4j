package com.deepinblog.bot.framework;

import org.ta4j.core.Bar;

/**
 * Created by louisyuu on 2021/9/6 10:56 上午
 */
public interface LivingListener {


    void onLiving(Bar latestBar, int latestIdx, double lastPrice);
}
