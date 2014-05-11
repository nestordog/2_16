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
package ch.algotrader.entity;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;

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
    public void testFindByIdInclSecurityAndSecurityFamily() {

        this.positionDao.findByIdInclSecurityAndSecurityFamily(0);
    }

    @Test
    public void testFindBySecurityAndStrategy() {

        this.positionDao.findBySecurityAndStrategy(0, null);
    }

    @Test
    public void testFindBySecurityAndStrategyIdLocked() {

        this.positionDao.findBySecurityAndStrategyIdLocked(0, 0);
    }

    @Test
    public void testFindByStrategy() {

        this.positionDao.findByStrategy(null);
    }

    @Test
    public void testFindExpirablePositions() {

        this.positionDao.findExpirablePositions(null);
    }

    @Test
    public void testFindNonPersistent() {

        this.positionDao.findNonPersistent();
    }

    @Test
    public void testFindOpenFXPositions() {

        this.positionDao.findOpenFXPositions();
    }

    @Test
    public void testFindOpenFXPositionsAggregated() {

        this.positionDao.findOpenFXPositionsAggregated();
    }

    @Test
    public void testFindOpenFXPositionsByStrategy() {

        this.positionDao.findOpenFXPositionsByStrategy(null);
    }

    @Test
    public void testFindOpenPositions() {

        this.positionDao.findOpenPositions();
    }

    @Test
    public void testFindOpenPositionsAggregated() {

        this.positionDao.findOpenPositionsAggregated();
    }

    @Test
    public void testFindOpenPositionsByMaxDateAggregated() {

        this.positionDao.findOpenPositionsByMaxDateAggregated(null);
    }

    @Test
    public void testFindOpenPositionsBySecurity() {

        this.positionDao.findOpenPositionsBySecurity(0);
    }

    @Test
    public void testFindOpenPositionsByStrategy() {

        this.positionDao.findOpenPositionsByStrategy(null);
    }

    @Test
    public void testFindOpenPositionsByStrategyAndMaxDate() {

        this.positionDao.findOpenPositionsByStrategyAndMaxDate(null, null);
    }

    @Test
    public void testFindOpenPositionsByStrategyAndSecurityFamily() {

        this.positionDao.findOpenPositionsByStrategyAndSecurityFamily(null, 0);
    }

    @Test
    public void testFindOpenPositionsByStrategyAndType() {

        this.positionDao.findOpenPositionsByStrategyAndType(null, 0);
    }

    @Test
    public void testFindOpenPositionsByStrategyTypeAndUnderlyingType() {

        this.positionDao.findOpenPositionsByStrategyTypeAndUnderlyingType(null, 0, 0);
    }

    @Test
    public void testFindOpenPositionsByUnderlying() {

        this.positionDao.findOpenPositionsByUnderlying(0);
    }

    @Test
    public void testFindOpenPositionsFromTransactions() {

        this.positionDao.findOpenPositionsFromTransactions();
    }

    @Test
    public void testFindOpenTradeablePositions() {

        this.positionDao.findOpenTradeablePositions();
    }

    @Test
    public void testFindOpenTradeablePositionsAggregated() {

        this.positionDao.findOpenTradeablePositionsAggregated();
    }

    @Test
    public void testFindOpenTradeablePositionsByStrategy() {

        this.positionDao.findOpenTradeablePositionsByStrategy(null);
    }

    @Test
    public void testFindPersistent() {

        this.positionDao.findPersistent();
    }

}
