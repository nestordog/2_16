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
package ch.algotrader.dao.strategy;

import java.util.Date;
import java.util.Set;

import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.strategy.Strategy} objects.
 *
 * @see ch.algotrader.entity.strategy.Strategy
 */
public interface StrategyDao extends ReadWriteDao<Strategy> {

    /**
     * Returns a Strategy representing the AlgoTrader Server.
     * @return Strategy
     */
    public Strategy findServer();

    /**
     * Finds an instance of Strategy with the given name.
     *
     * @param name
     * @return Strategy
     */
    public Strategy findByName(String name);

    /**
     * Finds all Strategies which are marked as {@code autoActivate}
     * @return List<Strategy>
     */
    public Set<Strategy> findAutoActivateStrategies();

    /**
     * Returns the current database time. This is a DB Query that will always hit the database and
     * make sure the DB is reachable.
     * @return Date
     */
    public Date findCurrentDBTime();

}