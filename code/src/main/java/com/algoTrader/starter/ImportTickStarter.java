package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ib.IbTickService;

public class ImportTickStarter {

    public static void main(String[] args) {

        IbTickService service = ServiceLocator.serverInstance().getIbTickService();

        String[] isins = args[0].split(":");
        for (String isin : isins) {
            service.importTicks(isin);
        }

        ServiceLocator.serverInstance().shutdown();
    }
}
