package com.binance.client.examples.websocket;

import com.deepinblog.bot.utils.DateUtil;
import com.binance.client.SubscriptionClient;

public class SubscribeSymbolTicker {

    public static void main(String[] args) {

        SubscriptionClient client = SubscriptionClient.create();
   
        client.subscribeSymbolTickerEvent("btcusdt", ((event) -> {
            System.out.println(DateUtil.getCurrentDateTime() + "==> " + event.getLastPrice());

//            System.out.println(event);
//            client.unsubscribeAll();
        }), null);

    }

}
