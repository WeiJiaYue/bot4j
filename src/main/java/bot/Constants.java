package bot;

import com.binance.client.model.enums.CandlestickInterval;

/**
 * Created by louisyuu on 2021/8/20 4:53 下午
 */
public interface Constants {
    CandlestickInterval MARKET_INTERVAL = CandlestickInterval.ONE_MINUTE;
    String MARKET_SYMBOL = "BTCUSDT";
    int INIT_KLINE_COUNT = 1000;
}
