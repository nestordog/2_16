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

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class SecurityTest extends EntityTest {

    private SecurityDao securityDao;

    @Before
    public void before() {

        this.securityDao = ServiceLocator.instance().getService("securityDao", SecurityDao.class);
    }

    @Test
    public void testFindByBbgid() {

        this.securityDao.findByBbgid(null);
    }

    @Test
    public void testFindByConid() {

        this.securityDao.findByConid(null);
    }

    @Test
    public void testFindById() {
        this.securityDao.findById(0);
    }

    @Test
    public void testFindByIdInclFamilyAndUnderlying() {

        this.securityDao.findByIdInclFamilyAndUnderlying(0);
    }

    @Test
    public void testFindByIds() {

        this.securityDao.findByIds(Collections.singleton(0));
    }

    @Test
    public void testFindByIsin() {

        this.securityDao.findByIsin(null);
    }

    @Test
    public void testFindByRic() {

        this.securityDao.findByRic(null);
    }

    @Test
    public void testFindBySymbol() {

        this.securityDao.findBySymbol(null);
    }

    @Test
    public void testFindSecurityIdByIsin() {

        this.securityDao.findSecurityIdByIsin(null);
    }

    @Test
    public void testFindSubscribed() {

        this.securityDao.findSubscribed();
    }

    @Test
    public void testFindSubscribedForAutoActivateStrategiesInclFamily() {

        this.securityDao.findSubscribedForAutoActivateStrategies();
    }

}
