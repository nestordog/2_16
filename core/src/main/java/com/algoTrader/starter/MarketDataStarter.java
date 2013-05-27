/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.strategy.StrategyImpl;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.service.InitializingServiceI;
import com.algoTrader.service.MarketDataService;

/**
 * Main Starter Class for Live Trading
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
        for (InitializingServiceI service : ServiceLocator.instance().getServices(InitializingServiceI.class)) {
            service.init();
        }

        // init market data subscriptions (needs to be invoked after all Spring Services have been properly initialized)
        MarketDataService marketDataService = ServiceLocator.instance().getMarketDataService();
        marketDataService.initSubscriptions();
    }

    public static void stop() {

        ServiceLocator.instance().shutdown();
    }
}
