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
package ch.algotrader.dao;

import java.util.List;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.security.Security;
import ch.algotrader.util.collection.Pair;

/**
 * DAO for {@link ch.algotrader.entity.Subscription} objects.
 *
 * @see ch.algotrader.entity.Subscription
 */
public interface SubscriptionDao extends ReadWriteDao<Subscription> {

    /**
     * Finds all Subscriptions for the given security..
     * @param securityId
     * @return List<Subscription>
     */
    List<Subscription> findBySecurity(long securityId);

    /**
     * Finds all Subscriptions by the defined {@code strategyName}.
     * @param strategyName
     * @return List<Subscription>
     */
    List<Subscription> findByStrategy(String strategyName);

    /**
     * Finds all Subscriptions by the defined {@code strategyName} including all attached {@code properties}
     * @param strategyName
     * @return List<Subscription>
     */
    List<Subscription> findByStrategyInclProps(String strategyName);

    /**
     * Find a Subscriptions by the defined {@code strategyName} and {@code securityId}.
     * @param strategyName
     * @param securityId
     * @return Subscription
     */
    Subscription findByStrategyAndSecurity(String strategyName, long securityId);

    /**
     * Find a Subscriptions by the defined {@code strategyName}, {@code securityId} and {@code
     * feedType}
     * @param strategyName
     * @param securityId
     * @param feedType
     * @return Subscription
     */
    Subscription findByStrategySecurityAndFeedType(String strategyName, long securityId, String feedType);

    /**
     * Finds all Subscriptions By Security and {@code feedType} for Strategies that are marked
     * {@code autoActivate}
     * @param securityId
     * @param feedType
     * @return List<Subscription>
     */
    List<Subscription> findBySecurityAndFeedTypeForAutoActivateStrategies(long securityId, String feedType);

    /**
     * Find {@code non-persistent} Subscriptions
     * @return List<Subscription>
     */
    List<Subscription> findNonPersistent();

    /**
     * Finds Subscriptions for the specified Strategy that do not have any open {@link ch.algotrader.entity.Position}s
     * @param strategyName
     * @return List<Subscription>
     */
    List<Subscription> findNonPositionSubscriptions(String strategyName);

    /**
     * Finds Subscriptions for the specified Strategy and SecurityType that do not have any open
     * {@link ch.algotrader.entity.Position}s.
     * @param strategyName
     * @param type security type (class)
     * @return List<Subscription>
     */
    List<Subscription> findNonPositionSubscriptionsByType(String strategyName, Class<? extends Security> type);


    /**
     * Finds all Securities and corresponding feed types that are subscribed by at least one
     * Strategy which is marked as {@code autoActive} and the specified {@code feedType}.
     * @return List<Map>
     */
    List<Pair<Security, String>> findSubscribedAndFeedTypeForAutoActivateStrategies();

    // spring-dao merge-point
}
