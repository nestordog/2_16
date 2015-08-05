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
package ch.algotrader.dao;

import java.util.List;

import ch.algotrader.entity.Subscription;
import ch.algotrader.enumeration.FeedType;

/**
 * DAO for {@link ch.algotrader.entity.Subscription} objects.
 *
 * @see ch.algotrader.entity.Subscription
 */
public interface SubscriptionDao extends ReadWriteDao<Subscription> {

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
    Subscription findByStrategySecurityAndFeedType(String strategyName, long securityId, FeedType feedType);

    /**
     * Finds all Subscriptions By Security and {@code feedType} for Strategies that are marked
     * {@code autoActivate}
     * @param securityId
     * @param feedType
     * @return List<Subscription>
     */
    List<Subscription> findBySecurityAndFeedTypeForAutoActivateStrategies(long securityId, FeedType feedType);

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
     * @param type The Security Type which has to be defined as an {@code int} using
     * {@link ch.algotrader.util.HibernateUtil#getDisriminatorValue HibernateUtil}
     * @return List<Subscription>
     */
    List<Subscription> findNonPositionSubscriptionsByType(String strategyName, int type);

    // spring-dao merge-point
}
