/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.rt;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderImpl;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.OrderService;

/**
 * RealTick MarketOrderTest
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class RTOrderTest {

    private static final String ACCOUNT_NAME = "RT_TEST";
    private static final String SYMBOL = "GOOG";
    private static final String STRATEGY_NAME = "TESTING";

    private LinkedBlockingQueue<Serializable> queue;
    private LookupService lookupService;
    private OrderService orderService;

    @Before @SuppressWarnings("unchecked")
    public void setup() {

        ServiceLocator.instance().init(ServiceLocator.CLIENT_BEAN_REFERENCE_LOCATION);

        this.queue = ServiceLocator.instance().getService("queue", LinkedBlockingQueue.class);
        this.lookupService = ServiceLocator.instance().getService("lookupService", LookupService.class);
        this.orderService = ServiceLocator.instance().getService("orderService", OrderService.class);
    }

    @Test
    public void testMarketOrder() throws Exception {

        Security stock = this.lookupService.getSecurityBySymbol(SYMBOL);
        Strategy strategy = this.lookupService.getStrategyByName(STRATEGY_NAME);
        Account rtTest = this.lookupService.getAccountByName(ACCOUNT_NAME);

        MarketOrder marketOrder = new MarketOrderImpl();
        marketOrder.setSecurity(stock);
        marketOrder.setStrategy(strategy);
        marketOrder.setAccount(rtTest);
        marketOrder.setSide(Side.BUY);
        marketOrder.setQuantity(100);

        marketOrder.validate();
        this.orderService.sendOrder(marketOrder);

        List<Serializable> session = new ArrayList<Serializable>();
        Serializable obj;
        while ((obj = this.queue.poll(10, TimeUnit.SECONDS)) != null) {
            session.add(obj);
            if (session.size() == 5) {
                break;
            }
        }

        Assert.assertEquals("Unexpected number of messages", 5, session.size());

        Serializable obj1 = session.get(0);
        Assert.assertTrue("Market order expected", obj1 instanceof MarketOrder);
        MarketOrder incomingmarketOrder = (MarketOrder) obj1;
        Assert.assertEquals(Side.BUY, incomingmarketOrder.getSide());
        Assert.assertEquals(100, incomingmarketOrder.getQuantity());

        Serializable obj2 = session.get(1);
        Assert.assertTrue("Status expected", obj2 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) obj2;
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(100, orderStatus1.getRemainingQuantity());

        Serializable obj3 = session.get(2);
        Assert.assertTrue("Status expected", obj3 instanceof OrderStatus);
        OrderStatus status2 = (OrderStatus) obj3;
        Assert.assertEquals(Status.EXECUTED, status2.getStatus());
        Assert.assertEquals(100, status2.getFilledQuantity());
        Assert.assertEquals(0, status2.getRemainingQuantity());

        Object obj4 = session.get(3);
        Assert.assertTrue("Fill expected", obj4 instanceof Fill);
        Fill fill = (Fill) obj4;
        Assert.assertEquals(100, fill.getQuantity());
        Assert.assertNotNull(fill.getPrice());

        Object obj5 = session.get(4);
        Assert.assertTrue("Transaction expected", obj5 instanceof Transaction);
        Transaction transaction = (Transaction) obj5;
        Assert.assertEquals(100, transaction.getQuantity());
        Assert.assertEquals(TransactionType.BUY, transaction.getType());
    }

    @Test
    public void testCancelReplace() throws Exception {
        Security stock = this.lookupService.getSecurityBySymbol(SYMBOL);
        Strategy strategy = this.lookupService.getStrategyByName(STRATEGY_NAME);
        Account rtTest = this.lookupService.getAccountByName(ACCOUNT_NAME);

        LimitOrder limitOrder = new LimitOrderImpl();
        limitOrder.setSecurity(stock);
        limitOrder.setStrategy(strategy);
        limitOrder.setAccount(rtTest);
        limitOrder.setSide(Side.BUY);
        limitOrder.setQuantity(100);
        limitOrder.setLimit(new BigDecimal("10.0"));

        limitOrder.validate();
        this.orderService.sendOrder(limitOrder);

        List<Serializable> session = new ArrayList<Serializable>();
        Serializable obj;
        while ((obj = this.queue.poll(10, TimeUnit.SECONDS)) != null) {
            session.add(obj);
            if (session.size() == 2) {
                break;
            }
        }

        Assert.assertEquals("Unexpected number of messages", 2, session.size());

        Serializable obj1 = session.get(0);
        Assert.assertTrue("Limit order expected", obj1 instanceof LimitOrder);
        LimitOrder incomingLimitOrder = (LimitOrder) obj1;
        Assert.assertEquals(Side.BUY, incomingLimitOrder.getSide());
        Assert.assertEquals(100, incomingLimitOrder.getQuantity());
        Assert.assertEquals(new BigDecimal("10.0"), incomingLimitOrder.getLimit());

        Serializable obj2 = session.get(1);
        Assert.assertTrue("Status expected", obj2 instanceof OrderStatus);
        OrderStatus orderStatus1 = (OrderStatus) obj2;
        Assert.assertEquals(Status.SUBMITTED, orderStatus1.getStatus());
        Assert.assertEquals(0, orderStatus1.getFilledQuantity());
        Assert.assertEquals(100, orderStatus1.getRemainingQuantity());

        Assert.assertNotNull(orderStatus1.getExtId());
        Assert.assertNotNull(orderStatus1.getIntId());

        limitOrder.setExtId(orderStatus1.getExtId());
        limitOrder.setIntId(orderStatus1.getIntId());
        limitOrder.setLimit(new BigDecimal("15.0"));

        limitOrder.validate();
        this.orderService.modifyOrder(limitOrder);

        session.clear();
        while ((obj = this.queue.poll(10, TimeUnit.SECONDS)) != null) {
            session.add(obj);
            if (session.size() == 2) {
                break;
            }
        }

        Assert.assertEquals("Unexpected number of messages", 2, session.size());

        Serializable obj3 = session.get(0);
        Assert.assertTrue("Limit order expected", obj3 instanceof LimitOrder);
        LimitOrder incomingLimitOrder2 = (LimitOrder) obj3;
        Assert.assertEquals(Side.BUY, incomingLimitOrder2.getSide());
        Assert.assertEquals(100, incomingLimitOrder2.getQuantity());
        Assert.assertEquals(new BigDecimal("15.0"), incomingLimitOrder2.getLimit());

        Serializable obj4 = session.get(1);
        Assert.assertTrue("Status expected", obj4 instanceof OrderStatus);
        OrderStatus orderStatus2 = (OrderStatus) obj4;
        Assert.assertEquals(Status.SUBMITTED, orderStatus2.getStatus());
        Assert.assertEquals(0, orderStatus2.getFilledQuantity());
        Assert.assertEquals(100, orderStatus2.getRemainingQuantity());

        Assert.assertNotNull(orderStatus2.getExtId());
        Assert.assertNotNull(orderStatus2.getIntId());

        limitOrder.setExtId(orderStatus2.getExtId());
        limitOrder.setIntId(orderStatus2.getIntId());

        this.orderService.cancelOrder(limitOrder);

        session.clear();
        while ((obj = this.queue.poll(10, TimeUnit.SECONDS)) != null) {
            session.add(obj);
            if (session.size() == 2) {
                break;
            }
        }

        Assert.assertEquals("Unexpected number of messages", 2, session.size());

        Serializable obj5 = session.get(0);
        Assert.assertTrue("Limit order expected", obj5 instanceof LimitOrder);
        LimitOrder incomingLimitOrder3 = (LimitOrder) obj5;
        Assert.assertEquals(Side.BUY, incomingLimitOrder3.getSide());
        Assert.assertEquals(100, incomingLimitOrder3.getQuantity());
        Assert.assertEquals(new BigDecimal("15.0"), incomingLimitOrder3.getLimit());

        Serializable obj6 = session.get(1);
        Assert.assertTrue("Status expected", obj6 instanceof OrderStatus);
        OrderStatus orderStatus3 = (OrderStatus) obj6;
        Assert.assertEquals(Status.CANCELED, orderStatus3.getStatus());
        Assert.assertEquals(0, orderStatus3.getFilledQuantity());
        Assert.assertEquals(100, orderStatus3.getRemainingQuantity());
    }

}
