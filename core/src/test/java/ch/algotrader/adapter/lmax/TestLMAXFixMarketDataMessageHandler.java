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
package ch.algotrader.adapter.lmax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.adapter.fix.fix44.FixTestUtils;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import quickfix.DataDictionary;
import quickfix.FieldNotFound;
import quickfix.fix44.MarketDataSnapshotFullRefresh;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestLMAXFixMarketDataMessageHandler {

    private static DataDictionary DATA_DICT;

    @Mock
    private Engine engine;

    private LMAXFixMarketDataMessageHandler impl;

    @BeforeClass
    public static void setupClass() throws Exception {

        DATA_DICT = new DataDictionary("lmax/LMAX-FIX-MarketData.xml");
    }

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);
        EngineLocator.instance().setEngine("SERVER", this.engine);

        this.impl = new LMAXFixMarketDataMessageHandler();
    }

    @Test
    public void testMarketDataFullRefresh() throws Exception {

        String s = "8=FIX.4.4|9=167|35=W|49=LMXBDM|56=SMdemo|34=8|52=20140313-16:59:27.747|262=EUR/USD|48=4001|22=8|" +
                "268=2|269=0|270=1.39043|271=45|272=20140313|273=16:59:27.683|269=1|270=1.39049|271=245|10=148|";
        MarketDataSnapshotFullRefresh fullRefresh = FixTestUtils.parseFix44Message(s, DATA_DICT, MarketDataSnapshotFullRefresh.class);
        Assert.assertNotNull(fullRefresh);

        this.impl.onMessage(fullRefresh, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.atLeast(2)).sendEvent(argCaptor1.capture());

        List<Object> events = argCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());

        DateFormat dateTimeParser = FixTestUtils.getSimpleDateTimeFormat();

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof BidVO);
        BidVO bid = (BidVO) event1;
        Assert.assertEquals("4001", bid.getTickerId());
        Assert.assertEquals(new BigDecimal("1.39043"), new BigDecimal(bid.getBid()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(450000, bid.getVolBid());
        Assert.assertEquals(dateTimeParser.parse("20140313-16:59:27.683"), bid.getDateTime());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof AskVO);

        AskVO ask = (AskVO) event2;
        Assert.assertEquals("4001", ask.getTickerId());
        Assert.assertEquals(new BigDecimal("1.39049"), new BigDecimal(ask.getAsk()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(2450000, ask.getVolAsk());
        Assert.assertEquals(dateTimeParser.parse("20140313-16:59:27.683"), ask.getDateTime());
    }

    @Test
    public void testMarketDataFullRefreshDateTimeInAsk() throws Exception {

        String s = "8=FIX.4.4|9=167|35=W|49=LMXBDM|56=SMdemo|34=8|52=20140313-16:59:27.747|262=EUR/USD|48=4001|22=8|" +
                "268=2|269=0|270=1.39043|271=45|269=1|270=1.39049|271=245|272=20140313|273=16:59:27.683|10=148|";
        MarketDataSnapshotFullRefresh fullRefresh = FixTestUtils.parseFix44Message(s, DATA_DICT, MarketDataSnapshotFullRefresh.class);
        Assert.assertNotNull(fullRefresh);

        this.impl.onMessage(fullRefresh, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.atLeast(2)).sendEvent(argCaptor1.capture());

        List<Object> events = argCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());

        DateFormat dateTimeParser = FixTestUtils.getSimpleDateTimeFormat();

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof BidVO);
        BidVO bid = (BidVO) event1;
        Assert.assertEquals(null, bid.getDateTime());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof AskVO);

        AskVO ask = (AskVO) event2;
        Assert.assertEquals(dateTimeParser.parse("20140313-16:59:27.683"), ask.getDateTime());
    }

    @Test(expected = FieldNotFound.class)
    public void testMarketDataFullRefreshMissingSecurityCode() throws Exception {

        String s = "8=FIX.4.4|9=167|35=W|49=LMXBDM|56=SMdemo|34=8|52=20140313-16:59:27.747|262=EUR/USD|22=8|" +
                "268=2|269=0|270=1.39043|271=45|269=1|270=1.39049|271=245|272=20140313|273=16:59:27.683|10=37|";
        MarketDataSnapshotFullRefresh fullRefresh = FixTestUtils.parseFix44Message(s, DATA_DICT, MarketDataSnapshotFullRefresh.class);
        Assert.assertNotNull(fullRefresh);

        this.impl.onMessage(fullRefresh, FixTestUtils.fakeFix44Session());
    }

}
