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

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyDao;

/**
 * {@link StrategyPersistenceService} implementation that directly
 * commits strategy entities to a persistent store.
 *
 * @author <a href="mailto:mterzer@algotrader.ch">Marco Terzer</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class StrategyPersistenceServiceImpl implements StrategyPersistenceService {

    private final StrategyDao strategyDao;

    public StrategyPersistenceServiceImpl(final StrategyDao strategyDao) {

        Validate.notNull(strategyDao, "StrategyDao is null");

        this.strategyDao = strategyDao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void persistStrategy(Strategy strategy) {

        Validate.notNull(strategy, "Strategy is null");

        strategyDao.save(strategy);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Strategy getOrCreateStrategy(String name, double allocation) {

        Validate.notNull(name, "Name is null");

        Strategy strategy = strategyDao.findByName(name);
        if (strategy == null) {
            strategy = Strategy.Factory.newInstance(name, true, allocation);
            strategyDao.save(strategy);
        } else if (strategy.getAllocation() != allocation) {
            strategy.setAllocation(allocation);
            strategyDao.save(strategy);
        }
        return strategy;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteStrategy(final long strageyId) {
        this.strategyDao.deleteById(strageyId);
    }
}
