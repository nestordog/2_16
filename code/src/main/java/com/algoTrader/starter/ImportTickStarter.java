package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ImportService;

public class ImportTickStarter {

    public static void main(String[] args) {

        ImportService service = ServiceLocator.serverInstance().getImportService();

        String[] isins = args[0].split(":");
        for (String isin : isins) {
            service.importTicks(isin);
        }

        ServiceLocator.serverInstance().shutdown();
    }
}
