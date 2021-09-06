package bot.utils;

import com.binance.client.model.enums.CandlestickInterval;

/**
 * Created by louisyuu on 2021/8/20 4:53 下午
 */
public interface Constants {

    double TAKER_FEE = 0.00040;
    String PROJECT_PATH = System.getProperty("user.dir");
    String SRC_PATH = "/src/main/java/bot/file/";
    String FILE_PATH = PROJECT_PATH + SRC_PATH;
    String CANDLESTICK_TEMPLE_FILE_NAME = "CandlestickTemple.xlsx";


    CandlestickInterval MARKET_INTERVAL = CandlestickInterval.ONE_MINUTE;
    String MARKET_SYMBOL = "BTCUSDT";
    int INIT_KLINE_COUNT = 1000;
}
