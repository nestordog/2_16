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

package ch.algotrader.entity.marketData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionDao;
import ch.algotrader.entity.SubscriptionDaoImpl;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.ImpliedVolatility;
import ch.algotrader.entity.security.ImpliedVolatilityImpl;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionImpl;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.security.SecurityDaoImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.esper.NoopEngine;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link ch.algotrader.entity.marketData.TickDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class TickDaoTest extends InMemoryDBTest {

    private TickDao dao;

    private SecurityFamily family1;

    private Forex forex1;

    private Strategy strategy1;

    private SecurityFamily family2;

    private Forex forex2;

    private Strategy strategy2;

    public TickDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        SubscriptionDao subscriptionDao = new SubscriptionDaoImpl(this.sessionFactory);
        SecurityDao securityDao = new SecurityDaoImpl(this.sessionFactory);

        this.dao = new TickDaoImpl(this.sessionFactory, subscriptionDao, securityDao, NoopEngine.SERVER);

        this.family1 = new SecurityFamilyImpl();
        this.family1.setName("Forex1");
        this.family1.setTickSizePattern("0<0.1");
        this.family1.setCurrency(Currency.USD);

        this.forex1 = new ForexImpl();
        this.forex1.setSymbol("EUR.USD");
        this.forex1.setBaseCurrency(Currency.EUR);
        this.forex1.setSecurityFamily(this.family1);

        this.strategy1 = new StrategyImpl();
        this.strategy1.setName("Strategy1");

        this.family2 = new SecurityFamilyImpl();
        this.family2.setName("Forex2");
        this.family2.setTickSizePattern("0<0.1");
        this.family2.setCurrency(Currency.GBP);

        this.forex2 = new ForexImpl();
        this.forex2.setSymbol("EUR.GBP");
        this.forex2.setBaseCurrency(Currency.EUR);
        this.forex2.setSecurityFamily(this.family2);

        this.strategy2 = new StrategyImpl();
        this.strategy2.setName("Strategy2");
    }

    @Test
    public void testFindBySecurity() {

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.HOUR, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.HOUR, 1);

        Tick tick1 = new TickImpl();
        tick1.setDateTime(cal1.getTime());
        tick1.setFeedType(FeedType.BB);
        tick1.setSecurity(this.forex1);

        Tick tick2 = new TickImpl();
        tick2.setDateTime(cal2.getTime());
        tick2.setFeedType(FeedType.CNX);
        tick2.setSecurity(this.forex2);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.family2);
        this.session.save(this.forex2);

        this.session.save(tick1);
        this.session.save(tick2);

        this.session.flush();

        List<Tick> ticks1 = this.dao.findBySecurity(this.forex1.getId());

        Assert.assertEquals(1, ticks1.size());
        Assert.assertSame(tick1, ticks1.get(0));

        tick2.setSecurity(this.forex1);
        this.session.flush();

        List<Tick> ticks2 = this.dao.findBySecurity(this.forex1.getId());

        Assert.assertEquals(2, ticks2.size());
        Assert.assertSame(tick1, ticks2.get(0));
        Assert.assertSame(tick2, ticks2.get(1));
    }

    @Test
    public void testFindBySecurityAndMaxDate() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "DATE_SUB" not found
    }

    @Test
    public void testFindTicksBySecurityAndMinDate() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "FROM_UNIXTIME" not found
    }

    @Test
    public void testFindTicksBySecurityAndMinDateWithLimit() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "FROM_UNIXTIME" not found
    }

    @Test
    public void testFindTicksBySecurityAndMaxDate() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "FROM_UNIXTIME" not found
    }

    @Test
    public void testFindTicksBySecurityAndMaxDateWithLimit() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "FROM_UNIXTIME" not found
    }

    @Test
    public void testFindDailyTickIdsBeforeTime() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "time" not found
    }

    @Test
    public void testFindDailyTickIdsAfterTime() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "time" not found
    }

    @Test
    public void testFindHourlyTickIdsBeforeMinutesByMinDate() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "DATE_FORMAT" not found
    }

    @Test
    public void testFindHourlyTickIdsAfterMinutesByMinDate() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "DATE_FORMAT" not found
    }

    @Test
    public void testFindByIdsInclSecurityAndUnderlying() {

        Tick tick = new TickImpl();
        tick.setDateTime(new Date());
        tick.setFeedType(FeedType.BB);
        tick.setSecurity(this.forex1);

        this.forex1.setUnderlying(this.forex2);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.family2);
        this.session.save(this.forex2);

        this.session.save(tick);

        this.session.flush();

        List<Long> ids = new ArrayList<>();
        ids.add((long) tick.getId());

        List<Tick> ticks = this.dao.findByIdsInclSecurityAndUnderlying(ids);

        Assert.assertEquals(1, ticks.size());
        Assert.assertSame(tick, ticks.get(0));
        Assert.assertSame(this.forex1, ticks.get(0).getSecurity());
        Assert.assertSame(this.forex2, ticks.get(0).getSecurity().getUnderlying());
    }

    @Test
    public void testFindSubscribedByTimePeriod() {

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 1);

        Tick tick = new TickImpl();
        tick.setDateTime(cal1.getTime());
        tick.setFeedType(FeedType.BB);
        tick.setSecurity(this.forex1);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(tick);

        this.session.flush();

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 0);

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 2);

        List<Tick> ticks1 = this.dao.findSubscribedByTimePeriod(minDate.getTime(), maxDate.getTime());

        Assert.assertEquals(0, ticks1.size());

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(subscription1);

        this.forex1.setSubscriptions(subscriptions);
        this.session.save(this.strategy1);

        this.session.save(subscription1);

        this.session.flush();

        List<Tick> ticks2 = this.dao.findSubscribedByTimePeriod(minDate.getTime(), maxDate.getTime());

        Assert.assertEquals(1, ticks2.size());
        Assert.assertEquals(1, ticks2.get(0).getSecurity().getSubscriptions().size());
        Assert.assertSame(tick, ticks2.get(0));
        Assert.assertSame(this.forex1, ticks2.get(0).getSecurity());
    }

    @Test
    public void testFindSubscribedByTimePeriodWithLimit() {

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 1);

        Tick tick1 = new TickImpl();
        tick1.setDateTime(cal1.getTime());
        tick1.setFeedType(FeedType.BB);
        tick1.setSecurity(this.forex1);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 2);

        Tick tick2 = new TickImpl();
        tick2.setDateTime(cal2.getTime());
        tick2.setFeedType(FeedType.BB);
        tick2.setSecurity(this.forex1);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(tick1);
        this.session.save(tick2);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(subscription1);

        this.forex1.setSubscriptions(subscriptions);
        this.session.save(this.strategy1);

        this.session.save(subscription1);

        this.session.flush();

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 0);

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 3);

        List<Tick> ticks1 = this.dao.findSubscribedByTimePeriod(1, minDate.getTime(), maxDate.getTime());

        Assert.assertEquals(1, ticks1.size());
        Assert.assertEquals(1, ticks1.size());
        Assert.assertEquals(1, ticks1.get(0).getSecurity().getSubscriptions().size());
        Assert.assertSame(tick1, ticks1.get(0));
        Assert.assertSame(this.forex1, ticks1.get(0).getSecurity());

        List<Tick> ticks2 = this.dao.findSubscribedByTimePeriod(2, minDate.getTime(), maxDate.getTime());

        Assert.assertEquals(2, ticks2.size());
        Assert.assertEquals(1, ticks2.get(0).getSecurity().getSubscriptions().size());
        Assert.assertSame(tick1, ticks2.get(0));
        Assert.assertSame(this.forex1, ticks2.get(0).getSecurity());

        Assert.assertSame(tick2, ticks2.get(1));
        Assert.assertSame(this.forex1, ticks2.get(1).getSecurity());
    }

    @Test
    public void testFindOptionTicksBySecurityDateTypeAndExpirationInclSecurity() {

        Calendar cal1 = Calendar.getInstance();

        Calendar expiration = Calendar.getInstance();
        expiration.set(Calendar.HOUR_OF_DAY, 0);
        expiration.set(Calendar.MINUTE, 0);
        expiration.set(Calendar.SECOND, 0);
        expiration.set(Calendar.MILLISECOND, 0);

        Option option = new OptionImpl();
        option.setExpiration(expiration.getTime());
        option.setSecurityFamily(this.family1);
        option.setType(OptionType.CALL);
        option.setStrike(new BigDecimal(111.11));
        option.setUnderlying(this.forex1);

        Tick tick = new TickImpl();
        tick.setDateTime(cal1.getTime());
        tick.setFeedType(FeedType.BB);
        tick.setSecurity(option);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(option);

        this.session.save(tick);

        this.session.flush();

        List<Tick> ticks1 = this.dao.findOptionTicksBySecurityDateTypeAndExpirationInclSecurity(0, cal1.getTime(), OptionType.CALL, option.getExpiration());
        Assert.assertEquals(0, ticks1.size());

        List<Tick> ticks2 = this.dao.findOptionTicksBySecurityDateTypeAndExpirationInclSecurity(this.forex1.getId(), cal1.getTime(), OptionType.CALL, option.getExpiration());

        Assert.assertEquals(1, ticks2.size());
        Assert.assertSame(tick, ticks2.get(0));
        Assert.assertSame(option, ticks2.get(0).getSecurity());
        Assert.assertSame(this.forex1, ticks2.get(0).getSecurity().getUnderlying());
    }

    @Test
    public void testFindImpliedVolatilityTicksBySecurityAndDate() {

        ImpliedVolatility impliedVolatility = new ImpliedVolatilityImpl();
        impliedVolatility.setSecurityFamily(this.family1);
        impliedVolatility.setDuration(Duration.DAY_1);
        impliedVolatility.setType(OptionType.CALL);
        impliedVolatility.setUnderlying(this.forex1);

        Calendar cal1 = Calendar.getInstance();

        Tick tick = new TickImpl();
        tick.setDateTime(cal1.getTime());
        tick.setFeedType(FeedType.BB);
        tick.setSecurity(impliedVolatility);

        this.session.save(this.family1);
        this.session.save(impliedVolatility);
        this.session.save(this.forex1);
        this.session.save(tick);

        this.session.flush();

        List<Tick> ticks1 = this.dao.findImpliedVolatilityTicksBySecurityAndDate(0, cal1.getTime());
        Assert.assertEquals(0, ticks1.size());

        List<Tick> ticks2 = this.dao.findImpliedVolatilityTicksBySecurityAndDate(this.forex1.getId(), cal1.getTime());

        Assert.assertEquals(1, ticks2.size());
        Assert.assertSame(tick, ticks2.get(0));
        Assert.assertSame(impliedVolatility, ticks2.get(0).getSecurity());
    }

    @Test
    public void testFindImpliedVolatilityTicksBySecurityDateAndDuration() {

        ImpliedVolatility impliedVolatility = new ImpliedVolatilityImpl();
        impliedVolatility.setSecurityFamily(this.family1);
        impliedVolatility.setDuration(Duration.DAY_1);
        impliedVolatility.setType(OptionType.CALL);
        impliedVolatility.setUnderlying(this.forex1);

        Calendar cal1 = Calendar.getInstance();

        Tick tick = new TickImpl();
        tick.setDateTime(cal1.getTime());
        tick.setFeedType(FeedType.BB);
        tick.setSecurity(impliedVolatility);

        this.session.save(this.family1);
        this.session.save(impliedVolatility);
        this.session.save(this.forex1);
        this.session.save(tick);

        this.session.flush();

        List<Tick> ticks1 = this.dao.findImpliedVolatilityTicksBySecurityDateAndDuration(this.forex1.getId(), cal1.getTime(), Duration.DAY_2);
        Assert.assertEquals(0, ticks1.size());

        List<Tick> ticks2 = this.dao.findImpliedVolatilityTicksBySecurityDateAndDuration(this.forex1.getId(), cal1.getTime(), Duration.DAY_1);

        Assert.assertEquals(1, ticks2.size());
        Assert.assertSame(tick, ticks2.get(0));
        Assert.assertSame(impliedVolatility, ticks2.get(0).getSecurity());
    }

    @Test
    public void testFindTickerIdBySecurity() {

        // Could not test the method due to EngineLocator dependency
    }

    @Test
    public void testFindCurrentTicksByStrategy() {

        // Could not test the method due to EngineLocator dependency
    }

}
