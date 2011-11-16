package com.algoTrader.starter;

import java.text.ParseException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IBStockOptionRetrieverService;

public class StockOptionRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        IBStockOptionRetrieverService service = ServiceLocator.serverInstance().getIBStockOptionRetrieverService();

        service.init();

        for (String arg : args) {

            int underlayingId = Integer.parseInt(arg);
            service.retrieveAllStockOptionsForUnderlaying(underlayingId);
        }

        ServiceLocator.serverInstance().shutdown();
    }
}
