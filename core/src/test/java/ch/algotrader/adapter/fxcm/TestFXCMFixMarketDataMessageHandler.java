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
package ch.algotrader.adapter.fxcm;

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
import ch.algotrader.vo.AskVO;
import ch.algotrader.vo.BidVO;
import quickfix.DataDictionary;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import quickfix.fix44.Reject;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestFXCMFixMarketDataMessageHandler {

    private static DataDictionary DATA_DICT;

    @Mock
    private Engine engine;

    private FXCMFixMarketDataMessageHandler impl;

    @BeforeClass
    public static void setupClass() throws Exception {

        DATA_DICT = new DataDictionary("fxcm/FIXFXCM10.xml");
    }

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        impl = new FXCMFixMarketDataMessageHandler(engine);
    }

    @Test
    public void testMarketDataFullRefresh() throws Exception {

        String s = "8=FIX.4.4|9=532|35=W|34=3|49=FXCM|50=U100D1|52=20140323-14:39:52.606|56=smoolman_client1|55=EUR/USD|228=1|231=1" +
                "|262=EUR/USD|460=4|9000=1|9001=5|9002=0.0001|9005=100|9011=0|9020=2|9080=1|9090=0|9091=0|9092=0|9093=0|9094=50000000" +
                "|9095=1|9096=O|268=4|269=0|270=1.37863|271=0|272=20140321|273=20:59:59|336=FXCM|625=U100D1|276=A|299=FXCM-EURUSD-7536569" +
                "|537=1|269=8|270=1.37863|272=20140321|273=20:59:59|269=7|270=1.38031|272=20140321|273=20:59:59|269=1|270=1.38031|271=0" +
                "|272=20140321|273=20:59:59|336=FXCM|625=U100D1|276=A|299=FXCM-EURUSD-7536569|537=1|10=033|";
        MarketDataSnapshotFullRefresh fullRefresh = FixTestUtils.parseFix44Message(s, DATA_DICT, MarketDataSnapshotFullRefresh.class);
        Assert.assertNotNull(fullRefresh);

        impl.onMessage(fullRefresh, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argCaptor1 = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(engine, Mockito.times(2)).sendEvent(argCaptor1.capture());

        List<Object> events = argCaptor1.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());

        DateFormat dateTimeParser = FixTestUtils.getSimpleDateTimeFormat();

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof BidVO);
        BidVO bid = (BidVO) event1;
        Assert.assertEquals("EUR/USD", bid.getTickerId());
        Assert.assertEquals(new BigDecimal("1.37863"), new BigDecimal(bid.getBid()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(0, bid.getVolBid());
        Assert.assertEquals(dateTimeParser.parse("20140321-20:59:59.000"), bid.getDateTime());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof AskVO);
        AskVO ask = (AskVO) event2;
        Assert.assertEquals("EUR/USD", ask.getTickerId());
        Assert.assertEquals(new BigDecimal("1.38031"), new BigDecimal(ask.getAsk()).setScale(5, RoundingMode.HALF_EVEN));
        Assert.assertEquals(0, ask.getVolAsk());
        Assert.assertEquals(dateTimeParser.parse("20140321-20:59:59.000"), ask.getDateTime());
    }

    @Test
    public void testBusinessMessageReject() throws Exception {

        String s = "8=FIX.4.4|9=193|35=j|34=2|49=FXCM|50=U100D1|52=20140323-10:06:41.085|56=smoolman_client1|58=Not Authorized" +
                "|336=FXCM|372=V|379=EUR/USD|380=6|625=U100D1|9025=1|9029=problem with request, session not authorized.|10=242|";
        BusinessMessageReject reject = FixTestUtils.parseFix44Message(s, DATA_DICT, BusinessMessageReject.class);
        Assert.assertNotNull(reject);

        impl.onMessage(reject, FixTestUtils.fakeFix44Session());
    }

    @Test
    public void testReject() throws Exception {

        String s = "8=FIX.4.4|9=122|35=3|34=3|49=FXCM|50=U100D1|52=20140326-10:03:27.152|56=smoolman_client1|45=3|58=Required tag missing" +
                "|371=146|372=V|373=1|10=138|";
        Reject rejects = FixTestUtils.parseFix44Message(s, DATA_DICT, Reject.class);
        Assert.assertNotNull(rejects);

        impl.onMessage(rejects, FixTestUtils.fakeFix44Session());

        Mockito.verify(engine, Mockito.never()).sendEvent(Mockito.any());
    }

}
