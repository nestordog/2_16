package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.service.InitializingServiceManager;
import com.algoTrader.service.MarketDataService;

public class MarketDataStarter {

    public static void main(String[] args) throws Exception {

        start();
    }

    public static void start() throws Exception {

        // start all BASE rules
        ServiceLocator.instance().init(ServiceLocator.SERVER_BEAN_REFERENCE_LOCATION);

        EsperManager.initServiceProvider(StrategyImpl.BASE);
        EsperManager.setInternalClock(StrategyImpl.BASE, true);
        EsperManager.deployAllModules(StrategyImpl.BASE);

        // initialize services
        InitializingServiceManager.init();

        // init market data subscriptions (needs to be invoked after all Spring Services have been properly initialized)
        MarketDataService marketDataService = ServiceLocator.instance().getMarketDataService();
        marketDataService.initSubscriptions();
    }

    public static void stop() {

        ServiceLocator.instance().shutdown();
    }
}
