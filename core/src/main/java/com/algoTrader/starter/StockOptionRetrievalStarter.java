package com.algoTrader.starter;

import java.text.ParseException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IBStockOptionRetrieverService;

public class StockOptionRetrievalStarter {

    public static void main(String[] args) throws ParseException {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        IBStockOptionRetrieverService service = ServiceLocator.instance().getService("iBStockOptionRetrieverService", IBStockOptionRetrieverService.class);

        service.init();

        for (String arg : args) {

            int underlayingId = Integer.parseInt(arg);
            service.retrieveAllStockOptionsForUnderlaying(underlayingId);
        }

        ServiceLocator.instance().shutdown();
    }
}
