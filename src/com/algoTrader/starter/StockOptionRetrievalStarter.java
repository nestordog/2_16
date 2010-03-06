package com.algoTrader.starter;

import java.text.ParseException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;

public class StockOptionRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        Security underlaying = ServiceLocator.instance().getLookupService().getSecurity(4);

        ServiceLocator.instance().getStockOptionRetrieverService().retrieveAllStockOptions(underlaying);
    }
}
