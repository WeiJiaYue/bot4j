package bot.trade;

import org.ta4j.core.BarSeries;

/**
 * Created by louisyuu on 2021/8/27 4:15 下午
 */
public interface LastBarStream {

    void onLastBar(BarSeries barSeries);
}
