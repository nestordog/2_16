package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ImportService;

public class ImportTickStarter {

    public static void main(String[] args) {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        ImportService service = ServiceLocator.instance().getService("importService", ImportService.class);

        String[] isins = args[0].split(":");
        for (String isin : isins) {
            service.importTicks(isin);
        }

        ServiceLocator.instance().shutdown();
    }
}
