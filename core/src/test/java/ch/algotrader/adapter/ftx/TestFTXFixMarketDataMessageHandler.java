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
package ch.algotrader.adapter.ftx;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.adapter.fix.fix44.FixTestUtils;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.esper.Engine;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import quickfix.fix44.Quote;
import quickfix.fix44.Reject;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestFTXFixMarketDataMessageHandler {

    @Mock
    private Engine engine;

    private FTXFixMarketDataMessageHandler impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new FTXFixMarketDataMessageHandler(engine);
    }

    @Test
    public void testMarketDataIncrementalRefresh1() throws Exception {

        String s = "8=FIX.4.4|9=146|35=S|49=AMIFXDEMO_QUOTE.FORTEX.NET|56=DAMCON_DM_FX|" +
                "34=6|52=20150610-08:03:36.656|131=0|55=EUR/USD|132=1.13653|134=1000000|" +
                "133=1.13677|135=1000000|10=230|";

        Quote quote = FixTestUtils.parseFix44Message(s, Quote.class);
        Assert.assertNotNull(quote);

        this.impl.onMessage(quote, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.atLeast(2)).sendEvent(argCaptor1.capture());

        List<Object> events = argCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof BidVO);
        BidVO bid = (BidVO) event1;
        Assert.assertEquals("EUR/USD", bid.getTickerId());
        Assert.assertEquals(new BigDecimal("1.13653"), new BigDecimal(bid.getBid()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(1000000, bid.getVolBid());
        Assert.assertEquals(FeedType.FTX.name(), bid.getFeedType());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-06-10 08:03:36.656"), bid.getDateTime());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof AskVO);

        AskVO ask = (AskVO) event2;
        Assert.assertEquals("EUR/USD", ask.getTickerId());
        Assert.assertEquals(new BigDecimal("1.13677"), new BigDecimal(ask.getAsk()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(1000000, ask.getVolAsk());
        Assert.assertEquals(FeedType.FTX.name(), ask.getFeedType());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-06-10 08:03:36.656"), ask.getDateTime());
    }

    @Test
    public void testMarketDataRequestRejected() throws Exception {

        String s = "8=FIX.4.4|9=104|35=3|49=AMIFXDEMO_QUOTE.FORTEX.NET|56=DAMCON_DM_FX|34=2|52=20150610-08:04:24.204|45=2|" +
                "58=55|372=R|373=1|10=099|";

        Reject reject = FixTestUtils.parseFix44Message(s, Reject.class);
        Assert.assertNotNull(reject);

        this.impl.onMessage(reject, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

}
