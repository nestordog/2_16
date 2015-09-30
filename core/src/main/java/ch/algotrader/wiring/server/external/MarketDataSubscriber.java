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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.Validate;

import ch.algotrader.concurrent.BasicThreadFactory;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.vo.SessionEventVO;

class MarketDataSubscriber implements SessionEventListener {

    private final MarketDataService marketDataService;
    private final Map<String, String> sessionToFeedTypeMap;
    private final ExecutorService executorService;

    public MarketDataSubscriber(final MarketDataService marketDataService, final Map<String, String> sessionToFeedTypeMap) {

        Validate.notNull(marketDataService, "MarketDataService is null");

        this.marketDataService = marketDataService;
        this.sessionToFeedTypeMap = new ConcurrentHashMap<>(sessionToFeedTypeMap);
        this.executorService = Executors.newSingleThreadExecutor(new BasicThreadFactory("Market-data-subscriber-thread", true));
    }

    @Override
    public void onChange(final SessionEventVO event) {
        if (event.getState() == ConnectionState.LOGGED_ON) {
            String feedType = sessionToFeedTypeMap.get(event.getQualifier());
            if (feedType != null) {
                this.executorService.execute(() -> {
                    try {
                        // Sleep a little
                        Thread.sleep(1000);
                        marketDataService.initSubscriptions(feedType);
                    } catch (InterruptedException ignore) {
                    }
                });

            }
        }
    }

    public void destroy() throws Exception {

        this.executorService.shutdownNow();
    }

}
