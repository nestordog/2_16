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
package ch.algotrader.entity.trade;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.esper.NoopEngine;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link ch.algotrader.entity.trade.OrderDaoImpl}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class OrderDaoTest extends InMemoryDBTest {

    private OrderDao dao;

    private SecurityFamily family;

    private Forex forex;

    private Account account;

    private Strategy strategy;

    public OrderDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new OrderDaoImpl(this.sessionFactory, NoopEngine.SERVER);

        this.family = new SecurityFamilyImpl();
        this.family.setName("Forex1");
        this.family.setTickSizePattern("0<0.1");
        this.family.setCurrency(Currency.USD);

        this.forex = new ForexImpl();
        this.forex.setSymbol("EUR.USD");
        this.forex.setBaseCurrency(Currency.EUR);
        this.forex.setSecurityFamily(this.family);

        this.account = new AccountImpl();
        this.account.setName("name1");
        this.account.setBroker(Broker.CNX);
        this.account.setOrderServiceType(OrderServiceType.CNX_FIX);
        this.account.setSessionQualifier("CNX_FIX");
        this.account.setActive(true);

        this.strategy = new StrategyImpl();
        this.strategy.setName("Strategy1");
    }

    @Test
    public void testFindLastIntOrderId() {

        Order order = new MarketOrderImpl();
        order.setIntId("ibn123.0");
        order.setDateTime(new Date());
        order.setSide(Side.BUY);
        order.setTif(TIF.ATC);
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(this.account);
        this.session.save(this.family);
        this.session.save(this.forex);
        this.session.save(order);
        this.session.flush();

        BigDecimal bigDecimal1 = this.dao.findLastIntOrderId("FAKE");

        Assert.assertNull(bigDecimal1);

        BigDecimal bigDecimal2 = this.dao.findLastIntOrderId("CNX_FIX");

        Assert.assertNotNull(bigDecimal2);
    }

    @Test
    public void testFindUnacknowledgedOrderIds() {

        Order order = new MarketOrderImpl();
        order.setIntId("ibn123.0");
        order.setDateTime(new Date());
        order.setSide(Side.BUY);
        order.setTif(TIF.ATC);
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(this.account);
        this.session.save(this.family);
        this.session.save(this.forex);
        this.session.save(order);
        this.session.flush();

        List<Integer> integers = this.dao.findUnacknowledgedOrderIds();

        Assert.assertEquals(1, integers.size());

        Assert.assertEquals(order.getId(), integers.get(0).intValue());
    }

    @Test
    public void testFindByIds() {

        Order order = new MarketOrderImpl();
        order.setIntId("ibn123.0");
        order.setDateTime(new Date());
        order.setSide(Side.BUY);
        order.setTif(TIF.ATC);
        order.setSecurity(this.forex);
        order.setAccount(this.account);
        order.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(this.account);
        this.session.save(this.family);
        this.session.save(this.forex);
        this.session.save(order);
        this.session.flush();

        List<Integer> ids1 = new ArrayList<Integer>();
        ids1.add(0);

        List<Order> orders1 = this.dao.findByIds(ids1);

        Assert.assertEquals(0, orders1.size());

        List<Integer> ids2 = new ArrayList<Integer>();
        ids2.add(order.getId());

        List<Order> orders2 = this.dao.findByIds(ids2);

        Assert.assertEquals(1, orders2.size());

        Assert.assertSame(order, orders2.get(0));
    }

    @Test
    public void testFindAllOpenOrders() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindOpenOrdersByStrategy() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindOpenOrdersByStrategyAndSecurity() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindOpenOrderByIntId() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindOpenOrderByRootIntId() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindOpenOrderByExtId() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindOpenOrdersByParentIntId() {

        // Could not test the method due to EngineLocator dependency
    }

}
