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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class HistoricalDataServiceTest extends IBServiceTest {

    @Test
    public void test() {

        HistoricalDataService historicalDataService = ServiceLocator.instance().getHistoricalDataService();
        LookupService lookupService = ServiceLocator.instance().getLookupService();

        historicalDataService.updateHistoricalBars(10, new Date(), 20, TimePeriod.DAY, Duration.DAY_1, BarType.MIDPOINT);

        List<Bar> bars = lookupService.getLastNBarsBySecurityAndBarSize(20, 10, Duration.DAY_1);

        // need at least -1 bars in the db
        Assert.assertTrue(bars.size() >= 19);

        // first bar is not more than 3 days back
        Assert.assertTrue(DateUtils.addDays(bars.get(0).getDateTime(), 3).compareTo(new Date()) > 0);

        // last bar is not more than 1 month back
        Assert.assertTrue(DateUtils.addMonths(bars.get(bars.size() - 1).getDateTime(), 1).compareTo(new Date()) > 0);
    }
}
