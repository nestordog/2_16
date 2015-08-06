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

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.enumeration.FeedType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface MarketDataService {

    /**
     * Persists a Tick to the DB and CSV File.
     */
    void persistTick(Tick tick);

    /**
     * Initializes current Subscriptions with the external Market Data Provider for the specified
     * {@link FeedType}
     */
    void initSubscriptions(FeedType feedType);

    /**
     * Subscribes a Security for the defined Strategy.
     */
    void subscribe(String strategyName, long securityId);

    /**
     * Subscribes a Security for the defined Strategy and {@link FeedType}.
     */
    void subscribe(String strategyName, long securityId, FeedType feedType);

    /**
     * Unsubscribes a Security for the defined Strategy.
     */
    void unsubscribe(String strategyName, long securityId);

    /**
     * Unsubscribes a Security for the defined Strategy and {@link FeedType}
     */
    void unsubscribe(String strategyName, long securityId, FeedType feedType);

    /**
     * Removes Subscriptions of a particular Strategy for which the Strategy does not have an open
     * Position.
     */
    void removeNonPositionSubscriptions(String strategyName);

    /**
     * Removes Subscriptions of a particular Strategy and type for which the Strategy does not have
     * an open Position.
     * @param type The class for which a potential Subscription should be removed. Example: {@link ch.algotrader.entity.security.Future Future}
     */
    void removeNonPositionSubscriptionsByType(String strategyName, Class type);

    /**
     * Publishes the latest Market Data Events of all subscribed Securities to the corresponding
     * Strategy.
     */
    void requestCurrentTicks(String strategyName);

    /**
     * Called in situations where no Market Data Events have been received for the specified {@code
     * SecurityFamily.maxGap}
     */
    void logTickGap(long securityId);

    /**
     * Returns all supported data feeds.
     */
    Set<FeedType> getSupportedFeeds();

    /**
     * Returns {@code true} if the data feed is supported.
     */
    boolean isSupportedFeed(FeedType feedType);

    /**
     * Verifies if the tick is valid for the security associated with it.
     */
    boolean isTickValid(TickVO tick);

}
