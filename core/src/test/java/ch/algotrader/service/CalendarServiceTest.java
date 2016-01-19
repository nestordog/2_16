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
import java.time.LocalDate;
import java.time.Month;
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
import ch.algotrader.entity.exchange.Holiday;
import ch.algotrader.entity.exchange.TradingHours;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CalendarServiceTest {

    private static Date parseAsTime(final CharSequence s) {
        Instant instant = DateTimeUtil.parseLocalTime(s)
                .atDate(LocalDate.of(1970, Month.JANUARY, 1)).atZone(ZoneId.systemDefault()).toInstant();
        return new Date(instant.toEpochMilli());
    }

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
        regular.addTradingHours(TradingHours.Factory.newInstance(parseAsTime("08:30:00"), parseAsTime("16:00:00"), false, true, true, true, true, true, false, regular));
        regular.addHolidays(Holiday.Factory.newInstance(parseAsDate("2012-12-31"), regular)); // New Years Eve
        regular.addHolidays(Holiday.Factory.newInstance(parseAsDate("2013-01-01"), regular)); // New Year
        regular.addHolidays(Holiday.Factory.newInstance(parseAsDate("2012-12-24"), null, parseAsTime("12:00:00"), regular)); // Christmas
        regular.addHolidays(Holiday.Factory.newInstance(parseAsDate("2013-01-02"), parseAsTime("12:00:00"), null, regular)); // Jan 2nd

        Exchange forex = Exchange.Factory.newInstance("FOREX", "Europe/Berlin");
        forex.addTradingHours(TradingHours.Factory.newInstance(parseAsTime("23:15:00"), parseAsTime("23:00:00"), true, true, true, true, true, false, false, forex));
        forex.addHolidays(Holiday.Factory.newInstance(parseAsDate("2012-12-31"), forex)); // New Years Eve
        forex.addHolidays(Holiday.Factory.newInstance(parseAsDate("2013-01-01"), forex)); // New Year
        forex.addHolidays(Holiday.Factory.newInstance(parseAsDate("2012-12-24"), null, parseAsTime("22:00:00"), forex)); // Christmas
        forex.addHolidays(Holiday.Factory.newInstance(parseAsDate("2013-01-02"), parseAsTime("23:30:00"), null, forex)); // Jan 2nd

        Exchange roundTheClock = Exchange.Factory.newInstance("ROUND_THE_CLOCK", "Europe/Berlin");
        roundTheClock.addTradingHours(TradingHours.Factory.newInstance(parseAsTime("00:00:00"), parseAsTime("23:59:59"), false, true, true, true, true, true, false, roundTheClock));

        Exchange futures = Exchange.Factory.newInstance("FUTURES", "Europe/Berlin");
        futures.addTradingHours(TradingHours.Factory.newInstance(parseAsTime("10:00:00"), parseAsTime("16:00:00"), false, true, true, true, true, true, false, futures));
        futures.addTradingHours(TradingHours.Factory.newInstance(parseAsTime("17:00:00"), parseAsTime("18:00:00"), false, true, true, true, true, true, false, futures));

        Mockito.when(this.exchangeDao.get(1)).thenReturn(regular);
        Mockito.when(this.exchangeDao.get(2)).thenReturn(forex);
        Mockito.when(this.exchangeDao.get(3)).thenReturn(roundTheClock);
        Mockito.when(this.exchangeDao.get(4)).thenReturn(futures);
    }

    // Regular
    @Test
    public void testIsOpenRegularOpen() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 14:15:00 CET"))); // Wed
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 14:20:00 CET"))); // Wed
        Assert.assertEquals(this.impl.getNextOpenTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 14:15:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-11-20 14:30:00 CET"));
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 14:45:00 CET")));
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 14:50:00 CET")));
    }

    @Test
    public void testIsOpenRegularClose() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 21:45:00 CET"))); // Wed
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 21:50:00 CET"))); // Wed
        Assert.assertEquals(this.impl.getNextCloseTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 21:45:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-11-20 22:00:00 CET")); // Jan 2nd
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 22:15:00 CET"))); // Wed
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 22:20:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularWeekend() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-23 17:00:00 CET"))); // Sat
        Assert.assertEquals(this.impl.getNextOpenTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-23 17:00:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-11-25 14:30:00 CET")); // Jan 2nd
    }

    // Regular Day light savings time
    @Test
    public void testIsOpenRegularOpenDLS() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-06-20 14:15:00 CEST"))); // Wed
        Assert.assertEquals(this.impl.getNextOpenTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-06-20 14:15:00 CEST")), DateTimeLegacy.parseAsZonedDateTime("2013-06-20 14:30:00 CEST"));
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-06-20 14:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularCloseDLS() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-06-20 21:45:00 CEST"))); // Wed
        Assert.assertEquals(this.impl.getNextCloseTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-06-20 21:45:00 CEST")), DateTimeLegacy.parseAsZonedDateTime("2013-06-20 22:00:00 CEST"));
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-06-20 22:15:00 CET"))); // Wed
    }

    // Regular Day light savings time switch (USA: march 10th, Europe: march 31st)
    @Test
    public void testIsOpenRegularOpenDLSSwitch() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-03-20 13:15:00 CET"))); // Wed
        Assert.assertEquals(this.impl.getNextOpenTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-03-20 13:15:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-03-20 13:30:00 CET"));
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-03-20 13:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularCloseDLSSwitch() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-03-20 20:45:00 CET"))); // Wed
        Assert.assertEquals(this.impl.getNextCloseTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-03-20 20:45:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-03-20 21:00:00 CET"));
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-03-20 21:15:00 CET"))); // Wed
    }

    // Regular Holidays

    @Test
    public void testIsOpenRegularHoliday() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-01-01 12:00:00 CET"))); // New Years
        Assert.assertEquals(this.impl.getNextOpenTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-01-01 12:00:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-01-02 18:00:00 CET"));
    }

    @Test
    public void testIsOpenEarlyCloseRegular() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2012-12-24 17:30:00 CET"))); // Christmas
        Assert.assertEquals(this.impl.getNextCloseTime(1, DateTimeLegacy.parseAsZonedDateTime("2012-12-24 17:30:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2012-12-24 18:00:00 CET"));
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2012-12-24 19:30:00 CET"))); // Christmas
    }

    @Test
    public void testIsOpenLateOpenRegular() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-01-02 17:30:00 CET"))); // Jan 2nd
        Assert.assertEquals(this.impl.getNextOpenTime(1, DateTimeLegacy.parseAsZonedDateTime("2013-01-02 17:30:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-01-02 18:00:00 CET"));
        Assert.assertTrue(this.impl.isOpen(1, DateTimeLegacy.parseAsZonedDateTime("2013-01-02 19:30:00 CET"))); // Jan 2nd
    }

    // Forex

    @Test
    public void testIsOpenForexBetween() throws Exception {
        Assert.assertTrue(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 12:00:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenForexAfter() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 23:07:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenForexWeekend() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2013-11-23 17:00:00 CET"))); // Sat
    }

    @Test
    public void testIsOpenForexWeekendBetween() throws Exception {
        Assert.assertTrue(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2013-11-24 23:30:00 CET"))); // Sun
    }

    @Test
    public void testIsOpenForexWeekendAfter() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2013-11-22 23:20:00 CET"))); // Fri
    }

    // ForexHolidays

    @Test
    public void testIsOpenForexHoliday() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2013-01-01 12:00:00 CET"))); // New Years
    }

    @Test
    public void testIsOpenForexEarlyClose() throws Exception {
        Assert.assertTrue(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2012-12-24 21:00:00 CET"))); // Christmas
        Assert.assertEquals(this.impl.getNextCloseTime(2, DateTimeLegacy.parseAsZonedDateTime("2012-12-24 21:00:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2012-12-24 22:00:00 CET"));
        Assert.assertFalse(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2012-12-24 23:00:00 CET"))); // Christmas
    }

    @Test
    public void testIsOpenForexLateOpen() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2013-01-01 23:15:00 CET"))); // Jan 2nd
        Assert.assertEquals(this.impl.getNextOpenTime(2, DateTimeLegacy.parseAsZonedDateTime("2013-01-01 23:15:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-01-01 23:30:00 CET"));
        Assert.assertTrue(this.impl.isOpen(2, DateTimeLegacy.parseAsZonedDateTime("2013-01-01 23:45:00 CET"))); // Jan 2nd
    }

    // Futures

    @Test
    public void testIsOpenFuturesOpen() throws Exception {
        Assert.assertFalse(this.impl.isOpen(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 09:45:00 CET"))); // Wed
        Assert.assertEquals(this.impl.getNextOpenTime(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 09:45:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-11-20 10:00:00 CET"));
        Assert.assertTrue(this.impl.isOpen(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 10:15:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenFuturesClose() throws Exception {
        Assert.assertTrue(this.impl.isOpen(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 15:45:00 CET"))); // Wed
        Assert.assertFalse(this.impl.isOpen(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 18:15:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenFuturesGap() throws Exception {
        Assert.assertTrue(this.impl.isOpen(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 15:45:00 CET"))); // Wed
        Assert.assertEquals(this.impl.getNextCloseTime(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 15:45:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-11-20 16:00:00 CET"));
        Assert.assertFalse(this.impl.isOpen(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 16:30:00 CET"))); // Wed
        Assert.assertEquals(this.impl.getNextOpenTime(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 16:30:00 CET")), DateTimeLegacy.parseAsZonedDateTime("2013-11-20 17:00:00 CET"));
        Assert.assertTrue(this.impl.isOpen(4, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 17:15:00 CET"))); // Wed
    }

    // 24h

    @Test
    public void testIsOpen24hBetween() throws Exception {
        Assert.assertTrue(this.impl.isOpen(3, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 12:00:00 CET"))); // Wed
    }

    @Test
    public void testIsOpen24hMidnight() throws Exception {
        Assert.assertTrue(this.impl.isOpen(3, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 24:00:00 CET"))); // Wed
    }

    @Test
    public void testIsOpen24hWeekend() throws Exception {
        Assert.assertFalse(this.impl.isOpen(3, DateTimeLegacy.parseAsZonedDateTime("2013-11-23 17:00:00 CET"))); // Sat
    }

    // is Trading day

    @Test
    public void testIsTradingDay() throws Exception {
        Assert.assertTrue(this.impl.isTradingDay(1, parseAsDate("2013-11-20"))); // Wed
    }

    @Test
    public void testIsTradingDayWeekend() throws Exception {
        Assert.assertFalse(this.impl.isTradingDay(1, parseAsDate("2013-11-23"))); // Sat
    }

    @Test
    public void testIsTradingDayHoliday() throws Exception {
        Assert.assertFalse(this.impl.isTradingDay(1, parseAsDate("2013-01-01"))); // New Years
    }

    // CurrenTradingDay

    @Test
    public void testCurrentTradingDate() throws Exception {
        Assert.assertEquals(this.impl.getCurrentTradingDate(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 18:00:00 CET")), parseAsDate("2013-11-20")); // Wed
    }

    @Test
    public void testCurrentTradingDateFX() throws Exception {
        Assert.assertEquals(this.impl.getCurrentTradingDate(2, DateTimeLegacy.parseAsZonedDateTime("2013-11-20 18:00:00 CET")), parseAsDate("2013-11-20")); // Wed
    }

    @Test
    public void testCurrentTradingDateWeekend() throws Exception {
        Assert.assertEquals(this.impl.getCurrentTradingDate(1, DateTimeLegacy.parseAsZonedDateTime("2013-11-23 18:00:00 CET")), parseAsDate("2013-11-22")); // Sat
    }

    @Test
    public void testCurrentTradingDateHoliday() throws Exception {
        Assert.assertEquals(this.impl.getCurrentTradingDate(1, DateTimeLegacy.parseAsZonedDateTime("2013-01-01 18:00:00 CET")), parseAsDate("2012-12-28")); // New Years
    }

    @Test
    public void testCurrentTradingDateAfterHoliday() throws Exception {
        Assert.assertEquals(this.impl.getCurrentTradingDate(1, DateTimeLegacy.parseAsZonedDateTime("2013-01-02 18:00:00 CET")), parseAsDate("2013-01-02")); // Jan 2nd
    }

    // Open Time

    @Test
    public void testOpenTime() throws Exception {
        Assert.assertEquals(this.impl.getOpenTime(1, parseAsDate("2013-11-20")), DateTimeLegacy.parseAsZonedDateTime("2013-11-20 14:30:00 CET")); // Wed
    }

    @Test
    public void testOpenTimeWeekend() throws Exception {
        Assert.assertNull(this.impl.getOpenTime(1, parseAsDate("2013-11-23"))); // Sat
    }

    @Test
    public void testOpenTimeHoliday() throws Exception {
        Assert.assertNull(this.impl.getOpenTime(1, parseAsDate("2013-01-01"))); // New Years
    }

    @Test
    public void testLateOpenTime() throws Exception {
        Assert.assertEquals(this.impl.getOpenTime(1, parseAsDate("2013-01-02")), DateTimeLegacy.parseAsZonedDateTime("2013-01-02 18:00:00 CET")); // Jan 2nd
    }

    // Close Time

    @Test
    public void testCloseTime() throws Exception {
        Assert.assertEquals(this.impl.getCloseTime(1, parseAsDate("2013-11-20")), DateTimeLegacy.parseAsZonedDateTime("2013-11-20 22:00:00 CET")); // Wed
    }

    @Test
    public void testCloseTimeWeekend() throws Exception {
        Assert.assertNull(this.impl.getCloseTime(1, parseAsDate("2013-11-23"))); // Sat
    }

    @Test
    public void testCloseTimeHoliday() throws Exception {
        Assert.assertNull(this.impl.getCloseTime(1, parseAsDate("2013-01-01"))); // New Years
    }

    @Test
    public void testEarlyCloseTime() throws Exception {
        Assert.assertEquals(this.impl.getCloseTime(1, parseAsDate("2012-12-24")), DateTimeLegacy.parseAsZonedDateTime("2012-12-24 18:00:00 CET")); // Jan 2nd
    }
}
