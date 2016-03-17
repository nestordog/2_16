/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.marketdata;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.Validate;

import ch.algotrader.UnrecoverableCoreException;
import ch.algotrader.concurrent.BasicThreadFactory;
import ch.algotrader.entity.PositionVO;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.listener.PositionEventListener;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.vo.SessionEventVO;

public class MarketDataSubscriber implements SessionEventListener, PositionEventListener {

    private final EventDispatcher eventDispatcher;
    private final LookupService lookupService;
    private final MarketDataService marketDataService;
    private final Map<String, String> sessionToFeedTypeMap;
    private final ExecutorService executorService;

    public MarketDataSubscriber(
            final EventDispatcher eventDispatcher,
            final LookupService lookupService,
            final MarketDataService marketDataService,
            final Map<String, String> sessionToFeedTypeMap) {

        Validate.notNull(eventDispatcher, "EventDispatcher is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(marketDataService, "MarketDataService is null");

        this.eventDispatcher = eventDispatcher;
        this.lookupService = lookupService;
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
                        this.marketDataService.initSubscriptions(feedType);
                    } catch (InterruptedException ignore) {
                    }
                });

            }
        }
    }

    @Override
    public void onPositionChange(final PositionVO positionMutation) {

        Strategy strategy = this.lookupService.getStrategy(positionMutation.getStrategyId());
        if (strategy == null) {
            throw new UnrecoverableCoreException("Unexpected strategy id: " + positionMutation.getStrategyId());
        }
        if (!this.eventDispatcher.isMarketDataSubscriptionRegistered(positionMutation.getSecurityId(), strategy.getName())) {
            this.marketDataService.subscribe(strategy.getName(), positionMutation.getSecurityId());
        }
    }

    public void destroy() throws Exception {

        this.executorService.shutdownNow();
    }

}
