package com.binance.client.examples.market;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;

import com.binance.client.examples.constants.PrivateConfig;
import com.binance.client.model.market.OrderBook;

public class GetOrderBook {
    public static void main(String[] args) {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);
        System.out.println();



        System.out.println(System.currentTimeMillis() + "==> " + syncRequestClient.getSymbolPriceTicker("BTCUSDT"));


        OrderBook book = syncRequestClient.getOrderBook("BTCUSDT", null);


        System.out.println("LastUpdateId"+book.getLastUpdateId());
        System.out.println("AskIsSell first:"+book.getAsks().get(0));
        System.out.println("AskIsSell 2:"+book.getAsks().get(1));
        System.out.println("AskIsSell 3:"+book.getAsks().get(2));
//        System.out.println("AskIsSell end:"+book.getAsks().get(book.getAsks().size()-1));


        System.out.println("BidIsBuy first:"+book.getBids().get(0));
//        System.out.println("BidIsBuy end:"+book.getBids().get(book.getBids().size()-1));

        System.out.println();

        System.out.println(System.currentTimeMillis() + "==> " + syncRequestClient.getSymbolPriceTicker("BTCUSDT"));

    }
}
