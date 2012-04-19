package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.service.MarketDataService;
import com.algoTrader.service.OrderService;
import com.algoTrader.service.ib.IBService;

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

        // initialize the IB services
        MarketDataService marketDataService = ServiceLocator.instance().getMarketDataService();
        marketDataService.init();

        OrderService orderService = ServiceLocator.instance().getOrderService();
        orderService.init();

        // init market data subscriptions (needs to be invoked after all Spring Services have been properly initialized)
        marketDataService.initSubscriptions();

        // wait a little to avoid several IBClients connecting at the same time
        // TODO find a better way for this
        Thread.sleep(10000);

        IBService ibService = ServiceLocator.instance().getService("iBService", IBService.class);
        ibService.init();
    }

    public static void stop() {

        ServiceLocator.instance().shutdown();
    }

}
