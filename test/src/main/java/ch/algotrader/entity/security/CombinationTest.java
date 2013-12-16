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
package ch.algotrader.entity.security;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;
import ch.algotrader.entity.security.CombinationDao;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class CombinationTest extends EntityTest {

    private CombinationDao combinationDao;

    @Before
    public void before() {

        this.combinationDao = ServiceLocator.instance().getService("combinationDao", CombinationDao.class);
    }

    @Test
    public void testFindNonPersistent() {

        this.combinationDao.findNonPersistent();
    }

    @Test
    public void testFindSubscribedByStrategy() {

        this.combinationDao.findSubscribedByStrategy(null);
    }

    @Test
    public void testFindSubscribedByStrategyAndComponent() {

        this.combinationDao.findSubscribedByStrategyAndComponent(null, 0);
    }

    @Test
    public void testFindSubscribedByStrategyAndComponentType() {

        this.combinationDao.findSubscribedByStrategyAndComponentType(null, 0);
    }

    @Test
    public void testFindSubscribedByStrategyAndComponentTypeWithZeroQty() {

        this.combinationDao.findSubscribedByStrategyAndComponentTypeWithZeroQty(null, 0);
    }

    @Test
    public void testFindSubscribedByStrategyAndUnderlying(){

        this.combinationDao.findSubscribedByStrategyAndUnderlying(null, 0);
    }

}
