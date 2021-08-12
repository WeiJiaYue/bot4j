package com.binance.client.examples.websocket;

import com.binance.client.SubscriptionClient;
import com.binance.client.SubscriptionListener;
import com.binance.client.model.enums.CandlestickInterval;
import com.binance.client.model.event.CandlestickEvent;

import java.util.concurrent.CountDownLatch;

public class SubscribeCandlestick {

    public static void main(String[] args) throws Exception{

        CountDownLatch latch = new CountDownLatch(1);

        SubscriptionClient client = SubscriptionClient.create();
   
        client.subscribeCandlestickEvent("btcusdt", CandlestickInterval.ONE_MINUTE, (new SubscriptionListener<CandlestickEvent>() {
            @Override
            public void onReceive(CandlestickEvent event) {
                System.out.println(event);
//                client.unsubscribeAll();
            }
        }), null);


//        latch.await();

    }

}
