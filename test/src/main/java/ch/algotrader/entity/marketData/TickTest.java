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
package ch.algotrader.entity.marketData;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickTest extends EntityTest {

    private TickDao tickDao;

    @Before
    public void before() {

        this.tickDao = ServiceLocator.instance().getService("tickDao", TickDao.class);
    }

    @Test
    public void testFindByIdInclSecurityAndSecurityFamily() {

        this.tickDao.findByIdsInclSecurityAndUnderlying(Collections.singleton(0));
    }

    @Test
    public void testFindBySecurity() {

        this.tickDao.findBySecurity(0);
    }

    @Test
    public void testFindBySecurityAndMaxDate() {

        this.tickDao.findBySecurityAndMaxDate(0, null);
    }

    @Test
    public void testFindDailyTickIdsAfterTime() {

        this.tickDao.findDailyTickIdsAfterTime(0, null);
    }

    @Test
    public void testFindDailyTickIdsBeforeTime() {

        this.tickDao.findDailyTickIdsBeforeTime(0, null);
    }

    @Test
    public void testFindHourlyTickIdsAfterMinutesByMinDate() {

        this.tickDao.findHourlyTickIdsAfterMinutesByMinDate(0, 0, null);
    }

    @Test
    public void testFindHourlyTickIdsBeforeMinutesByMinDate() {

        this.tickDao.findHourlyTickIdsBeforeMinutesByMinDate(0, 0, null);
    }

    @Test
    public void testFindImpliedVolatilityTicksBySecurityAndDate() {

        this.tickDao.findImpliedVolatilityTicksBySecurityAndDate(0, null);
    }

    @Test
    public void testFindImpliedVolatilityTicksBySecurityDateAndDuration() {

        this.tickDao.findImpliedVolatilityTicksBySecurityDateAndDuration(0, null, null);
    }

    @Test
    public void testFindOptionTicksBySecurityDateTypeAndExpirationInclSecurity() {

        this.tickDao.findOptionTicksBySecurityDateTypeAndExpirationInclSecurity(0, null, null, null);
    }

    @Test
    public void testFindSubscribedByTimePeriod() {

        this.tickDao.findSubscribedByTimePeriod(null, null);
    }

    @Test
    public void testFindTicksBySecurityAndMaxDate() {

        this.tickDao.findTicksBySecurityAndMaxDate(0, null, 0);
    }

    @Test
    public void testFindTicksBySecurityAndMinDate() {

        this.tickDao.findTicksBySecurityAndMinDate(0, null, 0);
    }
}
