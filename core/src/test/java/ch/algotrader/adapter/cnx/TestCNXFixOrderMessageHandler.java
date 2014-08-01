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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.adapter.fix.fix44.FixTestUtils;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.LookupService;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.fix44.ExecutionReport;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestCNXFixOrderMessageHandler {

    @Mock
    private LookupService lookupService;
    @Mock
    private Engine engine;

    private CNXFixOrderMessageHandler impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);
        EngineLocator.instance().setEngine("BASE", engine);

        impl = new CNXFixOrderMessageHandler();
        impl.setLookupService(lookupService);
    }

    @Test
    public void testExecutionReportOrderNew() throws Exception {

        String s = "8=FIX.4.4|9=227|35=8|34=3|49=CNX|52=20140722-19:17:24.232|56=cmsg071414trd|" +
                "37=2537951907|11=1475f81bdee|41=1475f81bdee|17=603430_58974|150=0|39=0|55=EUR/USD|" +
                "460=4|54=1|38=2000|40=C|15=USD|32=0|151=2000|14=0|6=0.0000|60=20140722-19:17:24|" +
                "110=0|10=240|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        MarketOrder order = new MarketOrderImpl();
        Mockito.when(lookupService.getOpenOrderByRootIntId("1475f81bdee")).thenReturn(order);

        impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        Object event1 = argumentCaptor.getValue();
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("1475f81bdee", orderStatus1.getIntId());
        Assert.assertEquals("603430_58974", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        Assert.assertEquals("2537951907", order.getExtId());
    }

    @Test
    public void testExecutionReportOrderFilled() throws Exception {

        String s = "8=FIX.4.4|9=292|35=8|34=4|49=CNX|52=20140722-19:17:24.239|56=cmsg071414trd|" +
                "37=2537951907|11=1475f81bdee|41=1475f81bdee|453=1|448=CMSHub|452=1|" +
                "17=B2014203091ZN00|150=F|39=2|64=20140724|55=EUR/USD|460=4|54=1|38=2000|40=C|" +
                "15=USD|32=2000|31=1.34666|151=0|14=2000|6=1.34666|75=20140722|60=20140722-19:17:24|" +
                "110=0|10=005|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(5);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        Mockito.when(lookupService.getOpenOrderByRootIntId("1475f81bdee")).thenReturn(order);

        impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(engine, Mockito.times(2)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(2, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("1475f81bdee", orderStatus1.getIntId());
        Assert.assertEquals("B2014203091ZN00", orderStatus1.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(2000L, orderStatus1.getFilledQuantity());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof Fill);
        Fill fill1 = (Fill) event2;
        Assert.assertEquals("B2014203091ZN00", fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertEquals(FixTestUtils.parseDateTime("20140722-19:17:24.000"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(2000L, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("1.34666"), fill1.getPrice());
    }

    @Test
    public void testExecutionReportOrderNotFound() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new OrdStatus(OrdStatus.NEW));
        executionReport.set(new ExecType(ExecType.NEW));
        executionReport.set(new ClOrdID("123"));

        Mockito.when(lookupService.getOpenOrderByRootIntId(Mockito.anyString())).thenReturn(null);

        impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(lookupService, Mockito.times(1)).getOpenOrderByRootIntId("123");
        Mockito.verify(engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testExecutionReportRejected() throws Exception {

        String s = "8=FIX.4.4|9=222|35=8|34=3|49=CNX|52=20140723-15:42:06.267|56=cmsg071414trd|" +
                "37=UNKNOWN|11=14763e2fd3e|41=14763e2fd3e|17=UNKNOWN|150=8|39=8|103=1|55=INR/RUB|" +
                "460=4|54=1|32=0|151=0|14=0|6=0.0000|60=20140723-15:42:06.266|58=invalid instrument|" +
                "10=167|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(lookupService, Mockito.never()).getOpenOrderByRootIntId(Mockito.anyString());
        Mockito.verify(engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testExecutionReportOrderCancelled() throws Exception {

        String s = "8=FIX.4.4|9=254|35=8|34=4|49=CNX|52=20140723-16:21:33.148|56=cmsg071414trd|" +
                "37=2532835877|11=14764071b06|41=14764071493|17=603355_19486|150=4|39=4|55=EUR/USD|" +
                "460=4|54=1|38=100000|40=F|44=35.0000|15=USD|32=0|151=0|14=0|6=0.0000|" +
                "60=20140723-16:21:32|110=0|58=system cancel|10=217|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
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
        Mockito.when(lookupService.getOpenOrderByRootIntId("14764071b06")).thenReturn(order);

        impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(1, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("14764071b06", orderStatus1.getIntId());
        Assert.assertEquals("603355_19486", orderStatus1.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
    }

    @Test
    public void testExecutionReportOrderReplaced() throws Exception {

        String s = "8=FIX.4.4|9=225|35=8|34=4|49=CNX|52=20140723-18:45:46.335|56=cmsg071414trd|" +
                "37=2532836283|11=147648b2485|41=147648b2394|17=603355_19934|150=5|39=5|55=EUR/USD|" +
                "460=4|54=1|38=100000|44=30|40=F|151=100000|14=0|6=0|15=USD|60=20140723-18:45:46.334" +
                "|10=086|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setIntId("147648b2394");
        order.setSecurity(forex);
        Mockito.when(lookupService.getOpenOrderByRootIntId("147648b2485")).thenReturn(order);

        impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(1, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("147648b2485", orderStatus1.getIntId());
        Assert.assertEquals("603355_19934", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
    }

}
