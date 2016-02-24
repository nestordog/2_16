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
package ch.algotrader.service;

import java.util.Set;

import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface MarketDataService {

    /**
     * Initializes current Subscriptions with the external Market Data Provider for the specified
     * feed type
     */
    void initSubscriptions(String feedType);

    /**
     * Subscribes a Security for the defined Strategy.
     */
    void subscribe(String strategyName, long securityId);

    /**
     * Subscribes a Security for the defined Strategy and feed type.
     */
    void subscribe(String strategyName, long securityId, String feedType);

    /**
     * Unsubscribes a Security for the defined Strategy.
     */
    void unsubscribe(String strategyName, long securityId);

    /**
     * Unsubscribes a Security for the defined Strategy and feed type
     */
    void unsubscribe(String strategyName, long securityId, String feedType);

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
    void removeNonPositionSubscriptionsByType(String strategyName, Class<? extends Security> type);

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
    Set<String> getSupportedFeedTypes();

    /**
     * Returns {@code true} if the data feed is supported.
     */
    boolean isSupportedFeedType(String feedType);

    /**
     * Verifies if the tick is valid for the security associated with it.
     */
    boolean isTickValid(TickVO tick);

    /**
     * Normalise the tick value according to broker params multiplier if normaliseMarketData is enabled
     */
    TickVO normaliseTick(TickVO tick);

}
