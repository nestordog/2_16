package com.algoTrader.service;

import com.algoTrader.ServiceLocator;

public class InitializingServiceManager {

    public static void init() {

        for (InitializingServiceI service : ServiceLocator.instance().getServices(InitializingServiceI.class)) {
            service.init();
        }
    }
}
