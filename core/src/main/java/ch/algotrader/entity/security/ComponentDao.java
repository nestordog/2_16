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
package ch.algotrader.entity.security;

import java.util.List;

import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.Component} objects.
 *
 * @see ch.algotrader.entity.Component
 */
public interface ComponentDao extends ReadWriteDao<Component> {

    /**
     * Finds all Components where the Combination is subscribed by the defined Strategy.  In
     * addition the Security and Combination are initialized.
     * @param strategyName
     * @return List<Component>
     */
    List<Component> findSubscribedByStrategyInclSecurity(String strategyName);

    /**
     * Finds all Components where the Combination is subscribed by at least one Strategy and where
     * the Security is of the specified {@code securityId}.  In addition the Security and
     * Combination are initialized.
     * @param securityId
     * @return List<Component>
     */
    List<Component> findSubscribedBySecurityInclSecurity(long securityId);

    /**
     * Finds all Components where the Combination is subscribed by the defined Strategy and where
     * the Security is of the specified {@code securityId}.  In addition the Security and
     * Combination are initialized.
     * @param strategyName
     * @param securityId
     * @return List<Component>
     */
    List<Component> findSubscribedByStrategyAndSecurityInclSecurity(String strategyName, long securityId);

    /**
     * Finds non-persistent Components.
     * @return List<Component>
     */
    List<Component> findNonPersistent();

    // spring-dao merge-point
}
