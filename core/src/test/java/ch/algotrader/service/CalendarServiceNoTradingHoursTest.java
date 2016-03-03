/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/

package ch.algotrader.service;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.dao.exchange.ExchangeDao;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CalendarServiceNoTradingHoursTest {

    public static Date parseAsDate(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimeUtil.parseLocalDate(s).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return new Date(instant.toEpochMilli());
    }

    @Mock private ExchangeDao exchangeDao;
    @Mock private EngineManager engineManager;

    private CalendarServiceImpl impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new CalendarServiceImpl(this.exchangeDao, this.engineManager);

        Mockito.when(this.engineManager.getCurrentEPTime()).thenReturn(new Date());

        Exchange regular = Exchange.Factory.newInstance("REGULAR", "US/Eastern");
        Mockito.when(this.exchangeDao.get(1)).thenReturn(regular);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCurrentTradingDate() throws Exception {
        this.impl.getCurrentTradingDate(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 18:00:00 CET"));
    }

    @Test
    public void testIsOpen() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 14:15:00 CET"))); // Wed
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsTradingDay() throws Exception {
        this.impl.isTradingDay(1, parseAsDate("2013-11-20"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOpenTime() throws Exception {
        this.impl.getOpenTime(1, parseAsDate("2013-11-20")); // Wed
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCloseTime() throws Exception {
        this.impl.getCloseTime(1, parseAsDate("2013-11-20"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNexOpenTime() throws Exception {
        this.impl.getNextOpenTime(1, parseAsDate("2013-11-20")); // Wed
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNextCloseTime() throws Exception {
        this.impl.getNextCloseTime(1, parseAsDate("2013-11-20"));
    }

}
