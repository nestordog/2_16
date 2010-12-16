package com.algoTrader.starter;

import java.text.ParseException;

import com.algoTrader.ServiceLocator;

public class StockOptionRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        start();
    }

    public static void start() {

        ServiceLocator.serverInstance().getDispatcherService().getStockOptionRetrieverService().retrieveAllStockOptionsForUnderlaying(4);
    }
}
