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
import ch.algotrader.service.OrderService;
import ch.algotrader.util.DateTimeLegacy;
import quickfix.DataDictionary;
import quickfix.SessionID;
import quickfix.field.ClOrdID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;
import quickfix.fix44.ExecutionReport;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TestFXCMFixOrderMessageHandler {

    private static DataDictionary DATA_DICT;

    private Account account;

    @Mock
 private OrderService orderService;
    @Mock
    private Engine engine;

    private FXCMFixOrderMessageHandler impl;

    @BeforeClass
    public static void setupClass() throws Exception {

        DATA_DICT = new DataDictionary("fxcm/FIXFXCM10.xml");
    }

    @Before
    public void setup() throws Exception {

        MockitoAnnotations.initMocks(this);

        this.impl = new FXCMFixOrderMessageHandler(this.orderService, this.engine);

        this.account = new AccountImpl();
        this.account.setBroker(Broker.IB);
        this.account.setExtAccount("test-account");
    }

    @Test
    public void testExecutionReportOrderNew() throws Exception {

        String s = "8=FIX.4.4|9=441|35=8|34=2|49=FXCM|50=U100D1|52=20140327-20:04:21.291|56=smoolman_client1|1=01727399|6=1.37432|11=1450524ad9d" +
                "|14=0|15=EUR|17=0|31=1.37432|32=0|37=174290346|38=1000|39=0|40=1|44=1.37432|54=1|55=EUR/USD|59=1|60=20140327-20:04:21|99=0|150=0" +
                "|151=1000|211=0|336=FXCM|625=U100D1|835=0|836=0|1094=0|9000=1|9041=62828936|9050=OM|9051=P|9061=0|453=1|448=FXCM ID|447=D|452=3" +
                "|802=4|523=32|803=26|523=smoolman|803=2|523=Moolman|803=22|523=1727399|803=10|10=131|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, DATA_DICT, ExecutionReport.class);
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
        order.setAccount(this.account);
        Mockito.when(this.orderService.getOpenOrderByRootIntId("1450524ad9d")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        Object event1 = argumentCaptor.getValue();
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("1450524ad9d", orderStatus1.getIntId());
        Assert.assertEquals("174290346", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-27 20:04:21.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("1.37432"), orderStatus1.getLastPrice());
        Assert.assertEquals(new BigDecimal("1.37432"), orderStatus1.getAvgPrice());
    }

    @Test
    public void testExecutionReportOrderStopped() throws Exception {

        String s = "8=FIX.4.4|9=444|35=8|34=3|49=FXCM|50=U100D1|52=20140327-20:04:21.292|56=smoolman_client1|1=01727399|6=1.37432|11=1450524ad9d|14=1000" +
                "|15=EUR|17=0|31=1.37432|32=1000|37=174290346|38=1000|39=7|40=1|44=1.37432|54=1|55=EUR/USD|59=1|60=20140327-20:04:21|99=0|150=F|151=0|211=0" +
                "|336=FXCM|625=U100D1|835=0|836=0|1094=0|9000=1|9041=62828936|9050=OM|9051=E|9061=0|453=1|448=FXCM ID|447=D|452=3|802=4|523=32|803=26" +
                "|523=smoolman|803=2|523=Moolman|803=22|523=1727399|803=10|10=043|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, DATA_DICT, ExecutionReport.class);
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
        order.setAccount(this.account);
        Mockito.when(this.orderService.getOpenOrderByRootIntId("1450524ad9d")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(2)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(2, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("1450524ad9d", orderStatus1.getIntId());
        Assert.assertEquals("174290346", orderStatus1.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(1000L, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-27 20:04:21.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("1.37432"), orderStatus1.getLastPrice());
        Assert.assertEquals(new BigDecimal("1.37432"), orderStatus1.getAvgPrice());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof Fill);
        Fill fill1 = (Fill) event2;
        Assert.assertEquals(null, fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-27 20:04:21.000"), fill1.getExtDateTime());
        Assert.assertEquals(Side.BUY, fill1.getSide());
        Assert.assertEquals(1000L, fill1.getQuantity());
        Assert.assertEquals(new BigDecimal("1.37432"), fill1.getPrice());
    }

    @Test
    public void testExecutionReportOrderFilled() throws Exception {

        String s = "8=FIX.4.4|9=464|35=8|34=4|49=FXCM|50=U100D1|52=20140327-20:04:21.426|56=smoolman_client1|1=01727399|6=1.37432|11=1450524ad9d|14=1000|15=EUR|" +
                "17=715531945|31=1.37432|32=1000|37=174290346|38=1000|39=2|40=1|44=1.37432|54=1|55=EUR/USD|58=Executed|59=1|60=20140327-20:04:21|99=0|150=F|151=0|" +
                "211=0|336=FXCM|625=U100D1|835=0|836=0|1094=0|9000=1|9041=62828936|9050=OM|9051=F|9061=0|453=1|448=FXCM ID|447=D|452=3|802=4|523=32|803=26|" +
                "523=smoolman|803=2|523=Moolman|803=22|523=1727399|803=10|10=179|";
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
        order.setAccount(this.account);
        Mockito.when(this.orderService.getOpenOrderByRootIntId("1450524ad9d")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(1, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("1450524ad9d", orderStatus1.getIntId());
        Assert.assertEquals("174290346", orderStatus1.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(1000L, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-27 20:04:21.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(new BigDecimal("1.374"), orderStatus1.getLastPrice());
        Assert.assertEquals(new BigDecimal("1.374"), orderStatus1.getAvgPrice());
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

        String s = "8=FIX.4.4|9=490|35=8|34=2|49=FXCM|50=U100D1|52=20140328-15:59:10.568|56=smoolman_client1|1=01727399|6=0|11=145096a919f|" +
                "14=0|17=0|31=0|32=0|37=NONE|38=0|39=8|40=1|44=0|54=1|55=RUB/UAH|58=Unsupported Order Type or Field Combination, " +
                "Invalid currency pair: RUB/UAH, Incorrect security: 0|59=1|60=20140328-15:59:10|99=0|150=8|151=0|211=0|336=FXCM|625=U100D1|" +
                "835=0|836=0|1094=0|9025=0|9029=Unsupported Order Type or Field Combination, Invalid currency pair: RUB/UAH, Incorrect " +
                "security: 0|9050=OM|9051=R|9061=0|10=080|";
        ExecutionReport executionReport = FixTestUtils.parseFix44Message(s, DATA_DICT, ExecutionReport.class);
        Assert.assertNotNull(executionReport);

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.AUD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("RUB.UAH");
        forex.setBaseCurrency(Currency.RUB);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        Mockito.when(this.orderService.getOpenOrderByRootIntId("145096a919f")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(1, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("145096a919f", orderStatus1.getIntId());
        Assert.assertEquals(null, orderStatus1.getExtId());
        Assert.assertEquals(Status.REJECTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0L, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-28 15:59:10.000"), orderStatus1.getExtDateTime());
        Assert.assertEquals(null, orderStatus1.getLastPrice());
        Assert.assertEquals(null, orderStatus1.getAvgPrice());
    }


    @Test
    public void testExecutionReportOrderCancelPending() throws Exception {

        String s = "8=FIX.4.4|9=446|35=8|34=3|49=FXCM|50=U100D1|52=20140328-16:24:07.165|56=smoolman_client1|1=01727399|6=2|11=14509816818|14=0|" +
                "15=EUR|17=715665074|31=2|32=0|37=174359806|38=1000|39=6|40=3|41=14509816672|44=2|54=1|55=EUR/USD|59=1|60=20140328-16:24:07|99=2|" +
                "150=6|151=1000|211=0|336=FXCM|625=U100D1|835=0|836=0|1094=0|9000=1|9041=62857932|9050=SE|9051=S|9061=0|453=1|448=FXCM ID|447=D|" +
                "452=3|802=4|523=32|803=26|523=smoolman|803=2|523=Moolman|803=22|523=1727399|803=10|10=046|";
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
        Mockito.when(this.orderService.getOpenOrderByRootIntId("14509816818")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        Mockito.verify(this.engine, Mockito.never()).sendEvent(Mockito.any());
    }

    @Test
    public void testExecutionReportOrderCancelled() throws Exception {

        String s = "8=FIX.4.4|9=456|35=8|34=4|49=FXCM|50=U100D1|52=20140328-16:37:34.099|56=smoolman_client1|1=01727399|6=2|11=145098db835|14=0|15=EUR|" +
                "17=715666512|31=2|32=0|37=174360633|38=1000|39=4|40=3|41=145098db61e|44=2|54=1|55=EUR/USD|58=Cancelled|59=1|60=20140328-16:37:34|99=2|" +
                "150=4|151=0|211=0|336=FXCM|625=U100D1|835=0|836=0|1094=0|9000=1|9041=62858268|9050=SE|9051=C|9061=0|453=1|448=FXCM ID|447=D|452=3|802=4|" +
                "523=32|803=26|523=smoolman|803=2|523=Moolman|803=22|523=1727399|803=10|10=160|";
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
        order.setAccount(this.account);
        Mockito.when(this.orderService.getOpenOrderByRootIntId("145098db835")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(1, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("145098db835", orderStatus1.getIntId());
        Assert.assertEquals("174360633", orderStatus1.getExtId());
        Assert.assertEquals(Status.CANCELED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-28 16:37:34.000"), orderStatus1.getExtDateTime());
    }

    @Test
    public void testExecutionReportOrderReplaced() throws Exception {

        String s = "8=FIX.4.4|9=454|35=8|34=3|49=FXCM|50=U100D1|52=20140328-17:17:56.520|56=smoolman_client1|1=01727399|6=1.9|11=14509b2ae84|14=0|15=EUR|" +
                "17=715671416|31=1.9|32=0|37=174363269|38=1000|39=5|40=3|41=14509b2ac86|44=1.9|54=1|55=EUR/USD|59=1|60=20140328-17:17:56|99=1.9|150=5|151=1000|" +
                "211=0|336=FXCM|625=U100D1|835=0|836=0|1094=0|9000=1|9041=62858822|9050=SE|9051=W|9061=0|453=1|448=FXCM ID|447=D|452=3|802=4|523=32|803=26|" +
                "523=smoolman|803=2|523=Moolman|803=22|523=1727399|803=10|10=216|";
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
        order.setAccount(this.account);
        Mockito.when(this.orderService.getOpenOrderByRootIntId("14509b2ae84")).thenReturn(order);

        this.impl.onMessage(executionReport, FixTestUtils.fakeFix44Session());

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(1)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(1, events.size());
        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("14509b2ae84", orderStatus1.getIntId());
        Assert.assertEquals("174363269", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(DateTimeLegacy.parseAsDateTimeMilliGMT("2014-03-28 17:17:56.000"), orderStatus1.getExtDateTime());
    }

    @Test
    public void testMarketOrderFullSession() throws Exception {

        SecurityFamily family = new SecurityFamilyImpl();
        family.setCurrency(Currency.USD);
        family.setScale(3);

        Forex forex = new ForexImpl();
        forex.setSymbol("EUR.USD");
        forex.setBaseCurrency(Currency.EUR);
        forex.setSecurityFamily(family);

        MarketOrder order = new MarketOrderImpl();
        order.setSecurity(forex);
        order.setAccount(this.account);
        order.setQuantity(20000);
        Mockito.when(this.orderService.getOpenOrderByRootIntId("fxcml2.0")).thenReturn(order);

        String s1 = "8=FIX.4.4|9=458|35=8|34=8|49=FXCM|50=U100R12|52=20140714-11:40:20.239|56=6444012301_client1|" +
                "1=6444012301|6=1.36248|11=fxcml2.0|14=0|15=EUR|17=82166892|31=1.36248|32=0|37=43179208|38=20000|" +
                "39=0|40=1|44=1.36248|54=1|55=EUR/USD|59=1|60=20140714-11:40:20|99=0|150=0|151=20000|211=0|336=FXCM|" +
                "625=100KREAL12|835=0|836=0|1094=0|9000=1|9041=18118820|9050=OM|9051=P|9061=0|453=1|448=FXCM ID|" +
                "447=D|452=3|802=4|523=32|803=26|523=6444012301|803=2|523=Moolman|803=22|523=44012301|803=10|10=245|";
        ExecutionReport executionReport1 = FixTestUtils.parseFix44Message(s1, DATA_DICT, ExecutionReport.class);
        Assert.assertNotNull(executionReport1);

        String s2 = "8=FIX.4.4|9=458|35=8|34=9|49=FXCM|50=U100R12|52=20140714-11:40:20.240|56=6444012301_client1|" +
                "1=6444012301|6=1.36248|11=fxcml2.0|14=0|15=EUR|17=82166893|31=1.36248|32=0|37=43179208|38=20000|" +
                "39=7|40=1|44=1.36248|54=1|55=EUR/USD|59=1|60=20140714-11:40:20|99=0|150=7|151=20000|211=0|336=FXCM|" +
                "625=100KREAL12|835=0|836=0|1094=0|9000=1|9041=18118820|9050=OM|9051=U|9061=0|453=1|448=FXCM ID|447=D|" +
                "452=3|802=4|523=32|803=26|523=6444012301|803=2|523=Moolman|803=22|523=44012301|803=10|10=002|";
        ExecutionReport executionReport2 = FixTestUtils.parseFix44Message(s2, DATA_DICT, ExecutionReport.class);
        Assert.assertNotNull(executionReport2);

        String s3 = "8=FIX.4.4|9=463|35=8|34=10|49=FXCM|50=U100R12|52=20140714-11:40:20.343|56=6444012301_client1|" +
                "1=6444012301|6=1.36248|11=fxcml2.0|14=20000|15=EUR|17=82166894|31=1.36248|32=20000|37=43179208|" +
                "38=20000|39=7|40=1|44=1.36248|54=1|55=EUR/USD|59=1|60=20140714-11:40:20|99=0|150=F|151=0|211=0|" +
                "336=FXCM|625=100KREAL12|835=0|836=0|1094=0|9000=1|9041=18118820|9050=OM|9051=E|9061=0|453=1|448=FXCM ID|" +
                "447=D|452=3|802=4|523=32|803=26|523=6444012301|803=2|523=Moolman|803=22|523=44012301|803=10|10=236|";
        ExecutionReport executionReport3 = FixTestUtils.parseFix44Message(s3, DATA_DICT, ExecutionReport.class);
        Assert.assertNotNull(executionReport3);

        String s4 = "8=FIX.4.4|9=475|35=8|34=11|49=FXCM|50=U100R12|52=20140714-11:40:20.344|56=6444012301_client1|" +
                "1=6444012301|6=1.36248|11=fxcml2.0|14=20000|15=EUR|17=82166895|31=1.36248|32=20000|37=43179208|" +
                "38=20000|39=2|40=1|44=1.36248|54=1|55=EUR/USD|58=Executed|59=1|60=20140714-11:40:20|99=0|150=F|" +
                "151=0|211=0|336=FXCM|625=100KREAL12|835=0|836=0|1094=0|9000=1|9041=18118820|9050=OM|9051=F|9061=0|" +
                "453=1|448=FXCM ID|447=D|452=3|802=4|523=32|803=26|523=6444012301|803=2|523=Moolman|803=22|523=44012301|" +
                "803=10|10=208|";
        ExecutionReport executionReport4 = FixTestUtils.parseFix44Message(s4, DATA_DICT, ExecutionReport.class);
        Assert.assertNotNull(executionReport4);

        SessionID sessionID = FixTestUtils.fakeFix44Session();
        this.impl.onMessage(executionReport1, sessionID);
        this.impl.onMessage(executionReport2, sessionID);
        this.impl.onMessage(executionReport3, sessionID);
        this.impl.onMessage(executionReport4, sessionID);

        ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
        Mockito.verify(this.engine, Mockito.times(5)).sendEvent(argumentCaptor.capture());

        List<Object> events = argumentCaptor.getAllValues();
        Assert.assertEquals(5, events.size());

        Object event1 = events.get(0);
        Assert.assertTrue(event1 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) event1;
        Assert.assertEquals("fxcml2.0", orderStatus1.getIntId());
        Assert.assertEquals("43179208", orderStatus1.getExtId());
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertSame(order, orderStatus1.getOrder());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());

        Object event2 = events.get(1);
        Assert.assertTrue(event2 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) event2;
        Assert.assertEquals("fxcml2.0", orderStatus2.getIntId());
        Assert.assertEquals("43179208", orderStatus2.getExtId());
        Assert.assertEquals(Status.PARTIALLY_EXECUTED, orderStatus2.getStatus());
        Assert.assertSame(order, orderStatus2.getOrder());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());

        Object event3 = events.get(2);
        Assert.assertTrue(event3 instanceof OrderStatus);
        OrderStatus orderStatus3 = (OrderStatus) event3;
        Assert.assertEquals("fxcml2.0", orderStatus3.getIntId());
        Assert.assertEquals("43179208", orderStatus3.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus3.getStatus());
        Assert.assertSame(order, orderStatus3.getOrder());
        Assert.assertEquals(20000, orderStatus3.getFilledQuantity());

        Object event4 = events.get(3);
        Assert.assertTrue(event4 instanceof Fill);
        Fill fill1 = (Fill) event4;
        Assert.assertEquals(null, fill1.getExtId());
        Assert.assertSame(order, fill1.getOrder());
        Assert.assertEquals(20000, fill1.getQuantity());

        Object event5 = events.get(4);
        Assert.assertTrue(event5 instanceof OrderStatus);
        OrderStatus orderStatus4 = (OrderStatus) event5;
        Assert.assertEquals("fxcml2.0", orderStatus4.getIntId());
        Assert.assertEquals("43179208", orderStatus4.getExtId());
        Assert.assertEquals(Status.EXECUTED, orderStatus4.getStatus());
        Assert.assertSame(order, orderStatus4.getOrder());
        Assert.assertEquals(20000, orderStatus4.getFilledQuantity());
    }

}
