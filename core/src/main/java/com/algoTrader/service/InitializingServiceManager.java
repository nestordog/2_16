package com.algoTrader.service;

import com.algoTrader.util.ServiceUtil;

public class InitializingServiceManager {

    public static void init() {

        for (InitializingServiceI service : ServiceUtil.getServicesByInterface(InitializingServiceI.class)) {
            service.init();
        }
    }
}
