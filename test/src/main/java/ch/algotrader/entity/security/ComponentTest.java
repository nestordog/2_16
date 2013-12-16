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
import ch.algotrader.entity.security.ComponentDao;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class ComponentTest extends EntityTest {

    private ComponentDao componentDao;

    @Before
    public void before() {

        this.componentDao = ServiceLocator.instance().getService("componentDao", ComponentDao.class);
    }

    @Test
    public void testFindNonPersistent() {

        this.componentDao.findNonPersistent();
    }

    @Test
    public void testFindSubscribedBySecurityInclSecurity() {

        this.componentDao.findSubscribedBySecurityInclSecurity(0);
    }

    @Test
    public void testFindSubscribedByStrategyAndSecurityInclSecurity() {

        this.componentDao.findSubscribedByStrategyAndSecurityInclSecurity(null, 0);
    }

    @Test
    public void testFindSubscribedByStrategyAndTypeInclSecurity() {

        this.componentDao.findSubscribedByStrategyAndTypeInclSecurity(null, 0);
    }

    @Test
    public void testFindSubscribedByStrategyInclSecurity() {

        this.componentDao.findSubscribedByStrategyInclSecurity(null);
    }

    @Test
    public void testFindSubscribedInclSecurity() {

        this.componentDao.findSubscribedInclSecurity();
    }

}
