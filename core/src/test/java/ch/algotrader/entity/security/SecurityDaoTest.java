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
package ch.algotrader.entity.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link ch.algotrader.entity.security.SecurityDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class SecurityDaoTest extends InMemoryDBTest {

    private SecurityDao dao;

    public SecurityDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();
        this.dao = new SecurityDaoImpl(this.sessionFactory);
    }

    @Test
    public void testFindById() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        Forex security1 = new ForexImpl();
        security1.setSymbol("EUR.USD");
        security1.setBaseCurrency(Currency.USD);
        security1.setSecurityFamily(family1);

        this.session.save(family1);
        this.session.save(security1);
        this.session.flush();

        Security security2 = this.dao.findById(0);

        Assert.assertNull(security2);

        Security security3 = this.dao.findById(security1.getId());

        Assert.assertNotNull(security3);
        Assert.assertSame(family1, security3.getSecurityFamily());
        Assert.assertSame(security1, security3);
    }

    @Test
    public void testFindByIds() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.USD);

        Forex security1 = new ForexImpl();
        security1.setSymbol("USD.USD");
        security1.setBaseCurrency(Currency.USD);
        security1.setSecurityFamily(family1);

        Forex security2 = new ForexImpl();
        security2.setSymbol("AUD.USD");
        security2.setBaseCurrency(Currency.AUD);
        security2.setSecurityFamily(family1);

        this.session.save(family1);
        this.session.save(security1);
        this.session.save(security2);
        this.session.flush();

        List<Integer> ids = new ArrayList<Integer>();

        ids.add(security1.getId());

        List<Security> securities1 = this.dao.findByIds(ids);

        Assert.assertEquals(1, securities1.size());
        Assert.assertSame(security1.getSecurityFamily(), securities1.get(0).getSecurityFamily());
        Assert.assertSame(security1, securities1.get(0));

        ids.add(security2.getId());

        List<Security> securities2 = this.dao.findByIds(ids);

        Assert.assertEquals(2, securities2.size());
        Assert.assertSame(family1, securities2.get(0).getSecurityFamily());
        Assert.assertSame(security1, securities2.get(0));
        Assert.assertSame(family1, securities2.get(1).getSecurityFamily());
        Assert.assertSame(security2, securities2.get(1));
    }

    @Test
    public void testFindBySymbol() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family1);

        this.session.save(family1);
        this.session.save(security1);
        this.session.flush();

        Security security2 = this.dao.findBySymbol("USD.EUR");

        Assert.assertNull(security2);

        Security security3 = this.dao.findBySymbol("GBP.EUR");

        Assert.assertNotNull(security3);
        Assert.assertSame(family1, security3.getSecurityFamily());
        Assert.assertSame(security1, security3);
    }

    @Test
    public void testFindByIsin() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family1);
        security1.setIsin("US0378331005");

        this.session.save(family1);
        this.session.save(security1);
        this.session.flush();

        Security security2 = this.dao.findByIsin("NOT_FOUND");

        Assert.assertNull(security2);

        Security security3 = this.dao.findByIsin("US0378331005");

        Assert.assertNotNull(security3);
        Assert.assertSame(family1, security3.getSecurityFamily());
        Assert.assertSame(security1, security3);
    }

    @Test
    public void testFindByBbgid() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family1);
        security1.setIsin("US0378331005");
        security1.setBbgid("BBG005Y3Z8B6");

        this.session.save(family1);
        this.session.save(security1);
        this.session.flush();

        Security security2 = this.dao.findByBbgid("NOT_FOUND");

        Assert.assertNull(security2);

        Security security3 = this.dao.findByBbgid("BBG005Y3Z8B6");

        Assert.assertNotNull(security3);
        Assert.assertSame(family1, security3.getSecurityFamily());
        Assert.assertSame(security1, security3);
    }

    @Test
    public void tesFindByRic() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family1);
        security1.setIsin("US0378331005");
        security1.setBbgid("BBG005Y3Z8B6");
        security1.setRic("RIC");

        this.session.save(family1);
        this.session.save(security1);
        this.session.flush();

        Security security2 = this.dao.findByRic("NOT_FOUND");

        Assert.assertNull(security2);

        Security security3 = this.dao.findByRic("RIC");

        Assert.assertNotNull(security3);
        Assert.assertSame(family1, security3.getSecurityFamily());
        Assert.assertSame(security1, security3);
    }

    @Test
    public void testFindByConid() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family1);
        security1.setIsin("US0378331005");
        security1.setBbgid("BBG005Y3Z8B6");
        security1.setRic("RIC");
        security1.setConid("CONID");

        this.session.save(family1);
        this.session.save(security1);
        this.session.flush();

        Security security2 = this.dao.findByConid("NOT_FOUND");

        Assert.assertNull(security2);

        Security security3 = this.dao.findByConid("CONID");

        Assert.assertNotNull(security3);
        Assert.assertSame(family1, security3.getSecurityFamily());
        Assert.assertSame(security1, security3);
    }

    @Test
    public void testFindByIdInclFamilyAndUnderlying() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.AUD);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("GBP.EUR");
        forex1.setBaseCurrency(Currency.AUD);
        forex1.setSecurityFamily(family1);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family2);
        security1.setIsin("US0378331005");
        security1.setBbgid("BBG005Y3Z8B6");
        security1.setRic("RIC");
        security1.setConid("CONID");
        security1.setUnderlying(forex1);

        this.session.save(family1);
        this.session.save(family2);
        this.session.save(forex1);
        this.session.save(security1);
        this.session.flush();

        Security security2 = this.dao.findByIdInclFamilyAndUnderlying(0);

        Assert.assertNull(security2);

        Security security3 = this.dao.findByIdInclFamilyAndUnderlying(security1.getId());

        Assert.assertNotNull(security3);
        Assert.assertSame(family1, security3.getUnderlying().getSecurityFamily());
        Assert.assertSame(forex1, security3.getUnderlying());
        Assert.assertSame(family2, security3.getSecurityFamily());
        Assert.assertSame(security1, security3);
    }

    @Test
    public void testFindSubscribedForAutoActivateStrategies() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("INR.EUR");
        forex1.setBaseCurrency(Currency.CAD);
        forex1.setSecurityFamily(family1);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
        strategy1.setAutoActivate(Boolean.FALSE);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(forex1);
        subscription1.setStrategy(strategy1);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family1);
        security1.addSubscriptions(subscription1);

        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.AUD);

        Forex forex2 = new ForexImpl();
        forex2.setSymbol("INR.USD");
        forex2.setBaseCurrency(Currency.INR);
        forex2.setSecurityFamily(family2);

        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
        strategy2.setAutoActivate(Boolean.FALSE);

        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.SIM);
        subscription2.setSecurity(forex2);
        subscription2.setStrategy(strategy2);

        Forex security2 = new ForexImpl();
        security2.setSymbol("INR.AUD");
        security2.setBaseCurrency(Currency.AUD);
        security2.setSecurityFamily(family1);
        security2.addSubscriptions(subscription2);

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
        this.session.save(security1);
        this.session.save(subscription1);
        this.session.save(family2);
        this.session.save(forex2);
        this.session.save(strategy2);
        this.session.save(security2);
        this.session.save(subscription2);
        this.session.flush();

        List<Security> securities1 = this.dao.findSubscribedForAutoActivateStrategies();

        Assert.assertEquals(0, securities1.size());

        strategy1.setAutoActivate(Boolean.TRUE);
        this.session.flush();

        List<Security> securities2 = this.dao.findSubscribedForAutoActivateStrategies();

        Assert.assertEquals(1, securities2.size());
        Assert.assertSame(security1, securities2.get(0));

        strategy2.setAutoActivate(Boolean.TRUE);
        this.session.flush();

        List<Security> securities3 = this.dao.findSubscribedForAutoActivateStrategies();

        Assert.assertEquals(2, securities3.size());
        Assert.assertSame(security1, securities3.get(0));
        Assert.assertSame(security2, securities3.get(1));
    }

    @Test
    public void testFindSubscribedByFeedTypeForAutoActivateStrategiesInclFamily() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("INR.EUR");
        forex1.setBaseCurrency(Currency.CAD);
        forex1.setSecurityFamily(family1);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
        strategy1.setAutoActivate(Boolean.TRUE);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(forex1);
        subscription1.setStrategy(strategy1);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family1);
        security1.addSubscriptions(subscription1);

        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.AUD);

        Forex forex2 = new ForexImpl();
        forex2.setSymbol("INR.USD");
        forex2.setBaseCurrency(Currency.INR);
        forex2.setSecurityFamily(family2);

        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
        strategy2.setAutoActivate(Boolean.TRUE);

        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.SIM);
        subscription2.setSecurity(forex2);
        subscription2.setStrategy(strategy2);

        Forex security2 = new ForexImpl();
        security2.setSymbol("INR.AUD");
        security2.setBaseCurrency(Currency.AUD);
        security2.setSecurityFamily(family1);
        security2.addSubscriptions(subscription2);

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
        this.session.save(security1);
        this.session.save(subscription1);
        this.session.save(family2);
        this.session.save(forex2);
        this.session.save(strategy2);
        this.session.save(security2);
        this.session.save(subscription2);
        this.session.flush();

        List<Security> securities1 = this.dao.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(FeedType.BB);

        Assert.assertEquals(0, securities1.size());

        subscription1.setFeedType(FeedType.BB);
        this.session.flush();

        List<Security> securities2 = this.dao.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(FeedType.BB);

        Assert.assertEquals(1, securities2.size());
        Assert.assertSame(security1, securities2.get(0));

        subscription2.setFeedType(FeedType.BB);
        this.session.flush();

        List<Security> securities3 = this.dao.findSubscribedByFeedTypeForAutoActivateStrategiesInclFamily(FeedType.BB);

        Assert.assertEquals(2, securities3.size());
        Assert.assertSame(security1, securities3.get(0));
        Assert.assertSame(security2, securities3.get(1));
    }

    @Test
    public void testFindSubscribedByFeedTypeAndStrategyInclFamily() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("INR.EUR");
        forex1.setBaseCurrency(Currency.CAD);
        forex1.setSecurityFamily(family1);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
        strategy1.setAutoActivate(Boolean.TRUE);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.BB);
        subscription1.setSecurity(forex1);
        subscription1.setStrategy(strategy1);

        Forex security1 = new ForexImpl();
        security1.setSymbol("GBP.EUR");
        security1.setBaseCurrency(Currency.AUD);
        security1.setSecurityFamily(family1);
        security1.addSubscriptions(subscription1);

        SecurityFamily family2 = new SecurityFamilyImpl();
        family2.setName("family2");
        family2.setTickSizePattern("0<0.1");
        family2.setCurrency(Currency.AUD);

        Forex forex2 = new ForexImpl();
        forex2.setSymbol("INR.USD");
        forex2.setBaseCurrency(Currency.INR);
        forex2.setSecurityFamily(family2);

        Strategy strategy2 = new StrategyImpl();
        strategy2.setName("Strategy2");
        strategy2.setAutoActivate(Boolean.TRUE);

        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.BB);
        subscription2.setSecurity(forex2);
        subscription2.setStrategy(strategy2);

        Forex security2 = new ForexImpl();
        security2.setSymbol("INR.AUD");
        security2.setBaseCurrency(Currency.AUD);
        security2.setSecurityFamily(family1);
        security2.addSubscriptions(subscription2);

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
        this.session.save(security1);
        this.session.save(subscription1);
        this.session.save(family2);
        this.session.save(forex2);
        this.session.save(strategy2);
        this.session.save(security2);
        this.session.save(subscription2);
        this.session.flush();

        List<Security> securities1 = this.dao.findSubscribedByFeedTypeAndStrategyInclFamily(FeedType.BB, "Dummy");

        Assert.assertEquals(0, securities1.size());

        List<Security> securities2 = this.dao.findSubscribedByFeedTypeAndStrategyInclFamily(FeedType.BB, "Strategy1");

        Assert.assertEquals(1, securities2.size());
        Assert.assertSame(security1, securities2.get(0));

        subscription2.setStrategy(strategy1);
        this.session.flush();

        List<Security> securities3 = this.dao.findSubscribedByFeedTypeAndStrategyInclFamily(FeedType.BB, "Strategy1");

        Assert.assertEquals(2, securities3.size());
        Assert.assertSame(security1, securities3.get(0));
        Assert.assertSame(security2, securities3.get(1));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testFindSubscribedAndFeedTypeForAutoActivateStrategies() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("INR.EUR");
        forex1.setBaseCurrency(Currency.CAD);
        forex1.setSecurityFamily(family1);

        Strategy strategy1 = new StrategyImpl();
        strategy1.setName("Strategy1");
        strategy1.setAutoActivate(Boolean.FALSE);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(forex1);
        subscription1.setStrategy(strategy1);

        this.session.save(family1);
        this.session.save(forex1);
        this.session.save(strategy1);
        this.session.save(subscription1);
        this.session.flush();

        List<Map> maps1 = this.dao.findSubscribedAndFeedTypeForAutoActivateStrategies();

        Assert.assertEquals(0, maps1.size());

        strategy1.setAutoActivate(Boolean.TRUE);
        this.session.flush();

        List<Map> maps2 = this.dao.findSubscribedAndFeedTypeForAutoActivateStrategies();

        Assert.assertEquals(1, maps2.size());

        Assert.assertSame(FeedType.SIM, maps2.get(0).get("feedType"));
        Assert.assertSame(forex1, maps2.get(0).get("security"));
    }

    @Test
    public void testFindSecurityIdByIsin() {

        SecurityFamily family1 = new SecurityFamilyImpl();
        family1.setName("family1");
        family1.setTickSizePattern("0<0.1");
        family1.setCurrency(Currency.EUR);

        Forex forex1 = new ForexImpl();
        forex1.setSymbol("INR.EUR");
        forex1.setBaseCurrency(Currency.CAD);
        forex1.setIsin("isin");
        forex1.setSecurityFamily(family1);

        this.session.save(family1);
        this.session.save(forex1);
        this.session.flush();

        Integer id1 = this.dao.findSecurityIdByIsin("Dummy");

        Assert.assertNull(id1);

        Integer id2 = this.dao.findSecurityIdByIsin("isin");

        Assert.assertNotNull(id2);
        Assert.assertEquals(forex1.getId(), id2.intValue());
    }

}
