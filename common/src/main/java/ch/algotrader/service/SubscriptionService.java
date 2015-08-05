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
package ch.algotrader.service;

import java.util.Set;

import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface SubscriptionService {

    /**
     * Subscribes {@link ch.algotrader.entity.marketData.MarketDataEvent MarketDataEvents} of a
     * Security for the defined Strategy. The default {@code feedType} is used.
     */
    public void subscribeMarketDataEvent(String strategyName, long securityId);

    /**
     * Subscribes {@link ch.algotrader.entity.marketData.MarketDataEvent MarketDataEvents} of a
     * Security for the defined Strategy with the specified {@link FeedType}.
     */
    public void subscribeMarketDataEvent(String strategyName, long securityId, FeedType feedType);

    /**
     * Unsubscribes {@link ch.algotrader.entity.marketData.MarketDataEvent MarketDataEvents} of a
     * Security for the defined Strategy. The default {@code feedType} is used.
     */
    public void unsubscribeMarketDataEvent(String strategyName, long securityId);

    /**
     * Unsubscribes {@link ch.algotrader.entity.marketData.MarketDataEvent MarketDataEvents} of a
     * Security for the defined Strategy with the specified {@link FeedType}
     */
    public void unsubscribeMarketDataEvent(String strategyName, long securityId, FeedType feedType);

    /**
     * Initializes market data subscriptions
     */
    public void initMarketDataEventSubscriptions();

    /**
     * Subscribes Generic Events of the specified class.
     */
    public void subscribeGenericEvents(Set<Class<?>> classes);

    /**
     * Initializes generic event subscriptions
     */
    public void initGenericEventSubscriptions();

}
