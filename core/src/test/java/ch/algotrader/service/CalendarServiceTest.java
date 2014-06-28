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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.entity.security.Exchange;
import ch.algotrader.entity.security.ExchangeDao;
import ch.algotrader.entity.security.Holiday;
import ch.algotrader.enumeration.WeekDay;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CalendarServiceTest {

    private static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat dayTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Mock private ExchangeDao exchangeDao;

    private CalendarServiceImpl impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new CalendarServiceImpl();
        this.impl.setExchangeDao(this.exchangeDao);

        Exchange regular = Exchange.Factory.newInstance("regular", null, WeekDay.MONDAY, hourFormat.parse("10:00:00"), hourFormat.parse("16:00:00"));
        Exchange forex = Exchange.Factory.newInstance("forex", null, WeekDay.SUNDAY, hourFormat.parse("23:15:00"), hourFormat.parse("23:00:00"));
        Exchange roundTheClock = Exchange.Factory.newInstance("roundTheClock", null, WeekDay.MONDAY, hourFormat.parse("00:00:00"), hourFormat.parse("00:00:00"));

        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("01.01.2013"), regular)); // New Years
        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("24.12.2013"), null, hourFormat.parse("12:00:00"), regular)); // christmas
        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("02.01.2013"), hourFormat.parse("12:00:00"), null, regular)); // Jan 2nd

        forex.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("01.01.2013"), forex)); // New Years
        forex.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("24.12.2013"), null, hourFormat.parse("12:00:00"), forex)); // christmas
        forex.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("02.01.2013"), hourFormat.parse("12:00:00"), null, forex)); // Jan 2nd

        roundTheClock.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("01.01.2013"), roundTheClock)); // New Years
        roundTheClock.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("24.12.2013"), null, hourFormat.parse("12:00:00"), roundTheClock)); // christmas
        roundTheClock.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("02.01.2013"), hourFormat.parse("12:00:00"), null, roundTheClock)); // Jan 2nd

        Mockito.when(this.exchangeDao.get(1)).thenReturn(regular);
        Mockito.when(this.exchangeDao.get(2)).thenReturn(forex);
        Mockito.when(this.exchangeDao.get(3)).thenReturn(roundTheClock);
    }

    // Regular

    @Test
    public void testIsExchangeOpenRegularBetween() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(1, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsExchangeOpenRegularBefore() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("20.11.2013 09:00:00"))); // Wed
    }

    @Test
    public void testIsExchangeOpenRegularAfter() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("20.11.2013 17:00:00"))); // Wed
    }

    @Test
    public void testIsExchangeOpenRegularWeekend() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
    }

    // Regular Holidays

    @Test
    public void testIsExchangeOpenRegularHoliday() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("01.01.2013 12:00:00"))); // New Years
    }

    @Test
    public void testIsExchangeOpenEarlyCloseRegularBefore() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(1, dayTimeFormat.parse("24.12.2013 11:00:00"))); // Christmas
    }

    @Test
    public void testIsExchangeOpenEarlyCloseRegularAfter() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("24.12.2013 13:00:00"))); // Christmas
    }

    @Test
    public void testIsExchangeOpenLateOpenRegularBefore() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("02.01.2013 11:00:00"))); // Jan 2nd
    }

    @Test
    public void testIsExchangeOpenLateOpenRegularAfter() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(1, dayTimeFormat.parse("02.01.2013 13:00:00"))); // Jan 2nd
    }

    // Forex

    @Test
    public void testIsExchangeOpenForexBetween() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(2, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsExchangeOpenForexAfter() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(2, dayTimeFormat.parse("20.11.2013 23:07:00"))); // Wed
    }

    @Test
    public void testIsExchangeOpenForexWeekend() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
    }

    @Test
    public void testIsExchangeOpenForexWeekendBetween() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(2, dayTimeFormat.parse("24.11.2013 23:30:00"))); // Sun
    }

    @Test
    public void testIsExchangeOpenForexWeekendAfter() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(2, dayTimeFormat.parse("22.11.2013 23:20:00"))); // Fri
    }

    // ForexHolidays

    @Test
    public void testIsExchangeOpenForexHoliday() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("01.01.2013 12:00:00"))); // New Years
    }

    @Test
    public void testIsExchangeOpenEarlyCloseForexBefore() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(1, dayTimeFormat.parse("24.12.2013 11:00:00"))); // Christmas
    }

    @Test
    public void testIsExchangeOpenEarlyCloseForexAfter() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("24.12.2013 13:00:00"))); // Christmas
    }

    @Test
    public void testIsExchangeOpenLateOpenForexBefore() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("02.01.2013 11:00:00"))); // Jan 2nd
    }

    @Test
    public void testIsExchangeOpenLateOpenForexAfter() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(1, dayTimeFormat.parse("02.01.2013 13:00:00"))); // Jan 2nd
    }

    // 24h

    @Test
    public void testIsExchangeOpen24hBetween() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(3, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsExchangeOpen24hMidnight() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(3, dayTimeFormat.parse("20.11.2013 24:00:00"))); // Wed
    }

    @Test
    public void testIsExchangeOpen24hWeekend() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(3, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
    }

    // 24h Holidays

    @Test
    public void testIsExchangeOpen24hHoliday() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("01.01.2013 12:00:00"))); // New Years
    }

    @Test
    public void testIsExchangeOpenEarlyClose24hBefore() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(1, dayTimeFormat.parse("24.12.2013 11:00:00"))); // Christmas
    }

    @Test
    public void testIsExchangeOpenEarlyClose24hAfter() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("24.12.2013 13:00:00"))); // Christmas
    }

    @Test
    public void testIsExchangeOpenLateOpen24hBefore() throws Exception {
        Assert.assertFalse(this.impl.isExchangeOpen(1, dayTimeFormat.parse("02.01.2013 11:00:00"))); // Jan 2nd
    }

    @Test
    public void testIsExchangeOpenLateOpen24hAfter() throws Exception {
        Assert.assertTrue(this.impl.isExchangeOpen(1, dayTimeFormat.parse("02.01.2013 13:00:00"))); // Jan 2nd
    }

    // Trading day

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
        Assert.assertEquals(this.impl.getOpenTime(1, dayFormat.parse("20.11.2013")), dayTimeFormat.parseObject("20.11.2013 10:00:00")); // Wed
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
        Assert.assertEquals(this.impl.getOpenTime(1, dayFormat.parse("02.01.2013")), dayTimeFormat.parseObject("02.01.2013 12:00:00")); // Jan 2nd
    }

    // Close Time

    @Test
    public void testCloseTime() throws Exception {
        Assert.assertEquals(this.impl.getCloseTime(1, dayFormat.parse("20.11.2013")), dayTimeFormat.parseObject("20.11.2013 16:00:00")); // Wed
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
        Assert.assertEquals(this.impl.getCloseTime(1, dayFormat.parse("24.12.2013")), dayTimeFormat.parseObject("24.12.2013 12:00:00")); // Jan 2nd
    }

}
