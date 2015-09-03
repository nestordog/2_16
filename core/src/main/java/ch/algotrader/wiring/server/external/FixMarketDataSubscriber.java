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

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.DataFeedSessionStateHolder;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.vo.SessionEventVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FixMarketDataSubscriber implements SessionEventListener {

    private final MarketDataService marketDataService;
    private final Map<String, DataFeedSessionStateHolder> sessionStateHolderMap;

    public FixMarketDataSubscriber(final MarketDataService marketDataService, final Map<String, DataFeedSessionStateHolder> sessionStateHolderMap) {

        Validate.notNull(marketDataService, "MarketDataService is null");

        this.marketDataService = marketDataService;
        this.sessionStateHolderMap = new ConcurrentHashMap<>(sessionStateHolderMap);
    }

    @Override
    public void onChange(final SessionEventVO event) {
        if (event.getState() == ConnectionState.LOGGED_ON) {
            final DataFeedSessionStateHolder sessionStateHolder = sessionStateHolderMap.get(event.getQualifier());
            if (sessionStateHolder != null) {
                marketDataService.initSubscriptions(sessionStateHolder.getFeedType());
            }
        }
    }

}
