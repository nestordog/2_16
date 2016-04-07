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
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.adapter.fix.DropCopyAllocationVO;
import ch.algotrader.adapter.fix.DropCopyAllocator;
import ch.algotrader.adapter.fix.fix42.FixTestUtils;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.TransactionImpl;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.OrderID;
import quickfix.fix42.BusinessMessageReject;
import quickfix.fix42.ExecutionReport;
import quickfix.fix42.Message;
import quickfix.fix42.OrderCancelReject;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TestTTFixOrderMessageHandler {

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
    private OrderExecutionService orderExecutionService;
    @Mock
    private LookupService lookupService;
    @Mock
    private DropCopyAllocator dropCopyAllocator;

    private Future future;
    private Future future2;
    private Account account;

    private TTFixOrderMessageHandler impl;

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        Exchange exchange = Exchange.Factory.newInstance();
        exchange.setName("CME");
        exchange.setCode("CME");
        exchange.setTimeZone("US/Central");

        FutureFamily futureFamily = FutureFamily.Factory.newInstance();
        futureFamily.setSymbolRoot("CL");
        futureFamily.setExpirationType(ExpirationType.NEXT_3_RD_MONDAY_3_MONTHS);
        futureFamily.setCurrency(Currency.USD);
        futureFamily.setExchange(exchange);

        this.future = Future.Factory.newInstance();
        this.future.setId(1L);
        this.future.setSymbol("CL NOV/15");
        this.future.setTtid("00A0KP00CLZ");
        this.future.setSecurityFamily(futureFamily);
        this.future.setExpiration(DateTimeLegacy.toGMTDate(DateTimeUtil.parseLocalDate("2015-11-01")));
        this.future.setMonthYear("201511");

        this.future2 = Future.Factory.newInstance();
        this.future2.setId(2L);
        this.future2.setSymbol("CL JAN/16");
        this.future2.setTtid("00A0AQ00CLZ");
        this.future2.setSecurityFamily(futureFamily);
        this.future2.setExpiration(DateTimeLegacy.toGMTDate(DateTimeUtil.parseLocalDate("2016-01-01")));

        this.account = Account.Factory.newInstance();
        this.account.setName("TT_TEST");
        this.account.setExtAccount("ratkodts2");
        this.account.setBroker(Broker.TT.name());

        this.impl = new TTFixOrderMessageHandler(this.orderExecutionService, this.lookupService, this.dropCopyAllocator);
    }

    @Test
    public void testExecutionReportOrderSubmitted() throws Exception {

        String s = "8=FIX.4.2|9=00390|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=2|" +
                "52=20151002-11:22:04.198|55=CL|48=00A0KP00CLZ|10455=CLX5|167=FUT|207=CME|15=USD|1=ratkodts2|47=A|" +
                "204=0|10553=RATKODTS2|11=1502848e0fc|18203=CME|16142=US,IL|18216=A49004_-1|198=63C2D|37=0GR44P004|" +
                "17=0GR44P004:0|200=201511|151=1|14=0|54=1|40=2|77=O|59=0|11028=N|150=0|20=0|39=0|442=1|44=4878|38=1|" +
                "6=0|60=20151002-11:22:04.250|146=0|10=111|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.future);
        order.setAccount(this.account);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("1502848e0fc")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertEquals("1502848e0fc", orderStatus1.getIntId());
        Assert.assertEquals("0GR44P004", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-02 11:22:04.250"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportOrderExecuted() throws Exception {

        String s = "8=FIX.4.2|9=00509|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=3|52=20151002-11:22:04.400|" +
                "55=CL|48=00A0KP00CLZ|10455=CLX5|167=FUT|207=CME|15=USD|1=ratkodts2|47=A|204=0|10553=RATKODTS2|" +
                "11=1502848e0fc|375=CME000A|18203=CME|16142=US,IL|18216=A49004_-1|198=63C2D|37=0GR44P004|" +
                "17=o5b4513xd1k4|58=Fill|10527=80216:M:387426TN0003287|16018=nvk7o0|200=201511|32=1|151=0|14=1|" +
                "75=20151002|54=1|40=2|77=O|59=0|11028=N|150=2|20=0|39=2|442=1|44=4878|38=1|31=4828|6=4828|" +
                "60=20151002-11:22:04.250|6038=20151002-11:22:04.190|146=0|10=023|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.future);
        order.setAccount(this.account);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("1502848e0fc")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertEquals("1502848e0fc", orderStatus1.getIntId());
        Assert.assertEquals("0GR44P004", orderStatus1.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(1, orderStatus1.getFilledQuantity());
        Assert.assertEquals(1, orderStatus1.getLastQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-02 11:22:04.250"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("4828"), orderStatus1.getLastPrice());
        Assert.assertEquals(new BigDecimal("4828"), orderStatus1.getAvgPrice());

        ArgumentCaptor<Fill> argumentCaptor2 = ArgumentCaptor.forClass(Fill.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleFill(argumentCaptor2.capture());

        Fill fill1 = argumentCaptor2.getValue();
        Assert.assertEquals("o5b4513xd1k4", fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-02 11:22:04.250"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(1, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("4828"), fill1.getPrice());
    }

    @Test
    public void testExecutionReportOrderNotFound() throws Exception {

        ExecutionReport executionReport = new ExecutionReport();
        executionReport.set(new ExecType(ExecType.NEW));
        executionReport.set(new ClOrdID("123"));
        executionReport.set(new OrderID("xxx"));
        executionReport.set(new ExecID("123567"));
        executionReport.set(new CumQty(0.0d));

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId(Mockito.anyString())).thenReturn(null);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.orderExecutionService, Mockito.times(1)).getOpenOrderByIntId("123");
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testExecutionReportOrderCanceled() throws Exception {

        String s = "8=FIX.4.2|9=00405|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=3|52=20151005-07:40:11.687|" +
                "55=CL|48=00A0KP00CLZ|10455=CLX5|167=FUT|207=CME|15=USD|1=ratkodts2|47=A|204=0|10553=RATKODTS2|" +
                "11=15036f0ca9c|18203=CME|16142=US,IL|18216=A49004_-1|198=63IPI|41=15036f0c9cb|37=0GS0TX004|" +
                "17=0GS0TX004:1|200=201511|151=0|14=0|54=2|40=2|77=O|59=0|11028=N|150=4|20=0|39=4|442=1|44=4800|" +
                "38=1|6=0|60=20151005-07:40:12.050|146=0|10=172|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.future);
        order.setAccount(this.account);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("15036f0c9cb")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertEquals("15036f0ca9c", orderStatus1.getIntId());
        Assert.assertEquals("0GS0TX004", orderStatus1.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-05 07:40:12.050"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportOrderReplaced() throws Exception {

        String s = "8=FIX.4.2|9=00405|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=5|52=20151005-08:07:58.367|" +
                "55=CL|48=00A0KP00CLZ|10455=CLX5|167=FUT|207=CME|15=USD|1=ratkodts2|47=A|204=0|10553=RATKODTS2|" +
                "11=150370a3913|18203=CME|16142=US,IL|18216=A49004_-1|198=63IPP|41=150370a3871|37=0GS0TX007|" +
                "17=0GS0TX007:1|200=201511|151=1|14=0|54=2|40=2|77=O|59=0|11028=N|150=5|20=0|39=5|442=1|44=4805|38=1|" +
                "6=0|60=20151005-08:07:58.761|146=0|10=190|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        MarketOrder order1 = new MarketOrderImpl();
        order1.setIntId("150370a3871");
        order1.setSecurity(this.future);
        order1.setAccount(this.account);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("150370a3871")).thenReturn(order1);

        MarketOrder order2 = new MarketOrderImpl();
        order2.setIntId("150370a3913");
        order2.setSecurity(this.future);
        order2.setAccount(this.account);
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("150370a3913")).thenReturn(order2);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(2)).handleOrderStatus(argumentCaptor1.capture());

        List<OrderStatus> events = argumentCaptor1.getAllValues();

        OrderStatus orderStatus1 = events.get(0);
        Assert.assertEquals("150370a3871", orderStatus1.getIntId());
        Assert.assertEquals(null, orderStatus1.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus1.getStatus());
        Assert.assertSame(order1, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-05 08:07:58.761"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());

        OrderStatus orderStatus2 = events.get(1);
        Assert.assertEquals("150370a3913", orderStatus2.getIntId());
        Assert.assertEquals("0GS0TX007", orderStatus2.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus2.getStatus());
        Assert.assertSame(order2, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-05 08:07:58.761"), orderStatus2.getExtDateTime());
        Assert.assertEquals(null, orderStatus2.getLastPrice());
        Assert.assertEquals(null, orderStatus2.getAvgPrice());
    }

    @Test
    public void testExternalExecutionReportStatus() throws Exception {

        String s = "8=FIX.4.2|9=00400|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=4|52=20151007-14:01:07.145|" +
                "55=CL|48=00A0KP00CLZ|10455=CLX5|167=FUT|207=CME|15=USD|1=ratkodts2|47=A|204=0|10553=RATKODTS2|18203=CME|" +
                "16142=US,IL|18216=A49004_-1|198=63Q8Y|37=0GQEEH026|17=0GQEEH026:0|58=Created from existing|200=201511|" +
                "151=1|14=0|54=1|40=2|77=O|59=0|11028=Y|150=0|20=0|39=0|442=1|44=4694|38=1|6=0|60=20151007-14:01:07.442|" +
                "146=0|10=054|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
    }

    @Test
    public void testExternalExecutionReportFill() throws Exception {

        String s = "8=FIX.4.2|9=00495|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=5|52=20151007-14:01:07.146|" +
                "55=CL|48=00A0KP00CLZ|10455=CLX5|167=FUT|207=CME|15=USD|1=ratkodts2|47=A|204=0|10553=RATKODTS2|375=CME000A|" +
                "18203=CME|16142=US,IL|18216=A49004_-1|198=63Q8Y|37=0GQEEH026|17=1m0ubj0hjw4y4|58=Fill|" +
                "10527=80217:M:194448TN0003054|16018=nvth00|200=201511|32=1|151=0|14=1|75=20151007|54=1|40=2|77=O|59=0|" +
                "11028=Y|150=2|20=0|39=2|442=1|44=4694|38=1|31=4644|6=4644|60=20151007-14:01:07.442|" +
                "6038=20151007-21:01:31.179|146=0|10=255|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.future);
        order.setAccount(this.account);

        Strategy strategy = Strategy.Factory.newInstance();

        Mockito.when(this.dropCopyAllocator.allocate("00A0KP00CLZ", "ratkodts2")).thenReturn(
                new DropCopyAllocationVO(this.future, this.account, strategy));

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<ExternalFill> argumentCaptor2 = ArgumentCaptor.forClass(ExternalFill.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleFill(argumentCaptor2.capture());

        ExternalFill fill1 = argumentCaptor2.getValue();
        Assert.assertSame(this.future, fill1.getSecurity());
        Assert.assertSame(this.account, fill1.getAccount());
        Assert.assertSame(strategy, fill1.getStrategy());
        Assert.assertEquals("1m0ubj0hjw4y4", fill1.getExtId());
        Assert.assertEquals("0GQEEH026", fill1.getExtOrderId());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-07 14:01:07.442"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(1, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("4644"), fill1.getPrice());
    }

    @Test
    public void testBusinessReject() throws Exception {
        String s = "8=FIX.4.2|9=00129|35=j|49=TTDEV14O|56=RATKODTS2|50=NONE|57=NONE|34=4|52=20151110-13:21:20.125|" +
                "379=ttt5.0|372=D|58=Duplicate ClOrdID(11)|45=4|380=0|10=097|";

        BusinessMessageReject businessMessageReject = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, BusinessMessageReject.class);
        Assert.assertNotNull(businessMessageReject);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.future);
        order.setAccount(this.account);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("ttt5.0")).thenReturn(order);

        this.impl.onMessage(businessMessageReject, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertEquals("ttt5.0", orderStatus1.getIntId());
        Assert.assertEquals(null, orderStatus1.getExtId());
        Assert.assertEquals(Status.REJECTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-11-10 13:21:20.125"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testOrderCancelReject() throws Exception {
        String s = "8=FIX.4.2|9=00214|35=9|49=TTDEV14O|56=RATKODTS2|57=NONE|50=NONE|34=18|52=20151118-14:26:18.499|" +
                "37=020VGR013|41=ttt211.0|58=GIS orders are not supported by Exchange.|198=AIVT|10553=RATKODTS2|102=2|" +
                "434=2|39=0|60=20151118-14:26:18.499|10=021|";
        OrderCancelReject orderCancelReject = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, OrderCancelReject.class);
        Assert.assertNotNull(orderCancelReject);

        this.impl.onMessage(orderCancelReject, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testOrderCancelRejectDropCopy() throws Exception {
        String s = "8=FIX.4.2|9=267|35=9|34=832|49=TT_ORDER|50=NONE|52=20160107-13:08:00.588|56=FIX_ECAMOS|57=NONE|" +
                "37=02Y13V670|39=0|58=You have exceeded the order rate control (50 orders per second) configured by your risk administrator.|" +
                "60=20160107-13:08:00.588|102=2|198=FN0EZ|434=2|10553=E1RMUELLER|10=111|";
        OrderCancelReject orderCancelReject = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, OrderCancelReject.class);
        Assert.assertNotNull(orderCancelReject);

        this.impl.onMessage(orderCancelReject, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testExecutionReportCancelMissingOrigIntId() throws Exception {
        String s = "8=FIX.4.2|9=00387|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=3|52=20151109-15:53:01.991|" +
                "55=ES|48=00A0LP00ESZ|10455=ESZ5|167=FUT|207=CME|15=USD|1=RATKODTS2|47=A|204=0|10553=RATKODTS2|11=ttt14.0|" +
                "18203=CME|16142=US,IL|18216=A49004_-1|198=8K6Q|37=0G5EC7014|17=0G5EC7014:1|200=201512|151=0|14=0|54=1|" +
                "40=2|77=O|59=0|11028=Y|150=4|20=0|39=4|442=1|44=200000|38=1|6=0|60=20151109-15:53:02.439|146=0|10=039|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(this.future);
        order.setAccount(this.account);

        Mockito.when(this.orderExecutionService.lookupIntId("0G5EC7014")).thenReturn("ttt14.0");
        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("ttt14.0")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertEquals("ttt14.0", orderStatus1.getIntId());
        Assert.assertEquals("0G5EC7014", orderStatus1.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-11-09 15:53:02.439"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportRestated() throws Exception {
        String s = "8=FIX.4.2|9=00395|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=38|" +
                "52=20151117-16:07:22.339|55=ES|48=00A0CQ00ESZ|10455=ESH6|167=FUT|207=CME|15=USD|1=RATKODTS2|47=A|204=0|" +
                "10553=RATKODTS2|11=ttt207.0|18203=CME|16142=US,IL|18216=A49004_-1|198=B1OS|37=020AU7009|17=020AU7009:1|" +
                "200=201603|151=1|14=0|54=1|40=2|77=O|59=0|11028=Y|150=D|20=0|39=0|442=1|378=4|44=200925|38=1|6=0|" +
                "60=20151117-16:07:22.119|146=0|10=160|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(this.future);
        order.setAccount(this.account);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("ttt207.0")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<Order> argumentCaptor1 = ArgumentCaptor.forClass(Order.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleRestatedOrder(Mockito.same(order), argumentCaptor1.capture());
        ArgumentCaptor<OrderStatus> argumentCaptor2 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor2.capture());

        List<Order> orders = argumentCaptor1.getAllValues();
        Assert.assertNotNull(orders);
        Assert.assertEquals(1, orders.size());

        Order order2 = orders.get(0);
        Assert.assertTrue(order2 instanceof LimitOrder);
        LimitOrder reinstatedOrder = (LimitOrder) order2;
        Assert.assertNotSame(order, reinstatedOrder);
        Assert.assertEquals("020AU7009", reinstatedOrder.getExtId());
        Assert.assertEquals(new BigDecimal("200925"), reinstatedOrder.getLimit());
        Assert.assertEquals(1, reinstatedOrder.getQuantity());
        Assert.assertEquals(TIF.DAY, reinstatedOrder.getTif());

        OrderStatus orderStatus1 = argumentCaptor2.getValue();
        Assert.assertEquals("ttt207.0", orderStatus1.getIntId());
        Assert.assertEquals("020AU7009", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order2, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(1, orderStatus1.getRemainingQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-11-17 16:07:22.119"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportRestatedWithTimeInForce() throws Exception {
        String s = "8=FIX.4.2|9=00408|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=20|52=20151118-13:37:09.956|" +
                "55=ES|48=00A0CQ00ESZ|10455=ESH6|167=FUT|207=CME|15=USD|1=RATKODTS2|47=A|204=0|10553=RATKODTS2|11=ttt210.0|" +
                "18203=CME|16142=US,IL|18216=A49004_-1|198=AISL|37=020VGR012|17=020VGR012:1|200=201603|151=1|14=0|54=1|" +
                "40=2|77=O|59=6|11028=Y|150=D|20=0|39=0|442=1|378=4|44=203675|38=1|6=0|432=20151120|60=20151118-13:37:09.423|" +
                "146=0|10=085|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(this.future);
        order.setAccount(this.account);

        Mockito.when(this.orderExecutionService.getOpenOrderByIntId("ttt210.0")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<Order> argumentCaptor1 = ArgumentCaptor.forClass(Order.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleRestatedOrder(Mockito.same(order), argumentCaptor1.capture());
        ArgumentCaptor<OrderStatus> argumentCaptor2 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor2.capture());

        List<Order> orders = argumentCaptor1.getAllValues();
        Assert.assertNotNull(orders);
        Assert.assertEquals(1, orders.size());

        Order order2 = orders.get(0);
        Assert.assertTrue(order2 instanceof LimitOrder);
        LimitOrder reinstatedOrder = (LimitOrder) order2;
        Assert.assertNotSame(order, reinstatedOrder);
        Assert.assertEquals("020VGR012", reinstatedOrder.getExtId());
        Assert.assertEquals(new BigDecimal("203675"), reinstatedOrder.getLimit());
        Assert.assertEquals(1, reinstatedOrder.getQuantity());
        Assert.assertEquals(TIF.GTD, reinstatedOrder.getTif());

        Assert.assertEquals(
                new Date(LocalDate.of(2015, Month.NOVEMBER, 20).atStartOfDay(ZoneId.of("US/Central")).toInstant().toEpochMilli()),
                reinstatedOrder.getTifDateTime());

        OrderStatus orderStatus1 = argumentCaptor2.getValue();
        Assert.assertEquals("ttt210.0", orderStatus1.getIntId());
        Assert.assertEquals("020VGR012", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order2, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(1, orderStatus1.getRemainingQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-11-18 13:37:09.423"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportExternalWorkingOrder() throws Exception {
        String s = "8=FIX.4.2|9=00398|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=2|52=20151209-09:38:29.284|" +
                "55=CL|48=00A0AQ00CLZ|10455=CLF6|167=FUT|207=CME|15=USD|1=flowtdts2|47=A|204=0|10553=RATKODTS2|18203=CME|" +
                "16142=US,IL|18216=A49004_-1|198=2JIB0|37=061YRV036|17=061YRV036:1|58=Working Order|200=201601|151=2|14=0|" +
                "54=1|40=2|77=O|59=0|11028=Y|150=D|20=3|39=0|442=1|378=4|44=3745|38=2|6=0|60=20151209-09:38:29.284|146=0|10=099|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(this.future2);
        order.setAccount(this.account);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testExecutionReportOrderBookDownload() throws Exception {
        String s = "8=FIX.4.2|9=00412|35=8|49=TTDEV14O|56=RATKODTS2|50=TTORDDS202001|57=NONE|34=3|52=20151209-09:38:29.441|" +
                "55=CL|48=00A0AQ00CLZ|10455=CLF6|167=FUT|207=CME|15=USD|1=flowtdts2|47=A|204=0|10553=RATKODTS2|18203=CME|" +
                "16142=US,IL|18216=A49004_-1|198=2JIB0|37=061YRV036|17=061YRV036:2|58=Order Book Download|200=201601|" +
                "151=2|14=0|16728=1|54=1|40=2|77=O|59=0|11028=Y|150=D|20=3|39=0|442=1|378=4|44=3745|38=2|6=0|" +
                "60=20151209-09:38:29.441|146=0|10=199|";
        ExecutionReport executionReport = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(this.future2);
        order.setAccount(this.account);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.orderExecutionService, Mockito.never()).handleOrderStatus(Mockito.any());
        Mockito.verify(this.orderExecutionService, Mockito.never()).handleFill(Mockito.<Fill>any());
    }

    @Test
    public void testPositionUpdateUnknownExternalFill() throws Exception {
        String s = "8=FIX.4.2|9=00476|35=UAP|49=TTDEV14O|56=RATKODTS2|34=5|52=20151209-10:35:52.898|50=TTORDDS202001|" +
                "57=NONE|55=CL|48=00A0AQ00CLZ|10455=CLF6|167=FUT|207=CME|15=USD|1=ratkodts|47=A|204=0|10553=RATKODTS2|" +
                "375=CME000A|18203=CME|18216=A49004_-1|58=Fill|10527=80232:M:37926TN0000013|16018=nz27s0|200=201601|" +
                "32=3|75=20151209|54=1|40=2|59=0|44=3821|38=3|31=3821|60=20151209-10:35:40.469|146=0|16710=uan-at-1|" +
                "16721=uan-at-1e7y7edm87u5m|198=2JIBE|37=065ZT3026|17=e7y7edm87u5m|16727=1|442=1|16724=1|77=O|20=0|10=218|";
        Message message = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, Message.class);
        Assert.assertNotNull(message);

        Strategy strategy = Strategy.Factory.newInstance();

        Mockito.when(this.lookupService.getTransactionByExtId(Mockito.anyString())).thenReturn(null);
        Mockito.when(this.orderExecutionService.lookupIntId(Mockito.anyString())).thenReturn(null);
        Mockito.when(this.dropCopyAllocator.allocate("00A0AQ00CLZ", "ratkodts")).thenReturn(
                new DropCopyAllocationVO(this.future2, this.account, strategy));

        this.impl.onMessage(message, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.lookupService, Mockito.times(1)).getTransactionByExtId("e7y7edm87u5m");
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).lookupIntId("065ZT3026");

        ArgumentCaptor<ExternalFill> argumentCaptor1 = ArgumentCaptor.forClass(ExternalFill.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleFill(argumentCaptor1.capture());

        ExternalFill fill1 = argumentCaptor1.getValue();
        Assert.assertNotNull(fill1);

        Assert.assertSame(this.future2, fill1.getSecurity());
        Assert.assertSame(this.account, fill1.getAccount());
        Assert.assertSame(strategy, fill1.getStrategy());
        Assert.assertEquals("e7y7edm87u5m", fill1.getExtId());
        Assert.assertEquals("065ZT3026", fill1.getExtOrderId());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-12-09 10:35:40.469"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(3, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("3821"), fill1.getPrice());

        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleFill(fill1);
    }

    @Test
    public void testPositionUpdateUnknownInternalFill() throws Exception {
        String s = "8=FIX.4.2|9=00480|35=UAP|49=TTDEV14O|56=RATKODTS2|34=3|52=20151209-10:49:18.092|50=TTORDDS202001|" +
                "57=NONE|55=CL|48=00A0AQ00CLZ|10455=CLF6|167=FUT|207=CME|15=USD|1=ratkodts|47=A|204=0|10553=RATKODTS2|" +
                "375=CME000A|18203=CME|18216=A49004_-1|58=Fill|10527=80232:M:37942TN0000014|16018=nz27s0|200=201601|32=3|" +
                "75=20151209|54=1|40=2|59=0|44=3821|38=6|31=3821|60=20151209-10:37:34.998|146=0|16710=uan-at-1|" +
                "16721=uan-at-11r80gux1b4i2si|198=2JIBF|37=065ZT3025|17=1r80gux1b4i2si|16727=3|442=1|16724=1|77=O|20=0|10=006|";
        Message message = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, Message.class);
        Assert.assertNotNull(message);

        LimitOrder order = new LimitOrderImpl();
        order.setSecurity(this.future2);
        order.setAccount(this.account);
        order.setQuantity(6);
        OrderStatusVO executionStatus = new OrderStatusVO(0L, null, Status.SUBMITTED, 1L, 5L, 0L, "ttt3.0", 0L, 0L);

        Mockito.when(this.lookupService.getTransactionByExtId(Mockito.anyString())).thenReturn(null);
        Mockito.when(this.orderExecutionService.lookupIntId("065ZT3025")).thenReturn("ttt3.0");
        Mockito.when(this.orderExecutionService.getOpenOrderDetailsByIntId("ttt3.0")).thenReturn(new OrderDetailsVO(order, executionStatus));

        this.impl.onMessage(message, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.lookupService, Mockito.times(1)).getTransactionByExtId("1r80gux1b4i2si");
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).lookupIntId("065ZT3025");

        ArgumentCaptor<OrderStatus> argumentCaptor1 = ArgumentCaptor.forClass(OrderStatus.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(argumentCaptor1.capture());

        ArgumentCaptor<Fill> argumentCaptor2 = ArgumentCaptor.forClass(Fill.class);
        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleFill(argumentCaptor2.capture());

        OrderStatus orderStatus1 = argumentCaptor1.getValue();
        Assert.assertNotNull(orderStatus1);

        Assert.assertEquals("ttt3.0", orderStatus1.getIntId());
        Assert.assertEquals("065ZT3025", orderStatus1.getExtId());
        Assert.assertEquals(Status.PARTIALLY_EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(4, orderStatus1.getFilledQuantity());
        Assert.assertEquals(2, orderStatus1.getRemainingQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-12-09 10:37:34.998"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("3821"), orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());

        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleOrderStatus(orderStatus1);

        Fill fill1 = argumentCaptor2.getValue();
        Assert.assertNotNull(fill1);

        Assert.assertSame(order, fill1.getOrder());
        Assert.assertEquals("1r80gux1b4i2si", fill1.getExtId());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-12-09 10:37:34.998"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(3, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("3821"), fill1.getPrice());

        Mockito.verify(this.orderExecutionService, Mockito.times(1)).handleFill(fill1);

    }

    @Test
    public void testPositionUpdateKnownExternalFill() throws Exception {
        String s = "8=FIX.4.2|9=00476|35=UAP|49=TTDEV14O|56=RATKODTS2|34=5|52=20151209-10:35:52.898|50=TTORDDS202001|" +
                "57=NONE|55=CL|48=00A0AQ00CLZ|10455=CLF6|167=FUT|207=CME|15=USD|1=ratkodts|47=A|204=0|10553=RATKODTS2|" +
                "375=CME000A|18203=CME|18216=A49004_-1|58=Fill|10527=80232:M:37926TN0000013|16018=nz27s0|200=201601|" +
                "32=3|75=20151209|54=1|40=2|59=0|44=3821|38=3|31=3821|60=20151209-10:35:40.469|146=0|16710=uan-at-1|" +
                "16721=uan-at-1e7y7edm87u5m|198=2JIBE|37=065ZT3026|17=e7y7edm87u5m|16727=1|442=1|16724=1|77=O|20=0|10=218|";
        Message message = FixTestUtils.parseFix42Message(s, DATA_DICTIONARY, Message.class);
        Assert.assertNotNull(message);

        Mockito.when(this.lookupService.getTransactionByExtId(Mockito.anyString())).thenReturn(new TransactionImpl());

        this.impl.onMessage(message, FixTestUtils.fakeFix42Session());

        Mockito.verifyZeroInteractions(this.orderExecutionService);
    }

}
