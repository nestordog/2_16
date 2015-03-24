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
package ch.algotrader.entity.order;

import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.SlicingOrder;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@RunWith(MockitoJUnitRunner.class)
public class SlicingOrderTest {

    private static final int ITERATIONS = 50;
    private static final Random random = new Random();

    @Mock
    private Tick tick;

    @Before
    public void beforeEach() {
        final int volBid = 10 + random.nextInt(90); // volBid: 10 - 100
        final int volAsk = 10 + random.nextInt(90); // volAsk: 10 - 100
        final BigDecimal bid = RoundUtil.getBigDecimal(10 + random.nextDouble() * 0.1, 2); // bid: 10 - 10.1
        final BigDecimal ask = RoundUtil.getBigDecimal(bid.doubleValue() + 0.01 + random.nextDouble() * 0.09, 2); // spread: 0.01 - 0.1

        when(tick.getBid()).thenReturn(bid);
        when(tick.getAsk()).thenReturn(ask);
        when(tick.getVolBid()).thenReturn(volBid);
        when(tick.getVolAsk()).thenReturn(volAsk);
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

        processSlices(order);
    }

    public void testQtyPct() throws OrderValidationException {

        SlicingOrder order = createInstance();

        order.setSide(random.nextBoolean() ? Side.BUY : Side.SELL);
        order.setQuantity(200);
        order.setMinVolPct(0.2);
        order.setMaxVolPct(0.8);
        order.setMaxDuration(1.0);
        order.setMaxDelay(1.0);

        processSlices(order);
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

        processSlices(order);
    }

    private SlicingOrder createInstance() {

        Security security = Mockito.mock(Security.class);
        Strategy strategy = Strategy.Factory.newInstance("TEST_STRATEGY", false, 0);
        Subscription subscription = Subscription.Factory.newInstance(null, FeedType.IB, true, strategy, security);
        SecurityFamily securityFamily = SecurityFamily.Factory.newInstance("", Currency.USD, 1, 2, "0<0.01", true, false);

        Mockito.when(security.getSubscriptions()).thenReturn(Collections.singleton(subscription));
        Mockito.when(security.getSecurityFamily()).thenReturn(securityFamily);
        Mockito.when(security.toString()).thenReturn("TEST_SECURITY");

        SlicingOrder order = new SlicingOrder();
        order.setSecurity(security);
        order.setStrategy(strategy);

        return order;
    }

    private void processSlices(SlicingOrder order) {

        long remainingQty = order.getQuantity();
        SimpleOrder slice;
        do {

            slice = order.nextOrder(remainingQty, tick);

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

            // create fills
            boolean filled = random.nextBoolean();
            if (filled) {
                long filledQty = Math.round(random.nextDouble() * slice.getQuantity());
                remainingQty -= filledQty;
                order.increaseOffsetTicks();
            } else {
                order.decreaseOffsetTicks();
            }
        } while (remainingQty > 0);
    }

}
