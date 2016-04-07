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
package ch.algotrader.adapter.tt;

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

import ch.algotrader.adapter.fix.fix42.FixTestUtils;
import ch.algotrader.esper.Engine;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.vo.marketData.AskVO;
import ch.algotrader.vo.marketData.BidVO;
import ch.algotrader.vo.marketData.TradeVO;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.fix42.MarketDataRequestReject;
import quickfix.fix42.MarketDataSnapshotFullRefresh;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestTTFixMarketDataMessageHandler {

    private final static DataDictionary DATA_DICTIONARY;

    static {
        try {
            DATA_DICTIONARY = new DataDictionary("tt/FIX42.xml");
            DATA_DICTIONARY.setCheckUnorderedGroupFields(false);
        } catch (ConfigError configError) {
            throw new Error(configError);
        }
    }

    @Mock
    private Engine engine;

    private TTFixMarketDataMessageHandler impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new TTFixMarketDataMessageHandler(engine);
    }

    @Test
    public void testMarketDataFullRefresh() throws Exception {

        String s = "8=FIX.4.2|9=00223|35=W|49=TTDEV14P|56=RATKODTS2|34=20|52=20160122-18:05:38.500|55=CL|48=00A0FQ00CLZ|10455=CLM6|167=FUT|" +
                "207=CME|15=USD|262=1|200=201606|387=753|268=3|269=0|290=1|270=3273|271=1|269=1|290=1|270=3307|271=94|269=2|270=3307|271=3|10=154|";
        MarketDataSnapshotFullRefresh fullRefresh = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, MarketDataSnapshotFullRefresh.class);
        Assert.assertNotNull(fullRefresh);

        this.impl.onMessage(fullRefresh, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<Object> argCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.atLeast(2)).sendEvent(argCaptor1.capture());

        List<Object> events = argCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(3, events.size());

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof BidVO);
        BidVO bid = (BidVO) event1;
        Assert.assertEquals("1", bid.getTickerId());
        Assert.assertEquals(new BigDecimal("3273.00000"), new BigDecimal(bid.getBid()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(1, bid.getVolBid());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2016-01-22 18:05:38.500"), bid.getDateTime());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof AskVO);

        AskVO ask = (AskVO) event2;
        Assert.assertEquals("1", ask.getTickerId());
        Assert.assertEquals(new BigDecimal("3307.00000"), new BigDecimal(ask.getAsk()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(94, ask.getVolAsk());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2016-01-22 18:05:38.500"), ask.getDateTime());

        Object event3 = events.get(2);
        Assert.assertTrue(event3 instanceof TradeVO);

        TradeVO trade = (TradeVO) event3;
        Assert.assertEquals("1", trade.getTickerId());
        Assert.assertEquals(new BigDecimal("3307.00000"), new BigDecimal(trade.getLast()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(3, trade.getVol());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2016-01-22 18:05:38.500"), trade.getLastDateTime());
    }

    @Test
    public void testMarketDataFullRefreshReject() throws Exception {

        String s = "8=FIX.4.2|9=00116|35=Y|49=TTDEV14P|56=RATKODTS2|34=2|52=20150930-12:31:47.465|262=stuff|" +
                "58=Unknown or missing security type: Entry #1|10=123|";
        MarketDataRequestReject reject = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, MarketDataRequestReject.class);
        Assert.assertNotNull(reject);

        this.impl.onMessage(reject, FixTestUtils.fakeFix42Session());
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

}
