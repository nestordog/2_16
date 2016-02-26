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
package ch.algotrader.service;

import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.marketData.TickVOBuilder;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVOBuilder;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.algo.SlicingOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.algo.SlicingOrderService;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class SlicingOrderServiceTest {

    private static final int ITERATIONS = 50;
    private static final Random random = new Random();

    private static MarketDataCacheService marketDataCacheService;
    private static SimpleOrderService simpleOrderService;
    private static OrderBook orderBook;
    private static SlicingOrderService slicingOrderService;

    private static Security security;
    private static Strategy strategy;
    private static String intId = "XYZ";

    private static TickVO tick;
    private static long remainingQty;
    private static SimpleOrder slice;

    @BeforeClass
    public static void setup() {
        
        marketDataCacheService = Mockito.mock(MarketDataCacheService.class);
        orderBook = Mockito.mock(OrderBook.class);

        simpleOrderService = new SimpleOrderService() {

            @Override
            public void validateOrder(SimpleOrder order) throws OrderValidationException {
            }

            @Override
            public void sendOrder(SimpleOrder order) {
                slice = order;
            }

            @Override
            public void cancelOrder(SimpleOrder order) {
            }

            @Override
            public void modifyOrder(SimpleOrder order) {
            }

            @Override
            public String getNextOrderId(Account account) {
                return null;
            }
        };

        slicingOrderService = new SlicingOrderService(marketDataCacheService, simpleOrderService, orderBook);

        SecurityFamily securityFamily = SecurityFamily.Factory.newInstance("", Currency.USD, 1, 2, "0<0.01", true, false);
        security = Stock.Factory.newInstance(securityFamily);
        security.setSymbol("TEST_SECURITY");
        security.setId(1);

        strategy = Strategy.Factory.newInstance("TEST_STRATEGY", false);

        when(marketDataCacheService.getCurrentMarketDataEvent(security.getId())).then(i -> {
            return tick;
        });
        
        when(orderBook.getStatusByIntId(intId)).then(invocation -> {
            return OrderStatusVOBuilder.create().setRemainingQuantity(remainingQty).build();
        });
    }

    @Test
    public void testPopulate() throws IllegalAccessException, InvocationTargetException, OrderValidationException {

        SlicingOrder order = new SlicingOrder();

        String nameValues = "side=BUY,quantity=2,minVolPct=0.5,maxVolPct=1.5,minQuantity=5,maxQuantity=10,minDuration=1.0,maxDuration=2.0,minDelay=1.0,maxDelay=2.0";

        Map<String, String> properties = new HashMap<>();
        for (String nameValue : nameValues.split(",")) {
            properties.put(nameValue.split("=")[0], nameValue.split("=")[1]);
        }

        BeanUtil.populate(order, properties);
    }

    @Test(expected = OrderValidationException.class)
    public void testValidateFail() throws OrderValidationException {

        SlicingOrder order = createInstance();

        order.validate();
    }

    @Test()
    public void testValidate() throws OrderValidationException {

        SlicingOrder order = createInstance();
        order.setMaxQuantity(80);
        order.setMaxDuration(1.0);
        order.setMaxDelay(1.0);

        order.validate();

        order = createInstance();
        order.setMaxVolPct(0.8);
        order.setMaxDuration(1.0);
        order.setMaxDelay(1.0);

        order.validate();
    }

    @Test
    public void testQty() throws OrderValidationException {

        for (int i = 0; i < ITERATIONS; i++) {
            testQtyAbs();
            testQtyPct();
            testQtyCombined();
        }
    }

    public void testQtyAbs() throws OrderValidationException {

        SlicingOrder order = createInstance();

        order.setSide(random.nextBoolean() ? Side.BUY : Side.SELL);
        order.setQuantity(200);
        order.setMinQuantity(10);
        order.setMaxQuantity(80);
        order.setMaxDuration(1.0);
        order.setMaxDelay(1.0);

        processOrder(order);
    }

    public void testQtyPct() throws OrderValidationException {

        SlicingOrder order = createInstance();

        order.setSide(random.nextBoolean() ? Side.BUY : Side.SELL);
        order.setQuantity(200);
        order.setMinVolPct(0.2);
        order.setMaxVolPct(0.8);
        order.setMaxDuration(1.0);
        order.setMaxDelay(1.0);

        processOrder(order);
    }

    public void testQtyCombined() throws OrderValidationException {

        SlicingOrder order = createInstance();

        order.setSide(random.nextBoolean() ? Side.BUY : Side.SELL);
        order.setQuantity(200);
        order.setMinVolPct(0.2);
        order.setMaxVolPct(0.8);
        order.setMinQuantity(10);
        order.setMaxQuantity(80);
        order.setMaxDuration(1.0);
        order.setMaxDelay(1.0);

        processOrder(order);
    }

    private SlicingOrder createInstance() {

        // update market data
        final int volBid = 10 + random.nextInt(90); // volBid: 10 - 100
        final int volAsk = 10 + random.nextInt(90); // volAsk: 10 - 100
        final BigDecimal bid = RoundUtil.getBigDecimal(10 + random.nextDouble() * 0.1, 2); // bid: 10 - 10.1
        final BigDecimal ask = RoundUtil.getBigDecimal(bid.doubleValue() + 0.01 + random.nextDouble() * 0.09, 2); // spread: 0.01 - 0.1

        tick = TickVOBuilder.create().setSecurityId(security.getId()).setBid(bid).setAsk(ask).setVolBid(volBid).setVolAsk(volAsk).build();

        SlicingOrder order = new SlicingOrder();
        order.setIntId(intId);
        order.setSecurity(security);
        order.setStrategy(strategy);

        return order;
    }

    private void processOrder(SlicingOrder order) throws OrderValidationException {

        remainingQty = order.getQuantity();

        slicingOrderService.validateOrder(order);
        // send order
        slicingOrderService.sendOrder(order);
        verifyAndFill(order);

        // work through order
        while (remainingQty > 0) {

            slicingOrderService.sendNextOrder(order);
            verifyAndFill(order);
        }

        // order executed
        OrderStatus orderStatusExecuted = OrderStatus.Factory.newInstance(new Date(), Status.EXECUTED, order.getQuantity(), 0l, 0l, intId, 0l, order);
        slicingOrderService.handleOrderStatus(orderStatusExecuted);
    }

    private void verifyAndFill(final SlicingOrder order) {

        long marketVolume;
        if (Side.BUY.equals(order.getSide())) {
            marketVolume = tick.getVolAsk();
        } else {
            marketVolume = tick.getVolBid();
        }

        // validate quantities
        Assert.assertTrue("qty = 0", slice.getQuantity() > 0);
        Assert.assertTrue("qty > remainingQty", slice.getQuantity() <= remainingQty);

        if (remainingQty >= order.getMinQuantity() && marketVolume >= order.getMinQuantity()) {
            boolean condition = slice.getQuantity() >= order.getMinQuantity();
            if (order.getMaxVolPct() != 0.0) {
                Assert.assertTrue("qty < minQty", condition || slice.getQuantity() >= order.getMaxVolPct() * marketVolume);
            } else {
                Assert.assertTrue("qty < minQty", condition);
            }
        }

        if (order.getMaxQuantity() > 0.0) {
            Assert.assertTrue("qty > maxQty", slice.getQuantity() <= order.getMaxQuantity());
        }

        if (order.getMinVolPct() != 0.0 && remainingQty >= marketVolume * order.getMinVolPct()) {
            Assert.assertTrue("qty < minVolPct", slice.getQuantity() >= Math.round(marketVolume * order.getMinVolPct()));
        }

        if (order.getMaxVolPct() != 0.0) {
            Assert.assertTrue("qty > minVolPct", slice.getQuantity() <= Math.round(marketVolume * order.getMaxVolPct()));
        }

        boolean filled = random.nextBoolean();
        if (filled) {
            long filledQty = Math.round(random.nextDouble() * slice.getQuantity());
            remainingQty -= filledQty;
            slicingOrderService.increaseOffsetTicks(order);
        } else {
            slicingOrderService.decreaseOffsetTicks(order);
        }
    }

}
