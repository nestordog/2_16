package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.service.ImportService;

public class IVolatilityImporter {

    public static void main(String[] args) {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        ImportService service = ServiceLocator.instance().getService("importService", ImportService.class);

        service.importIVolTicks(args[0], args[1]);

        ServiceLocator.instance().shutdown();
    }
}
