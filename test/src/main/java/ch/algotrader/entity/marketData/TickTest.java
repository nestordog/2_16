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
package ch.algotrader.entity.marketData;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.EntityTest;
import ch.algotrader.entity.marketData.TickDao;

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
    public void findByIdInclSecurityAndSecurityFamily() {

        this.tickDao.findByIdsInclSecurityAndUnderlying(new ArrayList<Integer>());
    }

    @Test
    public void findBySecurity() {

        this.tickDao.findBySecurity(0);
    }

    @Test
    public void findBySecurityAndMaxDate() {

        this.tickDao.findBySecurityAndMaxDate(0, null);
    }

    @Test
    public void findCurrentTicksByStrategy() {

        this.tickDao.findCurrentTicksByStrategy(null);
    }

    @Test
    public void findDailyTickIdsAfterTime() {

        this.tickDao.findDailyTickIdsAfterTime(0, null);
    }

    @Test
    public void findDailyTickIdsBeforeTime() {

        this.tickDao.findDailyTickIdsBeforeTime(0, null);
    }

    @Test
    public void findHourlyTickIdsAfterMinutesByMinDate() {

        this.tickDao.findHourlyTickIdsAfterMinutesByMinDate(0, 0, null);
    }

    @Test
    public void findHourlyTickIdsBeforeMinutesByMinDate() {

        this.tickDao.findHourlyTickIdsBeforeMinutesByMinDate(0, 0, null);
    }

    @Test
    public void findImpliedVolatilityTicksBySecurityAndDate() {

        this.tickDao.findImpliedVolatilityTicksBySecurityAndDate(0, null);
    }

    @Test
    public void findImpliedVolatilityTicksBySecurityDateAndDuration() {

        this.tickDao.findImpliedVolatilityTicksBySecurityDateAndDuration(0, null, null);
    }

    @Test
    public void findOptionTicksBySecurityDateTypeAndExpirationInclSecurity() {

        this.tickDao.findOptionTicksBySecurityDateTypeAndExpirationInclSecurity(0, null, null, null);
    }

    @Test
    public void findSubscribedByTimePeriod() {

        this.tickDao.findSubscribedByTimePeriod(null, null);
    }

    @Test
    public void findTickerIdBySecurity() {

        this.tickDao.findTickerIdBySecurity(0);
    }

    @Test
    public void findTicksBySecurityAndMaxDate() {

        this.tickDao.findTicksBySecurityAndMaxDate(0, null, 0);
    }

    @Test
    public void findTicksBySecurityAndMinDate() {

        this.tickDao.findTicksBySecurityAndMinDate(0, null, 0);
    }
}
