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
package ch.algotrader.adapter.tt;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import ch.algotrader.adapter.fix.fix42.FixTestUtils;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.fix42.ExecutionReport;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
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
    private OrderRegistry orderRegistry;
    @Mock
    private Engine engine;

    private Future future;
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
        this.future.setSymbol("CL NOV/16");
        this.future.setTtid("00A0KP00CLZ");
        this.future.setSecurityFamily(futureFamily);
        this.future.setExpiration(DateTimeLegacy.toGMTDate(DateTimeUtil.parseLocalDate("2015-11-01")));

        this.account = Account.Factory.newInstance();
        this.account.setName("TT_TEST");
        this.account.setExtAccount("ratkodts2");
        this.account.setBroker(Broker.TT.name());

        this.impl = new TTFixOrderMessageHandler(this.orderRegistry, this.engine);
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
        Mockito.when(this.orderRegistry.getOpenOrderByIntId("1502848e0fc")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        Object event1 = argumentCaptor.getValue();
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
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
        Mockito.when(this.orderRegistry.getOpenOrderByIntId("1502848e0fc")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(2)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(2, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("1502848e0fc", orderStatus1.getIntId());
        Assert.assertEquals("0GR44P004", orderStatus1.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(1, orderStatus1.getFilledQuantity());
        Assert.assertEquals(1, orderStatus1.getLastQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-02 11:22:04.250"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("4828"), orderStatus1.getLastPrice());
        Assert.assertEquals(new BigDecimal("4828"), orderStatus1.getAvgPrice());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof Fill);
        Fill fill1 = (Fill) event2;
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

        Mockito.when(this.orderRegistry.getOpenOrderByIntId(Mockito.anyString())).thenReturn(null);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        Mockito.verify(this.orderRegistry, Mockito.times(1)).getOpenOrderByIntId("123");
        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
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
        Mockito.when(this.orderRegistry.getOpenOrderByIntId("15036f0c9cb")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        Object event1 = argumentCaptor.getValue();
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
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
        Mockito.when(this.orderRegistry.getOpenOrderByIntId("150370a3871")).thenReturn(order1);

        MarketOrder order2 = new MarketOrderImpl();
        order2.setIntId("150370a3913");
        order2.setSecurity(this.future);
        order2.setAccount(this.account);
        Mockito.when(this.orderRegistry.getOpenOrderByIntId("150370a3913")).thenReturn(order2);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix42Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(2)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.size());

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("150370a3871", orderStatus1.getIntId());
        Assert.assertEquals(null, orderStatus1.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus1.getStatus());
        Assert.assertSame(order1, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-05 08:07:58.761"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals("150370a3913", orderStatus2.getIntId());
        Assert.assertEquals("0GS0TX007", orderStatus2.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus2.getStatus());
        Assert.assertSame(order2, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2015-10-05 08:07:58.761"), orderStatus2.getExtDateTime());
        Assert.assertEquals(null, orderStatus2.getLastPrice());
        Assert.assertEquals(null, orderStatus2.getAvgPrice());
    }

}
