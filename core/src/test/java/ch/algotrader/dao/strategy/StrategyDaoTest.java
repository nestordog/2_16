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

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.strategy.StrategyDaoImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link StrategyDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class StrategyDaoTest extends InMemoryDBTest {

    private StrategyDao dao;

    public StrategyDaoTest() throws IOException {

        super();
    }

    @Override @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new StrategyDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindServerStrategy() {

        Strategy strategy = this.dao.findServer();

        Assert.assertNotNull(strategy);
    }

    @Test
    public void testFindByName() {

        Strategy strategy1 = this.dao.findByName("blah");
        Assert.assertNull(strategy1);

        Strategy newStrategy = new StrategyImpl();
        newStrategy.setName("blah");

        this.dao.save(newStrategy);
        this.dao.flush();

        Strategy strategy3 = this.dao.findByName("blah");

        Assert.assertNotNull(strategy3);
        Assert.assertEquals("blah", strategy3.getName());
    }

    @Test
    public void testFindAutoActivateStrategies() {

        Set<Strategy> strategies1 = this.dao.findAutoActivateStrategies();

        Assert.assertNotNull(strategies1);

        Assert.assertEquals(1, strategies1.size());

        Strategy newStrategy = new StrategyImpl();
        newStrategy.setName("blah");
        newStrategy.setAutoActivate(true);

        this.dao.save(newStrategy);
        this.dao.flush();

        Set<Strategy> strategies2 = this.dao.findAutoActivateStrategies();

        Assert.assertNotNull(strategies2);
        Assert.assertEquals(2, strategies2.size());
        Assert.assertTrue(strategies2.contains(newStrategy));
    }

    @Test
    public void testFindCurrentDBTime() {

        Date currentDBTime = this.dao.findCurrentDBTime();

        Assert.assertNotNull(currentDBTime);
    }

}
