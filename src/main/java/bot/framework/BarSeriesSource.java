package bot.framework;

import com.binance.client.model.enums.CandlestickInterval;
import org.ta4j.core.BarSeries;

/**
 * Created by louisyuu on 2021/9/6 10:44 上午
 */
public interface BarSeriesSource {

    BarSeries getBarSeries();

    boolean isStandby();

    boolean isLivingStream();

    void enableSource();

    String symbol();

    CandlestickInterval interval();


}
