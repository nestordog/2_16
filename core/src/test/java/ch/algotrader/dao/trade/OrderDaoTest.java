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
package ch.algotrader.dao.trade;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import ch.algotrader.entity.trade.MarketOrderImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.hibernate.InMemoryDBTest;
import ch.algotrader.util.DateTimeLegacy;

/**
* Unit tests for {@link OrderDaoImpl}.
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

        this.dao = new OrderDaoImpl(this.sessionFactory);

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
        this.account.setBroker(Broker.CNX.name());
        this.account.setOrderServiceType(OrderServiceType.CNX_FIX.name());
        this.account.setSessionQualifier("CNX_FIX");
        this.account.setActive(true);

        this.strategy = new StrategyImpl();
        this.strategy.setName("Strategy1");
    }

    @Test
    public void testFindLastIntOrderIdBySessionQualifier() {

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

        BigDecimal bigDecimal1 = this.dao.findLastIntOrderIdBySessionQualifier("FAKE");

        Assert.assertNull(bigDecimal1);

        BigDecimal bigDecimal2 = this.dao.findLastIntOrderIdBySessionQualifier("CNX_FIX");

        Assert.assertNotNull(bigDecimal2);
    }

    @Test
    public void testFindLastIntOrderIdByOrderServiceType() {

        Order order = new MarketOrderImpl();
        order.setIntId("35");
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

        BigDecimal bigDecimal1 = this.dao.findLastIntOrderIdByServiceType("FAKE");

        Assert.assertNull(bigDecimal1);

        BigDecimal bigDecimal2 = this.dao.findLastIntOrderIdByServiceType("CNX_FIX");

        Assert.assertNotNull(bigDecimal2);
        Assert.assertEquals(35, bigDecimal2.intValue());
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

        List<Long> ids = this.dao.findUnacknowledgedOrderIds();

        Assert.assertEquals(1, ids.size());

        Assert.assertEquals(order.getId(), ids.get(0).longValue());
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

        List<Long> ids1 = new ArrayList<>();
        ids1.add((long) 0);

        List<Order> orders1 = this.dao.findByIds(ids1);

        Assert.assertEquals(0, orders1.size());

        List<Long> ids2 = new ArrayList<>();
        ids2.add((long) order.getId());

        List<Order> orders2 = this.dao.findByIds(ids2);

        Assert.assertEquals(1, orders2.size());

        Assert.assertSame(order, orders2.get(0));
    }

    @Test
    public void testFindByIntId() {

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

        Order order1 = this.dao.findByIntId("ibn123.0");

        Assert.assertEquals(order, order1);

        Order order2 = this.dao.findByIntId("blah");
        Assert.assertNull(order2);
    }

    @Test
    public void testFindDailyOrders() {
        Order order1 = new MarketOrderImpl();
        order1.setIntId("35");
        order1.setDateTime(new Date());
        order1.setSide(Side.BUY);
        order1.setTif(TIF.ATC);
        order1.setSecurity(this.forex);
        order1.setAccount(this.account);
        order1.setStrategy(this.strategy);

        Order order2 = new MarketOrderImpl();
        order2.setIntId("36");
        order2.setDateTime(DateTimeLegacy.toLocalDateTime(LocalDateTime.now().minusDays(2)));
        order2.setSide(Side.BUY);
        order2.setTif(TIF.ATC);
        order2.setSecurity(this.forex);
        order2.setAccount(this.account);
        order2.setStrategy(this.strategy);

        this.session.save(this.strategy);
        this.session.save(this.account);
        this.session.save(this.family);
        this.session.save(this.forex);
        this.session.flush();

        List<Order> dailyOrders1 = this.dao.getDailyOrders();
        Assert.assertNotNull(dailyOrders1);
        Assert.assertEquals(0, dailyOrders1.size());

        this.session.save(order1);
        this.session.save(order2);
        this.session.flush();

        List<Order> dailyOrders2 = this.dao.getDailyOrders();
        Assert.assertNotNull(dailyOrders2);
        Assert.assertEquals(1, dailyOrders2.size());

        List<Order> dailyOrders3 = this.dao.getDailyOrdersByStrategy("blah");
        Assert.assertNotNull(dailyOrders3);
        Assert.assertEquals(0, dailyOrders3.size());

        List<Order> dailyOrders4 = this.dao.getDailyOrdersByStrategy("Strategy1");
        Assert.assertNotNull(dailyOrders4);
        Assert.assertEquals(1, dailyOrders4.size());
    }

}
