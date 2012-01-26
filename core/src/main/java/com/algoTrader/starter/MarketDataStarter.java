package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.service.MarketDataService;
import com.algoTrader.service.OrderService;
import com.algoTrader.service.RuleService;
import com.algoTrader.service.ib.IBService;

public class MarketDataStarter {

    public static void main(String[] args) throws Exception {

        start();
    }

    public static void start() throws Exception {

        // start all BASE rules
        ServiceLocator.instance().init(ServiceLocator.SERVER_BEAN_REFERENCE_LOCATION);
        RuleService ruleService = ServiceLocator.instance().getRuleService();
        ruleService.initServiceProvider(StrategyImpl.BASE);
        ruleService.setInternalClock(StrategyImpl.BASE, true);
        ruleService.deployAllModules(StrategyImpl.BASE);

        // initialize the IB services
        MarketDataService marketDataService = ServiceLocator.instance().getMarketDataService();
        marketDataService.init();

        OrderService orderService = ServiceLocator.instance().getOrderService();
        orderService.init();

        // subscribe marketData for all securities on the watchlist (needs to be invoked after all Spring Services have been properly initialized)
        marketDataService.initWatchlist();

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
