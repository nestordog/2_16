/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.entity;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.PositionDao;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PositionTest extends EntityTest {

    private PositionDao positionDao;

    @Before
    public void before() {

        this.positionDao = ServiceLocator.instance().getService("positionDao", PositionDao.class);
    }

    @Test
    public void findById() {

        this.positionDao.findById(0);
    }

    @Test
    public void findByIdInclSecurityAndSecurityFamily() {

        this.positionDao.findByIdInclSecurityAndSecurityFamily(0);
    }

    @Test
    public void findBySecurityAndStrategy() {

        this.positionDao.findBySecurityAndStrategy(0, null);
    }

    @Test
    public void findBySecurityAndStrategyIdLocked() {

        this.positionDao.findBySecurityAndStrategyIdLocked(0, 0);
    }

    @Test
    public void findByStrategy() {

        this.positionDao.findByStrategy(null);
    }

    @Test
    public void findExpirablePositions() {

        this.positionDao.findExpirablePositions(null);
    }

    @Test
    public void findNonPersistent() {

        this.positionDao.findNonPersistent();
    }

    @Test
    public void findOpenFXPositions() {

        this.positionDao.findOpenFXPositions();
    }

    @Test
    public void findOpenFXPositionsAggregated() {

        this.positionDao.findOpenFXPositionsAggregated();
    }

    @Test
    public void findOpenFXPositionsByStrategy() {

        this.positionDao.findOpenFXPositionsByStrategy(null);
    }

    @Test
    public void findOpenPositions() {

        this.positionDao.findOpenPositions();
    }

    @Test
    public void findOpenPositionsAggregated() {

        //        this.positionDao.findOpenPositionsAggregated();
    }

    @Test
    public void findOpenPositionsByMaxDateAggregated() {

        //        this.positionDao.findOpenPositionsByMaxDateAggregated(null);
    }

    @Test
    public void findOpenPositionsBySecurity() {

        this.positionDao.findOpenPositionsBySecurity(0);
    }

    @Test
    public void findOpenPositionsByStrategy() {

        this.positionDao.findOpenPositionsByStrategy(null);
    }

    @Test
    public void findOpenPositionsByStrategyAndMaxDate() {

        this.positionDao.findOpenPositionsByStrategyAndMaxDate(null, null);
    }

    @Test
    public void findOpenPositionsByStrategyAndSecurityFamily() {

        this.positionDao.findOpenPositionsByStrategyAndSecurityFamily(null, 0);
    }

    @Test
    public void findOpenPositionsByStrategyAndType() {

        this.positionDao.findOpenPositionsByStrategyAndType(null, 0);
    }

    @Test
    public void findOpenPositionsByStrategyTypeAndUnderlyingType() {

        this.positionDao.findOpenPositionsByStrategyTypeAndUnderlyingType(null, 0, 0);
    }

    @Test
    public void findOpenPositionsByUnderlying() {

        this.positionDao.findOpenPositionsByUnderlying(0);
    }

    @Test
    public void findOpenPositionsFromTransactions() {

        this.positionDao.findOpenPositionsFromTransactions();
    }

    @Test
    public void findOpenTradeablePositions() {

        this.positionDao.findOpenTradeablePositions();
    }

    @Test
    public void findOpenTradeablePositionsAggregated() {

        this.positionDao.findOpenTradeablePositionsAggregated();
    }

    @Test
    public void findOpenTradeablePositionsByStrategy() {

        this.positionDao.findOpenTradeablePositionsByStrategy(null);
    }

    @Test
    public void findPersistent() {

        this.positionDao.findPersistent();
    }

}
