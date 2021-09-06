package com.binance.client.examples.websocket;

import bot.utils.DateUtil;
import com.binance.client.SubscriptionClient;

public class SubscribeBookDepth {

    public static void main(String[] args) {

        SubscriptionClient client = SubscriptionClient.create();

        client.subscribeBookDepthEvent("btcusdt", 5, ((event) -> {
            System.out.println(DateUtil.getCurrentDateTime() + "==> Ask" + event.getAsks().get(0) + "==> Bid" + event.getBids().get(0));

//            client.unsubscribeAll();
        }), null);

    }

}
