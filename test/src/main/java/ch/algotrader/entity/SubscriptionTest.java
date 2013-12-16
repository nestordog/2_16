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
package ch.algotrader.entity;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class SubscriptionTest extends EntityTest{

    private SubscriptionDao subscriptionDao;

    @Before
    public void before() {

        this.subscriptionDao = ServiceLocator.instance().getService("subscriptionDao", SubscriptionDao.class);
    }

    @Test
    public void testFindBySecurityForAutoActivateStrategies() {

        this.subscriptionDao.findBySecurityForAutoActivateStrategies(1);
    }

    @Test
    public void testFindByStrategy(){

        this.subscriptionDao.findByStrategy(null);
    }

    @Test
    public void testFindByStrategyAndSecurity(){

        this.subscriptionDao.findByStrategyAndSecurity(1, null, 1);
    }

    @Test
    public void testFindForAutoActivateStrategies(){

        this.subscriptionDao.findForAutoActivateStrategies();
    }

    @Test
    public void testFindNonPersistent(){

        this.subscriptionDao.findNonPersistent();
    }

    @Test
    public void testfindNonPositionSubscriptions(){

        this.subscriptionDao.findNonPositionSubscriptions(null);
    }

    @Test
    public void testfindNonPositionSubscriptionsByType(){

        this.subscriptionDao.findNonPositionSubscriptionsByType(null, 1);
    }
}
