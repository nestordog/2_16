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
package ch.algotrader.service.algo;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.OrderStatusVOBuilder;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.algo.VWAPOrder;
import ch.algotrader.entity.trade.algo.VWAPOrderStateVO;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.MarketDataEventType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TimePeriod;
import ch.algotrader.service.CalendarService;
import ch.algotrader.service.HistoricalDataService;
import ch.algotrader.service.OrderExecutionService;
import ch.algotrader.service.SimpleOrderService;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.util.DateUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class VWAPOrderServiceTest {

    @Mock
    private OrderExecutionService orderExecutionService;

    @Mock
    private SimpleOrderService simpleOrderService;

    @Mock
    private HistoricalDataService historicalDataService;

    @Mock
    private CalendarService calendarService;

    private VWAPOrderService vwapOrderService;

    private Security security;
    private Strategy strategy;
    private Exchange exchange;
    private String intId = "XYZ";
    private List<Bar> historicalBars;
    private Duration bucketSize = Duration.MIN_15;
    private int lookBackDays = 1;

    @Before
    public void setup() {
        
        this.vwapOrderService = new VWAPOrderService(this.orderExecutionService, this.historicalDataService, this.calendarService, this.simpleOrderService);

        this.exchange = Exchange.Factory.newInstance("TEST_EXCHANGE", TimeZone.getDefault().toString());
        this.exchange.setId(1);

        SecurityFamily family = SecurityFamily.Factory.newInstance("", Currency.USD, 1, 2, "0<0.01", true, false);
        family.setExchange(this.exchange);

        this.security = Stock.Factory.newInstance(family);
        this.security.setSymbol("TEST_SECURITY");
        this.security.setId(1);

        this.strategy = Strategy.Factory.newInstance("TEST_STRATEGY", false);


        this.historicalBars = new ArrayList<Bar>();
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 9, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 9123));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 9, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7061));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 9, 30, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7635));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 9, 45, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6984));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 10, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 8671));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 10, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7703));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 10, 30, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6531));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 10, 45, 0), null, this.security, Duration.MIN_15, null, null, null, null, 8234));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 5930));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 8151));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 30, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6053));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 45, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7192));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 12, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6314));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 12, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6391));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 12, 30, 0), null, this.security, Duration.MIN_15, null, null, null, null, 5838));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 12, 45, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6128));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 13, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6249));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 13, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7025));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 13, 30, 0), null, this.security, Duration.MIN_15, null, null, null, null, 5997));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 13, 45, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6658));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 14, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6946));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 14, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 4920));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 14, 30, 0), null, this.security, Duration.MIN_15, null, null, null, null, 5094));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 14, 45, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7601));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 15, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7192));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 15, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7482));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 15, 30, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7393));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 15, 45, 0), null, this.security, Duration.MIN_15, null, null, null, null, 6295));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 16, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 5955));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 16, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 8821));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 16, 30, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7751));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 16, 45, 0), null, this.security, Duration.MIN_15, null, null, null, null, 7976));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 17, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 9067));
        this.historicalBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 17, 15, 0), null, this.security, Duration.MIN_15, null, null, null, null, 10569));
        
        Mockito.when(this.historicalDataService.getHistoricalBars(
                this.security.getId(), //
                DateUtils.truncate(new Date(), Calendar.DATE), //
                this.lookBackDays, //
                TimePeriod.DAY, //
                this.bucketSize, //
                MarketDataEventType.TRADES, //
                Collections.emptyMap())).thenReturn(this.historicalBars);


        Mockito.when(this.calendarService.isOpen(this.exchange.getId())).thenReturn(false);

        Mockito.when(this.calendarService.getNextOpenTime(this.exchange.getId())).thenReturn(DateUtil.dateForYMDHMS(2016, 1, 1, 9, 00, 0));

        Mockito.when(this.calendarService.getNextCloseTime(this.exchange.getId())).thenReturn(DateUtil.dateForYMDHMS(2016, 1, 1, 17, 30, 0));
    }

    @Test
    public void testPopulate() throws IllegalAccessException, InvocationTargetException, OrderValidationException {

        VWAPOrder order = new VWAPOrder();

        String nameValues = "side=BUY,quantity=2,bucketSize=MIN_15,lookbackDays=20,minInterval=60,maxInterval=129";

        Map<String, String> properties = new HashMap<>();
        for (String nameValue : nameValues.split(",")) {
            properties.put(nameValue.split("=")[0], nameValue.split("=")[1]);
        }

        BeanUtil.populate(order, properties);
    }

    @Test(expected = OrderValidationException.class)
    public void testValidateFail() throws OrderValidationException {

        VWAPOrder order = new VWAPOrder();

        order.validate();
    }

    @Test()
    public void testValidate() throws OrderValidationException {

        VWAPOrder order = createOrder();
        order.setQuantity(50000);
        order.setBucketSize(Duration.MIN_15);
        order.setLookbackPeriod(1);
        order.setMinInterval(60);
        order.setMaxInterval(60);

        order.validate();
    }

    @Test
    public void testCreateOrderState() {

        VWAPOrder vwapOrder = createOrder();

        vwapOrder.setQuantity(50000);
        vwapOrder.setBucketSize(this.bucketSize);
        vwapOrder.setLookbackPeriod(this.lookBackDays);
        vwapOrder.setMinInterval(60);
        vwapOrder.setMaxInterval(60);
        vwapOrder.setStartTime(LocalTime.of(11, 0));
        vwapOrder.setEndTime(LocalTime.of(15, 0));

        VWAPOrderStateVO orderState = this.vwapOrderService.createAlgoOrderState(vwapOrder);

        Assert.assertEquals(0.4878, orderState.getParticipation(), 0.001);
    }

    @Test
    public void testSendOrderNoStartEndTime() {

        VWAPOrder vwapOrder = createOrder();

        vwapOrder.setQuantity(50000);
        vwapOrder.setBucketSize(this.bucketSize);
        vwapOrder.setLookbackPeriod(this.lookBackDays);
        vwapOrder.setMinInterval(60);
        vwapOrder.setMaxInterval(60);

        VWAPOrderStateVO orderState = this.vwapOrderService.createAlgoOrderState(vwapOrder);

        Assert.assertEquals(510, vwapOrder.getDuration());

        Assert.assertEquals(0.2058, orderState.getParticipation(), 0.001);
    }

    @Test
    public void testSendOrderOddStartAndEnd() {

        VWAPOrder vwapOrder = createOrder();

        vwapOrder.setQuantity(50000);
        vwapOrder.setBucketSize(this.bucketSize);
        vwapOrder.setLookbackPeriod(this.lookBackDays);
        vwapOrder.setMinInterval(60);
        vwapOrder.setMaxInterval(60);
        vwapOrder.setStartTime(LocalTime.of(11, 5));
        vwapOrder.setEndTime(LocalTime.of(14, 52, 30));

        VWAPOrderStateVO orderState = this.vwapOrderService.createAlgoOrderState(vwapOrder);

        Assert.assertEquals(0.5170, orderState.getParticipation(), 0.001);
    }

    @Test
    public void testNextOrder() throws Exception {

        VWAPOrder vwapOrder = createOrder();

        vwapOrder.setQuantity(50000);
        vwapOrder.setBucketSize(this.bucketSize);
        vwapOrder.setLookbackPeriod(this.lookBackDays);
        vwapOrder.setMinInterval(60);
        vwapOrder.setMaxInterval(60);

        OrderStatusVO orderStatus = OrderStatusVOBuilder.create().setRemainingQuantity(1000).build();
        Mockito.when(this.orderExecutionService.getStatusByIntId(vwapOrder.getIntId())).thenReturn(orderStatus);

        this.vwapOrderService.validateOrder(vwapOrder);
        this.vwapOrderService.sendOrder(vwapOrder);

        this.vwapOrderService.sendNextOrder(vwapOrder, DateUtil.dateForYMDHMS(2016, 1, 1, 11, 30, 0));

        ArgumentCaptor<SimpleOrder> argument = ArgumentCaptor.forClass(SimpleOrder.class);
        Mockito.verify(this.simpleOrderService, Mockito.times(1)).sendOrder(argument.capture());
        SimpleOrder childOrder = argument.getValue();

        Assert.assertEquals(83, childOrder.getQuantity());
    }

    @Test
    public void testReporting() {

        VWAPOrder vwapOrder = createOrder();

        vwapOrder.setQuantity(50000);
        vwapOrder.setBucketSize(this.bucketSize);
        vwapOrder.setLookbackPeriod(this.lookBackDays);
        vwapOrder.setMinInterval(60);
        vwapOrder.setMaxInterval(60);
        vwapOrder.setStartTime(LocalTime.of(11, 0));
        vwapOrder.setEndTime(LocalTime.of(11, 10));
        
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setOrder(vwapOrder);
        orderStatus.setStatus(Status.EXECUTED);
        
        List<Bar> oneMinuteBars = new ArrayList<>();
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 0, 0), null, this.security, Duration.MIN_15, null, null, null, null, 9, new BigDecimal("69.7716")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 1, 0), null, this.security, Duration.MIN_15, null, null, null, null, 21, new BigDecimal("69.7756")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 2, 0), null, this.security, Duration.MIN_15, null, null, null, null, 8, new BigDecimal("70.2361")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 3, 0), null, this.security, Duration.MIN_15, null, null, null, null, 13, new BigDecimal("70.0253")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 4, 0), null, this.security, Duration.MIN_15, null, null, null, null, 14, new BigDecimal("69.6608")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 5, 0), null, this.security, Duration.MIN_15, null, null, null, null, 35, new BigDecimal("69.8594")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 6, 0), null, this.security, Duration.MIN_15, null, null, null, null, 8, new BigDecimal("70.1897")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 7, 0), null, this.security, Duration.MIN_15, null, null, null, null, 13, new BigDecimal("69.846")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 8, 0), null, this.security, Duration.MIN_15, null, null, null, null, 12, new BigDecimal("70.3035")));
        oneMinuteBars.add(Bar.Factory.newInstance(DateUtil.dateForYMDHMS(2016, 1, 1, 11, 9, 0), null, this.security, Duration.MIN_15, null, null, null, null, 11, new BigDecimal("70.0386")));
        
        Mockito.when(this.historicalDataService.getHistoricalBars(
                Mockito.eq(this.security.getId()), //
                Mockito.any(), //
                Mockito.any(int.class), //
                Mockito.eq(TimePeriod.SEC), //
                Mockito.eq(Duration.MIN_1), //
                Mockito.eq(MarketDataEventType.TRADES), //
                Mockito.any())).thenReturn(oneMinuteBars);
        
        VWAPOrderStateVO algoOrderState = new VWAPOrderStateVO(.5, new TreeMap<>());
        algoOrderState.storeFill(createFill(9, "69.7716"));
        algoOrderState.storeFill(createFill(21, "69.7756"));
        algoOrderState.storeFill(createFill(8, "70.2361"));
        algoOrderState.storeFill(createFill(13, "70.0253"));
        algoOrderState.storeFill(createFill(14, "69.6608"));
        algoOrderState.storeFill(createFill(35, "69.8594"));
        algoOrderState.storeFill(createFill(8, "70.1897"));
        algoOrderState.storeFill(createFill(13, "69.846"));
        algoOrderState.storeFill(createFill(12, "70.3035"));
        algoOrderState.storeFill(createFill(11, "70.0386"));

        Map<String, Object> results = this.vwapOrderService.getResults(vwapOrder, algoOrderState);

        Assert.assertEquals(3, results.size());
        Assert.assertEquals(new BigDecimal("69.93"), results.get("price"));
        Assert.assertEquals(new BigDecimal("69.93"), results.get("benchmarkPrice"));
    }

    private VWAPOrder createOrder() {
    
        VWAPOrder vwapOrder = new VWAPOrder();
        vwapOrder.setIntId(this.intId);
        vwapOrder.setSecurity(this.security);
        vwapOrder.setStrategy(this.strategy);
        vwapOrder.setSide(Side.BUY);
        return vwapOrder;
    }

    private Fill createFill(int qty, String price) {
        Fill fill = new Fill();
        fill.setPrice(new BigDecimal(price));
        fill.setQuantity(qty);
        return fill;
    }

}
