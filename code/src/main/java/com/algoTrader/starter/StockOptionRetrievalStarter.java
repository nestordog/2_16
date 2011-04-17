package com.algoTrader.starter;

import java.text.ParseException;

import com.algoTrader.ServiceLocator;

public class StockOptionRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        for (String arg : args) {
            retrieve(Integer.parseInt(arg));
        }

        ServiceLocator.serverInstance().shutdown();
    }

    public static void retrieve(int underlayingId) {

        ServiceLocator.serverInstance().getDispatcherService().getStockOptionRetrieverService().retrieveAllStockOptionsForUnderlaying(underlayingId);

    }
}
