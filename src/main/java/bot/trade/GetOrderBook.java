package bot.trade;

import com.binance.client.SyncRequestClient;
import com.binance.client.model.market.OrderBook;

public class GetOrderBook {
    static SyncRequestClient syncRequestClient = SyncRequestClient.create();

    public static double getLongMarketPrice(String symbol) {
        OrderBook book = syncRequestClient.getOrderBook(symbol.toUpperCase(), null);
        return Double.parseDouble(String.valueOf(book.getAsks().get(0).getPrice()));
    }

    public static double getShortMarketPrice(String symbol) {
        OrderBook book = syncRequestClient.getOrderBook(symbol.toUpperCase(), null);
        return Double.parseDouble(String.valueOf(book.getBids().get(0).getPrice()));
    }
}
