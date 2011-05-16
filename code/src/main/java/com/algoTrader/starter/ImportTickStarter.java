package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IbMarketDataService;

public class ImportTickStarter {

    public static void main(String[] args) {

        IbMarketDataService service = ServiceLocator.serverInstance().getIbMarketDataService();

        String[] isins = args[0].split(":");
        for (String isin : isins) {
            service.importTicks(isin);
        }

        ServiceLocator.serverInstance().shutdown();
    }
}
