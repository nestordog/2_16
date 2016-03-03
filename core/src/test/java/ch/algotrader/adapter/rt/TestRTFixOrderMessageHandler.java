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
package ch.algotrader.adapter.rt;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.adapter.fix.fix44.FixTestUtils;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockImpl;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.util.DateTimeLegacy;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.fix44.ExecutionReport;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestRTFixOrderMessageHandler {

    @Mock
    private OrderExecutionService orderExecutionService;

    private RTFixOrderMessageHandler impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new RTFixOrderMessageHandler(this.orderExecutionService);
    }

    @Test
    public void testExecutionReportOrderSubmitted() throws Exception {

        String s = "8=FIX.4.4|9=250|35=8|49=RTICKUAT|56=RINCARO|34=50|50=JPM-US-SOR|52=20140610-12:56:01|37=ee54e171-9-12lv|" +
                "11=14685d97784|17=ee54e171-39-07re-2|150=0|39=0|1=20580736-2|55=MSFT|54=1|38=100|40=2|44=35.00|59=0|32=0|" +
                "31=0.000000|151=100|14=0|6=0.000000|60=20140610-12:56:01|10=024|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, null, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setScale(2);

        Stock msft = new StockImpl();
        msft.setSymbol("MSFT");
        msft.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(msft);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("14685d97784")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertEquals("14685d97784", orderStatus1.getIntId());
        Assert.assertEquals("ee54e171-9-12lv", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-06-10 12:56:01.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportOrderExecuted() throws Exception {

        String s = "8=FIX.4.4|9=238|35=8|49=RTICKUAT|56=RINCARO|34=9|50=JPM-US-SOR|52=20140610-11:27:52|37=ee54e171-9-12lg" +
                "|11=1468588c149|17=ee54e171-39-07qy-3|150=F|39=2|1=20580736-2|55=MSFT|54=1|38=100|40=1|59=0|32=100" +
                "|31=3.1416|151=0|14=100|6=3.1416|60=20140610-11:27:52|10=049|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setScale(3);

        Stock msft = new StockImpl();
        msft.setSymbol("MSFT");
        msft.setSecurityFamily(family);

        Account account = new AccountImpl();
        account.setName("TEST");
        account.setBroker(Broker.IB.name());

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(msft);
        order.setAccount(account);
        order.setQuantity(100);
        order.setSide(Side.BUY);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("1468588c149")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertEquals("1468588c149", orderStatus1.getIntId());
        Assert.assertEquals("ee54e171-9-12lg", orderStatus1.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(100, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-06-10 11:27:52.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("3.142"), orderStatus1.getLastPrice());
        Assert.assertEquals(new BigDecimal("3.142"), orderStatus1.getAvgPrice());

        ArgumentCaptor<Fill> argumentCaptor2 = ArgumentCaptor.forClass(Fill.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleFill(argumentCaptor2.capture());

        Fill fill1 = argumentCaptor2.getValue();
        Assert.assertEquals("ee54e171-39-07qy-3", fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-06-10 11:27:52.000"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(100L, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("3.142"), fill1.getPrice());
    }

    @Test
    public void testExecutionReportOrderNotFound() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.NEW));
        executionReport.set(new ClOrdID("123"));

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId(Mockito.anyString())).thenReturn(null);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderExecutionService, Mockito.times(1)).getOpenOrderByIntId("123");
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testExecutionReportPendingNew() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.PENDING_NEW));
        executionReport.set(new ClOrdID("123"));

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).getOpenOrderByIntId("123");
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testExecutionReportPendingReplace() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.PENDING_REPLACE));
        executionReport.set(new ClOrdID("123"));

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).getOpenOrderByIntId("123");
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testExecutionReportPendingCancel() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.PENDING_CANCEL));
        executionReport.set(new ClOrdID("123"));

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).getOpenOrderByIntId("123");
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testExecutionReportRejected() throws Exception {

        String s = "8=FIX.4.4|9=295|35=8|49=RTICKUAT|56=RINCARO|34=11|50=JPM-US-SOR|52=20140610-11:29:37|37=ee54e171-9-12lh|" +
                "11=146858a5fa7|17=ee54e171-39-07qz-4|150=8|39=8|1=20580736-2|55=grass|54=1|38=100|40=1|59=0|32=0|31=0.000000|" +
                "151=0|14=0|6=0.0000|60=20140610-11:29:36|58=Route required data, RIC_CODE( 1299 ), not available.|10=173|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setScale(3);

        Stock msft = new StockImpl();
        msft.setSymbol("MSFT");
        msft.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(msft);
        order.setQuantity(100);
        order.setSide(Side.BUY);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("146858a5fa7")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertEquals("146858a5fa7", orderStatus1.getIntId());
        Assert.assertEquals(null, orderStatus1.getExtId());
        Assert.assertEquals(Status.REJECTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-06-10 11:29:36.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

}
