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
package ch.algotrader.util;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.enumeration.WeekDay;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DateUtilMarketHourTest {

    private static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dayTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public SecurityFamily regular;
    public SecurityFamily forex;
    public SecurityFamily roundTheClock;

    @Before
    public void before() throws Exception {

        this.regular = new SecurityFamilyImpl();
        this.regular.setMarketOpenDay(WeekDay.MONDAY);
        this.regular.setMarketOpen(hourFormat.parse("10:00:00"));
        this.regular.setMarketClose(hourFormat.parse("16:00:00"));

        this.forex = new SecurityFamilyImpl();
        this.forex.setMarketOpenDay(WeekDay.SUNDAY);
        this.forex.setMarketOpen(hourFormat.parse("23:15:00"));
        this.forex.setMarketClose(hourFormat.parse("23:00:00"));

        this.roundTheClock = new SecurityFamilyImpl();
        this.roundTheClock.setMarketOpenDay(WeekDay.MONDAY);
        this.roundTheClock.setMarketOpen(hourFormat.parse("00:00:00"));
        this.roundTheClock.setMarketClose(hourFormat.parse("00:00:00"));
    }

    // Regular

    @Test
    public void testIsMarketOpenRegularBetween() throws Exception {
        Assert.assertTrue(DateUtil.isMarketOpen(this.regular, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenRegularBefore() throws Exception {
        Assert.assertFalse(DateUtil.isMarketOpen(this.regular, dayTimeFormat.parse("20.11.2013 09:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenRegularAfter() throws Exception {
        Assert.assertFalse(DateUtil.isMarketOpen(this.regular, dayTimeFormat.parse("20.11.2013 17:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenRegularWeekend() throws Exception {
        Assert.assertFalse(DateUtil.isMarketOpen(this.regular, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
    }

    // Forex

    @Test
    public void testIsMarketOpenForexBetween() throws Exception {
        Assert.assertTrue(DateUtil.isMarketOpen(this.forex, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenForexAfter() throws Exception {
        Assert.assertFalse(DateUtil.isMarketOpen(this.forex, dayTimeFormat.parse("20.11.2013 23:07:00"))); // Wed
    }

    @Test
    public void testIsMarketOpenForexWeekend() throws Exception {
        Assert.assertFalse(DateUtil.isMarketOpen(this.regular, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
    }

    @Test
    public void testIsMarketOpenForexWeekendBetween() throws Exception {
        Assert.assertTrue(DateUtil.isMarketOpen(this.forex, dayTimeFormat.parse("24.11.2013 23:30:00"))); // Sun
    }

    @Test
    public void testIsMarketOpenForexWeekendAfter() throws Exception {
        Assert.assertFalse(DateUtil.isMarketOpen(this.forex, dayTimeFormat.parse("22.11.2013 23:20:00"))); // Fri
    }


    // 24h

    @Test
    public void testIsMarketOpen24hBetween() throws Exception {
        Assert.assertTrue(DateUtil.isMarketOpen(this.roundTheClock, dayTimeFormat.parse("20.11.2013 12:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpen24hMidnight() throws Exception {
        Assert.assertTrue(DateUtil.isMarketOpen(this.roundTheClock, dayTimeFormat.parse("20.11.2013 24:00:00"))); // Wed
    }

    @Test
    public void testIsMarketOpen24hWeekend() throws Exception {
        Assert.assertFalse(DateUtil.isMarketOpen(this.roundTheClock, dayTimeFormat.parse("23.11.2013 17:00:00"))); // Sat
    }
}
