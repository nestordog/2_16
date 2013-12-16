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

/**
 * @author <a href="mailto:amasood@algotrader.ch">Ahmad Masood</a>
 *
 * @version $Revision$ $Date$
 */
public class FutureTest extends EntityTest {

    private FutureDao futureDao;

    @Before
    public void before() {

        this.futureDao = ServiceLocator.instance().getService("futureDao", FutureDao.class);
    }

    @Test
    public void testFindByExpirationInclSecurityFamily() {

        this.futureDao.findByExpirationInclSecurityFamily(0, null);
    }

    @Test
    public void testFindByExpirationMonth() {

        this.futureDao.findByExpirationMonth(0, null);
    }

    @Test
    public void testFindByMinExpiration() {

        this.futureDao.findByMinExpiration(0, null);
    }

    @Test
    public void testFindFuturesBySecurityFamily() {

        this.futureDao.findBySecurityFamily(0);
    }

    @Test
    public void testFindSubscribedFutures() {

        this.futureDao.findSubscribedFutures();
    }

}
