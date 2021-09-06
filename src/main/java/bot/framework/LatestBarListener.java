package bot.framework;

import org.ta4j.core.Bar;

/**
 * Created by louisyuu on 2021/9/6 10:55 上午
 */
public interface LatestBarListener {


    void onLatestBar(Bar latestBar, int latestIdx, double lastPrice);

}
