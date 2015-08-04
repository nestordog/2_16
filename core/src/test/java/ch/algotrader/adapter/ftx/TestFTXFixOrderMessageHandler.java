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
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.Reject;
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
import ch.algotrader.service.OrderService;
import ch.algotrader.util.DateTimeLegacy;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestFTXFixOrderMessageHandler {

    @Mock
 private OrderService orderService;
    @Mock
    private Engine engine;

    private FTXFixOrderMessageHandler impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new FTXFixOrderMessageHandler(this.orderService, this.engine);
    }

    @Test
    public void testExecutionReportOrderNew() throws Exception {

        String s = "8=FIX.4.4|9=292|35=8|49=AMIFXDEMO.FORTEX.NET|56=DAMCON_DM_FX|34=00000066|" +
                "52=20150610-12:53:29.760|1=DAMCON_DM_FX|6=0|11=14ddd87a9d2|14=0|17=1433940809|" +
                "18=u|37=14ddd87a9d2|39=0|40=1|44=0|54=1|55=EUR/USD|59=1|60=20150610-08:53:29|" +
                "76=INTX|100=INTX|150=0|151=2000|167=FOR|640=0|9164=FIX|9166=DAMCON_DM_FX|9201=1|10=253|";
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
        order.setQuantity(2000);

        Mockito.when(this.orderService.getOpenOrderByRootIntId("14ddd87a9d2")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        Object event1 = argumentCaptor.getValue();
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("14ddd87a9d2", orderStatus1.getIntId());
        Assert.assertEquals("14ddd87a9d2", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-06-10 08:53:29.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportOrderFilled() throws Exception {

        String s = "8=FIX.4.4|9=366|35=8|49=AMIFXDEMO.FORTEX.NET|56=DAMCON_DM_FX|34=00000067|" +
                "52=20150610-12:53:29.760|1=DAMCON_DM_FX|6=1.13067|11=14ddd87a9d2|12=0|14=2000|" +
                "17=DAMCON_DM_FX:DAMCON_DM_FX:14ddd87a9d2_14339408091|31=1.13067|32=2000|37=14ddd87a9d2|" +
                "39=2|54=1|55=EUR/USD|60=20150610-08:53:29|64=20150612|75=20150610|76=INTX|100=INTX|" +
                "150=F|151=0|167=FOR|9164=FIX|9166=DAMCON_DM_FX|9173=1|9329=0|10=007|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(5);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Account account = new AccountImpl();
        account.setBroker(Broker.FTX);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setQuantity(2000);
        order.setAccount(account);
        Mockito.when(this.orderService.getOpenOrderByRootIntId("14ddd87a9d2")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(2)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(2, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("14ddd87a9d2", orderStatus1.getIntId());
        Assert.assertEquals("14ddd87a9d2", orderStatus1.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(2000L, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-06-10 08:53:29.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("1.13067"), orderStatus1.getLastPrice());
        Assert.assertEquals(new BigDecimal("1.13067"), orderStatus1.getAvgPrice());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof Fill);
        Fill fill1 = (Fill) event2;
        Assert.assertEquals("DAMCON_DM_FX:DAMCON_DM_FX:14ddd87a9d2_14339408091", fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-06-10 08:53:29.000"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(2000L, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("1.13067"), fill1.getPrice());
    }

    @Test
    public void testExecutionReportOrderNotFound() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new OrdStatus(OrdStatus.NEW));
        executionReport.set(new ExecType(ExecType.NEW));
        executionReport.set(new ClOrdID("123"));

        Mockito.when(this.orderService.getOpenOrderByRootIntId(Mockito.anyString())).thenReturn(null);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderService, Mockito.times(1)).getOpenOrderByRootIntId("123");
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testExecutionReportRejected() throws Exception {

        String s = "8=FIX.4.4|9=288|35=8|49=AMIFXDEMO.FORTEX.NET|56=DAMCON_DM_FX|34=00000061|52=20150610-12:50:34.070|" +
                "1=DAMCON_DM_FX|6=0|11=14ddd84fb8e|14=0|17=1433940634|37=14ddd84fb8e|39=8|40=1|44=0|54=1|55=EUR/USD|" +
                "58=97|60=20150610-08:50:34|76=INTX|100=INTX|150=8|151=2000|167=FOR|640=0|9164=FIX|9166=DAMCON_DM_FX|9201=1|10=123|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(5);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        Account account = new AccountImpl();
        account.setBroker(Broker.FTX);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setQuantity(2000);
        order.setAccount(account);

        Mockito.when(this.orderService.getOpenOrderByRootIntId("14ddd84fb8e")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        Object event1 = argumentCaptor.getValue();
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("14ddd84fb8e", orderStatus1.getIntId());
        Assert.assertEquals(null, orderStatus1.getExtId());
        Assert.assertEquals(Status.REJECTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(null, order.getExtId());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-06-10 08:50:34.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testReject() throws Exception {

        String s = "8=FIX.4.4|9=155|35=3|49=AMIFXDEMO.FORTEX.NET|56=DAMCON_DM_FX|34=52|52=20150616-07:57:59.302|" +
                "45=15|58=message sequence number is too small,it should be 24(15)|372=A|373=99|10=073|";
        Reject reject = FixTestUtils.parseFix44Message(s, Reject.class);
        Assert.assertNotNull(reject);

        this.impl.onMessage(reject, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

}
