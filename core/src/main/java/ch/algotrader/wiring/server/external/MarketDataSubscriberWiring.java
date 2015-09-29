/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.wiring.server.external;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.service.ExternalMarketDataService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.vo.SessionEventVO;

/**
 * Fix market data subscriber.
 */
@Configuration
@Profile({"iBMarketData", "dcMarkteData", "lMAXMarketData", "fXCMMarketData", "cNXMarketData", "fTXMarketData"})
public class MarketDataSubscriberWiring {

    @Bean(name = "fixMarketDataSubscriber")
    public SessionEventListener createFixMarketDataSubscriber(final MarketDataService marketDataService, final ApplicationContext applicationContext) {

        Map<String, ExternalMarketDataService> beanMap1 = applicationContext.getBeansOfType(ExternalMarketDataService.class);
        Map<String, String> beanMap2 = beanMap1.values().stream()
                .collect(Collectors.toMap(ExternalMarketDataService::getSessionQualifier, ExternalMarketDataService::getFeedType));

        return new MarketDataSubscriber(marketDataService, beanMap2);
    }

    static class MarketDataSubscriber implements SessionEventListener {

        private final MarketDataService marketDataService;
        private final Map<String, String> sessionToFeedTypeMap;

        public MarketDataSubscriber(final MarketDataService marketDataService, final Map<String, String> sessionToFeedTypeMap) {

            Validate.notNull(marketDataService, "MarketDataService is null");

            this.marketDataService = marketDataService;
            this.sessionToFeedTypeMap = new ConcurrentHashMap<>(sessionToFeedTypeMap);
        }

        @Override
        public void onChange(final SessionEventVO event) {
            if (event.getState() == ConnectionState.LOGGED_ON) {
                String feedType = sessionToFeedTypeMap.get(event.getQualifier());
                if (feedType != null) {
                    marketDataService.initSubscriptions(feedType);
                }
            }
        }

    }

}
