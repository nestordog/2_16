package com.algoTrader.starter;

import java.text.ParseException;

import com.algoTrader.ServiceLocator;

public class StockOptionRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        start(Integer.parseInt(args[0]));
    }

    public static void start(int underlayingId) {

        ServiceLocator.serverInstance().getDispatcherService().getStockOptionRetrieverService().retrieveAllStockOptionsForUnderlaying(underlayingId);

        ServiceLocator.serverInstance().shutdown();
    }
}
