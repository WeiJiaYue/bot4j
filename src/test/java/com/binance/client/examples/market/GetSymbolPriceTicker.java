package com.binance.client.examples.market;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;

import com.binance.client.examples.constants.PrivateConfig;

public class GetSymbolPriceTicker {
    public static void main(String[] args) {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);

        System.out.println(System.currentTimeMillis() + "==> " + syncRequestClient.getSymbolPriceTicker("BTCUSDT"));

//        for (int i = 0; i < 100; i++) {
//
//
//            try {
//                Thread.sleep(500L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
        // System.out.println(syncRequestClient.getSymbolPriceTicker(null));
    }
}
