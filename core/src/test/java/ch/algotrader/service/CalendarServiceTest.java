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
package ch.algotrader.service;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.entity.security.Exchange;
import ch.algotrader.entity.security.ExchangeDao;
import ch.algotrader.entity.security.Holiday;
import ch.algotrader.entity.security.TradingHours;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CalendarServiceTest {

    private static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy");

    private static final SimpleDateFormat dayTimeLocalFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss zz", Locale.ENGLISH);

    @Mock private ExchangeDao exchangeDao;

    private CalendarServiceImpl impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new CalendarServiceImpl(exchangeDao);
        //this.impl.setExchangeDao(this.exchangeDao);

        Exchange regular = Exchange.Factory.newInstance("REGULAR", "REG", "US/Eastern");
        regular.addTradingHours(TradingHours.Factory.newInstance(hourFormat.parse("08:30:00"), hourFormat.parse("16:00:00"), false, true, true, true, true, true, false, regular));
        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("31.12.2012"), regular)); // New Years Eve
        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("01.01.2013"), regular)); // New Year
        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("24.12.2012"), null, hourFormat.parse("12:00:00"), regular)); // Christmas
        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("02.01.2013"), hourFormat.parse("12:00:00"), null, regular)); // Jan 2nd

        Exchange forex = Exchange.Factory.newInstance("FOREX", "FX", "Europe/Berlin");
        forex.addTradingHours(TradingHours.Factory.newInstance(hourFormat.parse("23:15:00"), hourFormat.parse("23:00:00"), true, true, true, true, true, false, false, forex));
        forex.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("31.12.2012"), forex)); // New Years Eve
        forex.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("01.01.2013"), forex)); // New Year
        forex.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("23.12.2012"), null, hourFormat.parse("22:00:00"), forex)); // Christmas
        forex.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("02.01.2013"), hourFormat.parse("23:30:00"), null, forex)); // Jan 2nd

        Exchange roundTheClock = Exchange.Factory.newInstance("ROUND_THE_CLOCK", "24H", "Europe/Berlin");
        roundTheClock.addTradingHours(TradingHours.Factory.newInstance(hourFormat.parse("00:00:00"), hourFormat.parse("23:59:59"), false, true, true, true, true, true, false, roundTheClock));

        Exchange futures = Exchange.Factory.newInstance("FUTURES", "FUT", "Europe/Berlin");
        futures.addTradingHours(TradingHours.Factory.newInstance(hourFormat.parse("10:00:00"), hourFormat.parse("16:00:00"), false, true, true, true, true, true, false, futures));
        futures.addTradingHours(TradingHours.Factory.newInstance(hourFormat.parse("17:00:00"), hourFormat.parse("18:00:00"), false, true, true, true, true, true, false, futures));

        Mockito.when(this.exchangeDao.get(1)).thenReturn(regular);
        Mockito.when(this.exchangeDao.get(2)).thenReturn(forex);
        Mockito.when(this.exchangeDao.get(3)).thenReturn(roundTheClock);
        Mockito.when(this.exchangeDao.get(4)).thenReturn(futures);
    }

    // Regular
    @Test
    public void testIsOpenRegularBeforeOpen() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.11.2013 14:15:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularAfterOpen() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.11.2013 14:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularBeforeClose() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.11.2013 21:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularAfterClose() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.11.2013 22:15:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularWeekend() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("23.11.2013 17:00:00 CET"))); // Sat
    }

    // Regular Day light savings time
    @Test
    public void testIsOpenRegularBeforeOpenDLS() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.06.2013 14:15:00 CEST"))); // Wed
    }

    @Test
    public void testIsOpenRegularAfterOpenDLS() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.06.2013 14:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularBeforeCloseDLS() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.06.2013 21:45:00 CEST"))); // Wed
    }

    @Test
    public void testIsOpenRegularAfterCloseDLSSwitch() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.06.2013 22:15:00 CET"))); // Wed
    }

    // Regular Day light savings time switch (USA: march 10th, Europe: march 31st)
    @Test
    public void testIsOpenRegularBeforeOpenDLSSwitch() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.03.2013 13:15:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularAfterOpenDLSSwitch() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.03.2013 13:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularBeforeCloseDLSSwitch() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.03.2013 20:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenRegularAfterCloseDLS() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("20.03.2013 21:15:00 CET"))); // Wed
    }

    // Regular Holidays

    @Test
    public void testIsOpenRegularHoliday() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("01.01.2013 12:00:00 CET"))); // New Years
    }

    @Test
    public void testIsOpenEarlyCloseRegularBefore() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, dayTimeLocalFormat.parse("24.12.2012 17:30:00 CET"))); // Christmas
    }

    @Test
    public void testIsOpenEarlyCloseRegularAfter() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("24.12.2012 19:30:00 CET"))); // Christmas
    }

    @Test
    public void testIsOpenLateOpenRegularBefore() throws Exception {
        Assert.assertFalse(this.impl.isOpen(1, dayTimeLocalFormat.parse("02.01.2013 17:30:00 CET"))); // Jan 2nd
    }

    @Test
    public void testIsOpenLateOpenRegularAfter() throws Exception {
        Assert.assertTrue(this.impl.isOpen(1, dayTimeLocalFormat.parse("02.01.2013 19:30:00 CET"))); // Jan 2nd
    }

    // Forex

    @Test
    public void testIsOpenForexBetween() throws Exception {
        Assert.assertTrue(this.impl.isOpen(2, dayTimeLocalFormat.parse("20.11.2013 12:00:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenForexAfter() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, dayTimeLocalFormat.parse("20.11.2013 23:07:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenForexWeekend() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, dayTimeLocalFormat.parse("23.11.2013 17:00:00 CET"))); // Sat
    }

    @Test
    public void testIsOpenForexWeekendBetween() throws Exception {
        Assert.assertTrue(this.impl.isOpen(2, dayTimeLocalFormat.parse("24.11.2013 23:30:00 CET"))); // Sun
    }

    @Test
    public void testIsOpenForexWeekendAfter() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, dayTimeLocalFormat.parse("22.11.2013 23:20:00 CET"))); // Fri
    }

    // ForexHolidays

    @Test
    public void testIsOpenForexHoliday() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, dayTimeLocalFormat.parse("01.01.2013 12:00:00 CET"))); // New Years
    }

    @Test
    public void testIsOpenForexEarlyCloseBefore() throws Exception {
        Assert.assertTrue(this.impl.isOpen(2, dayTimeLocalFormat.parse("24.12.2012 21:00:00 CET"))); // Christmas
    }

    @Test
    public void testIsOpenForexEarlyCloseAfter() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, dayTimeLocalFormat.parse("24.12.2012 23:00:00 CET"))); // Christmas
    }

    @Test
    public void testIsOpenForexLateOpenBefore() throws Exception {
        Assert.assertFalse(this.impl.isOpen(2, dayTimeLocalFormat.parse("02.01.2013 23:15:00 CET"))); // Jan 2nd
    }

    @Test
    public void testIsOpenForexLateOpenAfter() throws Exception {
        Assert.assertTrue(this.impl.isOpen(2, dayTimeLocalFormat.parse("02.01.2013 23:45:00 CET"))); // Jan 2nd
    }

    // Futures

    @Test
    public void testIsOpenFuturesBeforeOpen() throws Exception {
        Assert.assertFalse(this.impl.isOpen(4, dayTimeLocalFormat.parse("20.11.2013 09:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenFuturesAfterOpen() throws Exception {
        Assert.assertTrue(this.impl.isOpen(4, dayTimeLocalFormat.parse("20.11.2013 10:15:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenFuturesBeforeClose() throws Exception {
        Assert.assertTrue(this.impl.isOpen(4, dayTimeLocalFormat.parse("20.11.2013 15:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenFuturesAfterClose() throws Exception {
        Assert.assertFalse(this.impl.isOpen(4, dayTimeLocalFormat.parse("20.11.2013 18:15:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenFuturesBeforeGap() throws Exception {
        Assert.assertTrue(this.impl.isOpen(4, dayTimeLocalFormat.parse("20.11.2013 15:45:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenFuturesDuringGap() throws Exception {
        Assert.assertFalse(this.impl.isOpen(4, dayTimeLocalFormat.parse("20.11.2013 16:30:00 CET"))); // Wed
    }

    @Test
    public void testIsOpenFuturesAfterGap() throws Exception {
        Assert.assertTrue(this.impl.isOpen(4, dayTimeLocalFormat.parse("20.11.2013 17:15:00 CET"))); // Wed
    }

    // 24h

    @Test
    public void testIsOpen24hBetween() throws Exception {
        Assert.assertTrue(this.impl.isOpen(3, dayTimeLocalFormat.parse("20.11.2013 12:00:00 CET"))); // Wed
    }

    @Test
    public void testIsOpen24hMidnight() throws Exception {
        Assert.assertTrue(this.impl.isOpen(3, dayTimeLocalFormat.parse("20.11.2013 24:00:00 CET"))); // Wed
    }

    @Test
    public void testIsOpen24hWeekend() throws Exception {
        Assert.assertFalse(this.impl.isOpen(3, dayTimeLocalFormat.parse("23.11.2013 17:00:00 CET"))); // Sat
    }

    // is Trading day

    @Test
    public void testIsTradingDay() throws Exception {
        Assert.assertTrue(this.impl.isTradingDay(1, dayFormat.parse("20.11.2013"))); // Wed
    }

    @Test
    public void testIsTradingDayWeekend() throws Exception {
        Assert.assertFalse(this.impl.isTradingDay(1, dayFormat.parse("23.11.2013"))); // Sat
    }

    @Test
    public void testIsTradingDayHoliday() throws Exception {
        Assert.assertFalse(this.impl.isTradingDay(1, dayFormat.parse("01.01.2013"))); // New Years
    }

    // Open Time

    @Test
    public void testOpenTime() throws Exception {
        Assert.assertEquals(this.impl.getOpenTime(1, dayFormat.parse("20.11.2013")), dayTimeLocalFormat.parseObject("20.11.2013 14:30:00 CET")); // Wed
    }

    @Test
    public void testOpenTimeWeekend() throws Exception {
        Assert.assertNull(this.impl.getOpenTime(1, dayFormat.parse("23.11.2013"))); // Sat
    }

    @Test
    public void testOpenTimeHoliday() throws Exception {
        Assert.assertNull(this.impl.getOpenTime(1, dayFormat.parse("01.01.2013"))); // New Years
    }

    @Test
    public void testLateOpenTime() throws Exception {
        Assert.assertEquals(this.impl.getOpenTime(1, dayFormat.parse("02.01.2013")), dayTimeLocalFormat.parseObject("02.01.2013 18:00:00 CET")); // Jan 2nd
    }

    // Close Time

    @Test
    public void testCloseTime() throws Exception {
        Assert.assertEquals(this.impl.getCloseTime(1, dayFormat.parse("20.11.2013")), dayTimeLocalFormat.parseObject("20.11.2013 22:00:00 CET")); // Wed
    }

    @Test
    public void testCloseTimeWeekend() throws Exception {
        Assert.assertNull(this.impl.getCloseTime(1, dayFormat.parse("23.11.2013"))); // Sat
    }

    @Test
    public void testCloseTimeHoliday() throws Exception {
        Assert.assertNull(this.impl.getCloseTime(1, dayFormat.parse("01.01.2013"))); // New Years
    }

    @Test
    public void testEarlyCloseTime() throws Exception {
        Assert.assertEquals(this.impl.getCloseTime(1, dayFormat.parse("24.12.2012")), dayTimeLocalFormat.parseObject("24.12.2012 18:00:00 CET")); // Jan 2nd
    }

}
