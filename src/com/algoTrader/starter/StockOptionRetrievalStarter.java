package com.algoTrader.starter;

import java.text.ParseException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;

public class StockOptionRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        start();
    }

    public static void start() {

        Security underlaying = ServiceLocator.instance().getLookupService().getSecurity(4);

        ServiceLocator.instance().getDispatcherService().getStockOptionRetrieverService().retrieveAllStockOptions(underlaying);
    }
}
