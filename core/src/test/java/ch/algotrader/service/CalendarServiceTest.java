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

import ch.algotrader.entity.security.Holiday;
import ch.algotrader.entity.security.Market;
import ch.algotrader.entity.security.MarketDao;
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

    @Mock private MarketDao marketDao;

    private CalendarServiceImpl impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new CalendarServiceImpl();
        this.impl.setMarketDao(this.marketDao);

        Market regular = Market.Factory.newInstance("regular", WeekDay.MONDAY, hourFormat.parse("10:00:00"), hourFormat.parse("16:00:00"));
        Market forex = Market.Factory.newInstance("forex", WeekDay.SUNDAY, hourFormat.parse("23:15:00"), hourFormat.parse("23:00:00"));
        Market roundTheClock = Market.Factory.newInstance("roundTheClock", WeekDay.MONDAY, hourFormat.parse("00:00:00"), hourFormat.parse("00:00:00"));

        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("24.12.2013"), null, hourFormat.parse("12:00:00"), regular)); // christmas
        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("01.01.2013"), regular)); // New Years
        regular.addHolidays(Holiday.Factory.newInstance(dayFormat.parse("02.01.2013"), hourFormat.parse("12:00:00"), null, regular)); // Jan 2nd

        Mockito.when(this.marketDao.get(1)).thenReturn(regular);
        Mockito.when(this.marketDao.get(2)).thenReturn(forex);
        Mockito.when(this.marketDao.get(3)).thenReturn(roundTheClock);
    }

    // Regular

    @Test
    public void testIsMarketOpenRegularBetween() throws Exception {
        Assert.assertTrue(this.impl.isMarketOpen(1, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenRegularBefore() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(1, dayTimeFormat.parse("20.11.2013 09:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenRegularAfter() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(1, dayTimeFormat.parse("20.11.2013 17:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenRegularWeekend() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(1, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
    }

    // Holidays

    @Test
    public void testIsMarketOpenHoliday() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(1, dayTimeFormat.parse("01.01.2013 12:00:00"))); // New Years
    }

    @Test
    public void testIsMarketOpenEarlyCloseBefore() throws Exception {
        Assert.assertTrue(this.impl.isMarketOpen(1, dayTimeFormat.parse("24.12.2013 11:00:00"))); // Christmas
    }

    @Test
    public void testIsMarketOpenEarlyCloseAfter() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(1, dayTimeFormat.parse("24.12.2013 13:00:00"))); // Christmas
    }

    @Test
    public void testIsMarketOpenLateOpenBefore() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(1, dayTimeFormat.parse("02.01.2013 11:00:00"))); // Jan 2nd
    }

    @Test
    public void testIsMarketOpenLateOpenAfter() throws Exception {
        Assert.assertTrue(this.impl.isMarketOpen(1, dayTimeFormat.parse("02.01.2013 13:00:00"))); // Jan 2nd
    }

    // Forex

    @Test
    public void testIsMarketOpenForexBetween() throws Exception {
        Assert.assertTrue(this.impl.isMarketOpen(2, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenForexAfter() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(2, dayTimeFormat.parse("20.11.2013 23:07:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenForexWeekend() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(1, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
    }

    @Test
    public void testIsMarketOpenForexWeekendBetween() throws Exception {
        Assert.assertTrue(this.impl.isMarketOpen(2, dayTimeFormat.parse("24.11.2013 23:30:00"))); // Sun
    }

    @Test
    public void testIsMarketOpenForexWeekendAfter() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(2, dayTimeFormat.parse("22.11.2013 23:20:00"))); // Fri
    }

    // 24h

    @Test
    public void testIsMarketOpen24hBetween() throws Exception {
        Assert.assertTrue(this.impl.isMarketOpen(3, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpen24hMidnight() throws Exception {
        Assert.assertTrue(this.impl.isMarketOpen(3, dayTimeFormat.parse("20.11.2013 24:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpen24hWeekend() throws Exception {
        Assert.assertFalse(this.impl.isMarketOpen(3, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
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
