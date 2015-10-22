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
package ch.algotrader.adapter.lmax;

import java.math.BigDecimal;
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
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.util.DateTimeLegacy;
import quickfix.DataDictionary;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.Reject;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestLMAXFixOrderMessageHandler {

    private static DataDictionary DATA_DICT;

    @Mock
    private OrderRegistry orderRegistry;
    @Mock
    private Engine engine;

    private LMAXFixOrderMessageHandler impl;

    @BeforeClass
    public static void setupClass() throws Exception {

        DATA_DICT = new DataDictionary("lmax/LMAX-FIX-Trading.xml");
    }

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new LMAXFixOrderMessageHandler(this.orderRegistry, this.engine);
    }

    @Test
    public void testExecutionReportOrderSubmitted() throws Exception {

        String s = "8=FIX.4.4|9=213|35=8|49=LMXBD|56=SMdemo|34=2|52=20140317-19:48:33.856|1=566809101|11=144d196a0cf" +
                "|48=4001|22=8|54=1|37=AAIm0gAAAAAVcFaM|59=3|40=1|60=20140317-19:48:33.854|6=0|17=IcjSDQAAAAJoCIaf|527=0|39=0|150=0" +
                "|14=0|151=10|38=10|10=037|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, DATA_DICT, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        Mockito.when(this.orderRegistry.getOpenOrderByIntId("144d196a0cf")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        Object event1 = argumentCaptor.getValue();
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("144d196a0cf", orderStatus1.getIntId());
        Assert.assertEquals("AAIm0gAAAAAVcFaM", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-17 19:48:33.854"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportOrderExecuted() throws Exception {

        String s = "8=FIX.4.4|9=249|35=8|49=LMXBD|56=SMdemo|34=3|52=20140317-19:48:33.856|1=566809101|11=144d196a0cf" +
                "|48=4001|22=8|54=1|37=AAIm0gAAAAAVcFaM|59=3|40=1|60=20140317-19:48:33.854|6=1.3925|17=IcjSDQAAAAJoCIag|527=QITNEAAAAAVCJRA5" +
                "|38=10|39=2|150=F|14=10|151=0|32=10|31=1.3925|10=099|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, DATA_DICT, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Account account = new AccountImpl();
        account.setName("TEST");
        account.setBroker(Broker.IB.name());

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setAccount(account);

        Mockito.when(this.orderRegistry.getOpenOrderByIntId("144d196a0cf")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(2)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(2, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("144d196a0cf", orderStatus1.getIntId());
        Assert.assertEquals("AAIm0gAAAAAVcFaM", orderStatus1.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(100000, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-17 19:48:33.854"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("1.393"), orderStatus1.getLastPrice());
        Assert.assertEquals(new BigDecimal("1.393"), orderStatus1.getAvgPrice());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof Fill);
        Fill fill1 = (Fill) event2;
        Assert.assertEquals("IcjSDQAAAAJoCIag", fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-17 19:48:33.854"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(100000L, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("1.393"), fill1.getPrice());
    }

    @Test
    public void testExecutionReportOrderNotFound() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.NEW));
        executionReport.set(new ClOrdID("123"));

        Mockito.when(this.orderRegistry.getOpenOrderByIntId(Mockito.anyString())).thenReturn(null);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderRegistry, Mockito.times(1)).getOpenOrderByIntId("123");
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testExecutionReportPendingNew() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.PENDING_NEW));
        executionReport.set(new ClOrdID("123"));

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderRegistry, Mockito.never()).getOpenOrderByIntId("123");
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testExecutionReportPendingReplace() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.PENDING_REPLACE));
        executionReport.set(new ClOrdID("123"));

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderRegistry, Mockito.never()).getOpenOrderByIntId("123");
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testExecutionReportPendingCancel() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.PENDING_CANCEL));
        executionReport.set(new ClOrdID("123"));

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderRegistry, Mockito.never()).getOpenOrderByIntId("123");
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testExecutionReportRejected() throws Exception {

        String s = "8=FIX.4.4|9=217|35=8|49=LMXBD|56=SMdemo|34=2|52=20140319-11:23:37.280|1=566809101|11=144da150f4b" +
                "|17=IcjSDQAAAAAAAAAA|527=0|48=99999|22=8|103=1|58=INSTRUMENT_DOES_NOT_EXIST|150=8|14=0|151=0|6=0|54=7" +
                "|60=20140319-11:23:37.280|39=8|37=0|10=247|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, DATA_DICT, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.INR);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("RUB.INR");
        forex.setBaseCurrency(Currency.RUB);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        Mockito.when(this.orderRegistry.getOpenOrderByIntId("144da150f4b")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        Object event1 = argumentCaptor.getValue();
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("144da150f4b", orderStatus1.getIntId());
        Assert.assertEquals(null, orderStatus1.getExtId());
        Assert.assertEquals(Status.REJECTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-19 11:23:37.280"), orderStatus1.getExtDateTime());
    }

    @Test
    public void testMessageRejected() throws Exception {

        String s = "8=FIX.4.4|9=121|35=3|49=LMXBD|56=SMdemo|34=2|52=20140319-11:22:26.896|45=2|371=48|372=D|373=6" +
                "|58=Required: Base10-encoded 64-bit integer|10=059|";
        Reject reject = FixTestUtils.parseFix44Message(s, DATA_DICT, Reject.class);
        Assert.assertNotNull(reject);

        this.impl.onMessage(reject, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testMessageRejectedNoRefs() throws Exception {

        String s = "8=FIX.4.4|9=121|35=3|49=LMXBD|56=SMdemo|34=2|52=20140319-11:22:26.896|45=2|371=48" +
                "|58=Required: Base10-encoded 64-bit integer|10=12|";
        Reject reject = FixTestUtils.parseFix44Message(s, DATA_DICT, Reject.class);
        Assert.assertNotNull(reject);

        this.impl.onMessage(reject, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

}
