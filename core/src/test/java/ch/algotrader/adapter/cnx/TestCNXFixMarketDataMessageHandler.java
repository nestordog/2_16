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
package ch.algotrader.adapter.cnx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.adapter.fix.fix44.FixTestUtils;
import ch.algotrader.esper.Engine;
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import quickfix.fix44.MarketDataIncrementalRefresh;
import quickfix.fix44.MarketDataRequestReject;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestCNXFixMarketDataMessageHandler {

    @Mock
    private Engine engine;

    private CNXFixMarketDataMessageHandler impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new CNXFixMarketDataMessageHandler(this.engine);
    }

    @Test
    public void testMarketDataIncrementalRefresh1() throws Exception {

        String s = "8=FIX.4.4|9=195|35=X|34=3|49=CNX|52=20140721-08:22:11.550|56=cmsg071414str|" +
                "262=EUR/USD|268=2|279=0|269=0|278=1|55=EUR/USD|270=1.35259|271=500000|346=1|279=0|" +
                "269=1|278=2|55=EUR/USD|270=1.35271|271=15600000|346=1|10=231|";

        MarketDataIncrementalRefresh incrementalRefresh = FixTestUtils.parseFix44Message(s, MarketDataIncrementalRefresh.class);
        Assert.assertNotNull(incrementalRefresh);

        this.impl.onMessage(incrementalRefresh, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.atLeast(2)).sendEvent(argCaptor1.capture());

        List<Object> events = argCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());

        DateFormat dateTimeParser = FixTestUtils.getSimpleDateTimeFormat();

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof BidVO);
        BidVO bid = (BidVO) event1;
        Assert.assertEquals("EUR/USD", bid.getTickerId());
        Assert.assertEquals(new BigDecimal("1.35259"), new BigDecimal(bid.getBid()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(500000, bid.getVolBid());
        Assert.assertEquals(dateTimeParser.parse("20140721-08:22:11.550"), bid.getDateTime());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof AskVO);

        AskVO ask = (AskVO) event2;
        Assert.assertEquals("EUR/USD", ask.getTickerId());
        Assert.assertEquals(new BigDecimal("1.35271"), new BigDecimal(ask.getAsk()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(15600000, ask.getVolAsk());
        Assert.assertEquals(dateTimeParser.parse("20140721-08:22:11.550"), ask.getDateTime());
    }

    @Test
    public void testMarketDataIncrementalRefresh2() throws Exception {

        String s = "8=FIX.4.4|9=135|35=X|34=4|49=CNX|52=20140721-08:22:11.612|56=cmsg071414str|" +
                "262=EUR/USD|268=1|279=0|269=1|278=2|55=EUR/USD|270=1.3527|271=2100000|346=1|10=081|";

        MarketDataIncrementalRefresh incrementalRefresh = FixTestUtils.parseFix44Message(s, MarketDataIncrementalRefresh.class);
        Assert.assertNotNull(incrementalRefresh);

        this.impl.onMessage(incrementalRefresh, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argCaptor1.capture());

        List<Object> events = argCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(1, events.size());

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof AskVO);

        DateFormat dateTimeParser = FixTestUtils.getSimpleDateTimeFormat();

        AskVO ask = (AskVO) event1;
        Assert.assertEquals("EUR/USD", ask.getTickerId());
        Assert.assertEquals(new BigDecimal("1.35270"), new BigDecimal(ask.getAsk()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(2100000, ask.getVolAsk());
        Assert.assertEquals(dateTimeParser.parse("20140721-08:22:11.612"), ask.getDateTime());
    }

    @Test
    public void testMarketDataRequestRejected() throws Exception {

        String s = "8=FIX.4.4|9=97|35=Y|34=3|49=CNX|52=20140719-13:40:39.526|56=cmsg071414str|" +
                "262=stuff|281=2|58=Invalid Instrument|10=003|";

        MarketDataRequestReject reject = FixTestUtils.parseFix44Message(s, MarketDataRequestReject.class);
        Assert.assertNotNull(reject);

        this.impl.onMessage(reject, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testMarketDataIncrementalRefreshActionDelete() throws Exception {

        String s = "8=FIX.4.4|9=136|35=X|34=84|49=CNX|52=20141120-12:15:29.980|56=cmsg071414str|262=USD/CHF|" +
                "268=2|279=2|269=0|278=5|55=USD/CHF|279=2|269=1|278=6|55=USD/CHF|10=238|";

        MarketDataIncrementalRefresh incrementalRefresh = FixTestUtils.parseFix44Message(s, MarketDataIncrementalRefresh.class);
        Assert.assertNotNull(incrementalRefresh);

        this.impl.onMessage(incrementalRefresh, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }


}
