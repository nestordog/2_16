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
package ch.algotrader.esper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPOnDemandQueryResult;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EventBean;

import ch.algotrader.entity.marketData.BarVO;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.enumeration.FeedType;

public class CurrentValuesEsperTest extends EsperTestBase {

    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    @Before
    public void setupEsper() {
        Configuration config = new Configuration();
        config.addEventType("MarketDataEventVO", MarketDataEventVO.class.getName());
        config.addEventType("TickVO", TickVO.class.getName());
        config.addEventType("BarVO", BarVO.class.getName());
        config.addImport(FeedType.class);
        config.getEngineDefaults().getExpression().setMathContext(new MathContext(4, RoundingMode.HALF_EVEN));

        epService = EPServiceProviderManager.getDefaultProvider(config);
        epRuntime = epService.getEPRuntime();
    }

    @After
    public void cleanUpEsper() {
        if (epService != null) {
            epService.destroy();
        }
    }

    @Test
    public void testInsertTick() throws Exception {

        deployModule(epService, getClass().getResource("/module-current-values.epl"), "MARKET_DATA_WINDOW", "INSERT_INTO_CURRENT_MARKET_DATA_EVENT");

        TickVO tick1 = new TickVO(0L, new Date(), FeedType.IB, 1L, new BigDecimal("1.11"), new Date(), new BigDecimal("1.12"), new BigDecimal("1.1"), 0, 0, 0);
        epRuntime.sendEvent(tick1);
        TickVO tick2 = new TickVO(0L, new Date(), FeedType.IB, 2L, new BigDecimal("1.21"), new Date(), new BigDecimal("1.22"), new BigDecimal("1.2"), 0, 0, 0);
        epRuntime.sendEvent(tick2);
        TickVO tick3 = new TickVO(0L, new Date(), FeedType.IB, 1L, new BigDecimal("1.12"), new Date(), new BigDecimal("1.13"), new BigDecimal("1.11"), 0, 0, 0);
        epRuntime.sendEvent(tick3);
        TickVO tick4 = new TickVO(0L, new Date(), FeedType.LMAX, 2L, new BigDecimal("1.22"), new Date(), new BigDecimal("1.23"), new BigDecimal("1.21"), 0, 0, 0);
        epRuntime.sendEvent(tick4);

        EPOnDemandQueryResult result = epRuntime.executeQuery("select * from MarketDataWindow");

        EventBean[] entries = result.getArray();
        Assert.assertEquals(3, entries.length);
        EventBean entry1 = entries[0];
        Assert.assertEquals(1L, entry1.get("securityId"));
        Assert.assertEquals(FeedType.IB, entry1.get("feedType"));
        Assert.assertSame(tick3, entry1.get("marketDataEvent"));
        EventBean entry2 = entries[1];
        Assert.assertEquals(2L, entry2.get("securityId"));
        Assert.assertEquals(FeedType.IB, entry2.get("feedType"));
        Assert.assertSame(tick2, entry2.get("marketDataEvent"));
        EventBean entry3 = entries[2];
        Assert.assertEquals(2L, entry3.get("securityId"));
        Assert.assertEquals(FeedType.LMAX, entry3.get("feedType"));
        Assert.assertSame(tick4, entry3.get("marketDataEvent"));
    }

}
