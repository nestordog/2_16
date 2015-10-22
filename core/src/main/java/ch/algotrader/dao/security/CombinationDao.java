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
package ch.algotrader.dao.security;

import java.util.List;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.security.Combination;

/**
 * DAO for {@link ch.algotrader.entity.security.Combination} objects.
 *
 * @see ch.algotrader.entity.security.Combination
 */
public interface CombinationDao extends ReadWriteDao<Combination> {

    /**
     * Finds Combinations that are subscribed by the specified Strategy.
     * @param strategyName
     * @return Collection<Combination>
     */
    List<Combination> findSubscribedByStrategy(String strategyName);

    /**
     * Finds Combinations that are subscribed by the specified Strategy and have an Underlying
     * corresponding to {@code underlyingId}
     * @param strategyName
     * @param underlyingId
     * @return Collection<Combination>
     */
    List<Combination> findSubscribedByStrategyAndUnderlying(String strategyName, long underlyingId);

    /**
     * Finds Combinations that are subscribed by the specified Strategy and have a Component with
     * the specified {@code securityId}
     * @param strategyName
     * @param securityId
     * @return Collection<Combination>
     */
    List<Combination> findSubscribedByStrategyAndComponent(String strategyName, long securityId);

    /**
     * Finds Combinations that are subscribed by the specified Strategy and have a Component with
     * the specified Security Type.
     * @param strategyName
     * @param type The Security Type which has to be defined as an {@code int} using {@link
    ch.algotrader.util.HibernateUtil#getDisriminatorValue HibernateUtil}
     * @return Collection<Combination>
     */
    List<Combination> findSubscribedByStrategyAndComponentType(String strategyName, int type);

    /**
     * Finds Combinations that are subscribed by the specified Strategy, have a Component with the
     * specified Security Type and a Component quantity of zero.
     * @param strategyName
     * @param type The Security Type which has to be defined as an {@code int} using {@link
    ch.algotrader.util.HibernateUtil#getDisriminatorValue HibernateUtil}
     * @return Collection<Combination>
     */
    List<Combination> findSubscribedByStrategyAndComponentTypeWithZeroQty(String strategyName, int type);

    /**
     * Finds non-persistent Combinations.
     * @return Collection<Combination>
     */
    List<Combination> findNonPersistent();

    // spring-dao merge-point
}
