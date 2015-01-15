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
package ch.algotrader.entity.strategy;

import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.strategy.DefaultOrderPreference} objects.
 *
 * @see ch.algotrader.entity.strategy.DefaultOrderPreference
 */
public interface DefaultOrderPreferenceDao extends ReadWriteDao<DefaultOrderPreference> {

    /**
     * Finds a DefaultOrderPreference by the specified Strategy and SecurityFamily with their
     * corresponding OrderPreference fetched.
     * @param strategyName
     * @param securityFamilyId
     * @return DefaultOrderPreference
     */
    DefaultOrderPreference findByStrategyAndSecurityFamilyInclOrderPreference(String strategyName, int securityFamilyId);

    // spring-dao merge-point
}
