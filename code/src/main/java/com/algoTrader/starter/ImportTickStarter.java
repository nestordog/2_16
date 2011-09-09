package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IBSyncMarketDataService;

public class ImportTickStarter {

    public static void main(String[] args) {

        IBSyncMarketDataService service = ServiceLocator.serverInstance().getIBSyncMarketDataService();

        String[] isins = args[0].split(":");
        for (String isin : isins) {
            service.importTicks(isin);
        }

        ServiceLocator.serverInstance().shutdown();
    }
}
