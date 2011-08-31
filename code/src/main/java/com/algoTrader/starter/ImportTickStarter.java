package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IbSyncMarketDataService;

public class ImportTickStarter {

    public static void main(String[] args) {

        IbSyncMarketDataService service = ServiceLocator.serverInstance().getIbSyncMarketDataService();

        String[] isins = args[0].split(":");
        for (String isin : isins) {
            service.importTicks(isin);
        }

        ServiceLocator.serverInstance().shutdown();
    }
}
