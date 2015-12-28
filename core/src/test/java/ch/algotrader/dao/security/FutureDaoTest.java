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
package ch.algotrader.dao.security;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.hibernate.InMemoryDBTest;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimePatterns;

/**
 * Unit tests for {@link FutureDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FutureDaoTest extends InMemoryDBTest {

    private FutureDao dao;

    private SecurityFamily family1;

    private Strategy strategy1;

    public FutureDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new FutureDaoImpl(this.sessionFactory);

        this.family1 = new SecurityFamilyImpl();
        this.family1.setName("Forex1");
        this.family1.setTickSizePattern("0<0.1");
        this.family1.setCurrency(Currency.USD);

        this.strategy1 = new StrategyImpl();
        this.strategy1.setName("Strategy1");
    }

    @Test
    public void testFindByExpirationInclSecurityFamily() {

        LocalDate today = LocalDate.now();

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(DateTimeLegacy.toLocalDate(today));
        future1.setMonthYear(DateTimePatterns.MONTH_YEAR.format(today));

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.flush();

        Future future2 = this.dao.findByExpirationInclSecurityFamily(0, DateTimeLegacy.toLocalDate(today));

        Assert.assertNull(future2);

        Future future3 = this.dao.findByExpirationInclSecurityFamily(this.family1.getId(), new Date());

        Assert.assertNull(future3);

        Future future4 = this.dao.findByExpirationInclSecurityFamily(this.family1.getId(), DateTimeLegacy.toLocalDate(today));

        Assert.assertNotNull(future4);

        Assert.assertSame(future1, future4);
        Assert.assertSame(this.family1, future4.getSecurityFamily());
    }

    @Test
    public void testFindByMinExpiration() {

        LocalDate today = LocalDate.now();

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(DateTimeLegacy.toLocalDate(today));
        future1.setMonthYear(DateTimePatterns.MONTH_YEAR.format(today));

        LocalDate nextMonth = today.plusMonths(1);

        Future future2 = new FutureImpl();
        future2.setSecurityFamily(this.family1);
        future2.setExpiration(DateTimeLegacy.toLocalDate(nextMonth));
        future2.setMonthYear(DateTimePatterns.MONTH_YEAR.format(nextMonth));

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.save(future2);
        this.session.flush();

        LocalDate afterNextMonth = nextMonth.plusMonths(1);

        List<Future> futures1 = this.dao.findByMinExpiration(this.family1.getId(), DateTimeLegacy.toLocalDate(afterNextMonth));

        Assert.assertEquals(0, futures1.size());

        List<Future> futures2 = this.dao.findByMinExpiration(this.family1.getId(), DateTimeLegacy.toLocalDate(nextMonth));

        Assert.assertEquals(1, futures2.size());

        Assert.assertSame(future2, futures2.get(0));
        Assert.assertSame(this.family1, futures2.get(0).getSecurityFamily());

        List<Future> futures3 = this.dao.findByMinExpiration(this.family1.getId(), DateTimeLegacy.toLocalDate(today));

        Assert.assertEquals(2, futures3.size());

        Assert.assertSame(future1, futures3.get(0));
        Assert.assertSame(this.family1, futures3.get(0).getSecurityFamily());
        Assert.assertSame(future2, futures3.get(1));
        Assert.assertSame(this.family1, futures3.get(1).getSecurityFamily());
    }

    @Test
    public void testFindByMinExpirationByLimit() {

        LocalDate today = LocalDate.now();

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(DateTimeLegacy.toLocalDate(today));
        future1.setMonthYear(DateTimePatterns.MONTH_YEAR.format(today));

        LocalDate nextMonth = today.plusMonths(1);

        Future future2 = new FutureImpl();
        future2.setSecurityFamily(this.family1);
        future2.setExpiration(DateTimeLegacy.toLocalDate(nextMonth));
        future2.setMonthYear(DateTimePatterns.MONTH_YEAR.format(nextMonth));

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.save(future2);
        this.session.flush();

        List<Future> futures3 = this.dao.findByMinExpiration(1, this.family1.getId(), DateTimeLegacy.toLocalDate(today));

        Assert.assertEquals(1, futures3.size());

        Assert.assertSame(future1, futures3.get(0));
        Assert.assertSame(this.family1, futures3.get(0).getSecurityFamily());

        List<Future> futures4 = this.dao.findByMinExpiration(2, this.family1.getId(), DateTimeLegacy.toLocalDate(today));

        Assert.assertEquals(2, futures4.size());

        Assert.assertSame(future1, futures4.get(0));
        Assert.assertSame(this.family1, futures4.get(0).getSecurityFamily());
        Assert.assertSame(future2, futures4.get(1));
        Assert.assertSame(this.family1, futures4.get(1).getSecurityFamily());
    }

    @Test
    public void testFindByMonthYear() {

        LocalDate expiration = LocalDate.of(2015, Month.JANUARY, 1);

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(DateTimeLegacy.toLocalDate(expiration));
        future1.setMonthYear(DateTimePatterns.MONTH_YEAR.format(expiration));

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.flush();

        Future future3 = this.dao.findByMonthYear(this.family1.getId(), 2015, 1);

        Assert.assertNotNull(future3);

        Assert.assertSame(future1, future3);

        Future future4 = this.dao.findByMonthYear(this.family1.getId(), 2015, 2);

        Assert.assertNull(future4);
    }

    @Test
    public void testFindSubscribedFutures() {

        LocalDate today = LocalDate.now();

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(DateTimeLegacy.toLocalDate(today));
        future1.setMonthYear(DateTimePatterns.MONTH_YEAR.format(today));

        LocalDate nextMonth = today.plusMonths(1);

        Future future2 = new FutureImpl();
        future2.setSecurityFamily(this.family1);
        future2.setExpiration(DateTimeLegacy.toLocalDate(nextMonth));
        future2.setMonthYear(DateTimePatterns.MONTH_YEAR.format(nextMonth));

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.save(future2);
        this.session.flush();

        List<Future> futures1 = this.dao.findSubscribedFutures();

        Assert.assertEquals(0, futures1.size());

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(future1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.strategy1);
        this.session.save(subscription1);

        future1.addSubscriptions(subscription1);

        this.session.flush();

        List<Future> futures2 = this.dao.findSubscribedFutures();

        Assert.assertEquals(1, futures2.size());

        Assert.assertSame(future1, futures2.get(0));
        Assert.assertSame(this.family1, futures2.get(0).getSecurityFamily());
        Assert.assertSame(1, futures2.get(0).getSubscriptions().size());

        Subscription subscription2 = new SubscriptionImpl();
        subscription2.setFeedType(FeedType.BB.name());
        subscription2.setSecurity(future2);
        subscription2.setStrategy(this.strategy1);

        this.session.save(subscription2);

        future2.addSubscriptions(subscription2);

        this.session.flush();

        List<Future> futures3 = this.dao.findSubscribedFutures();

        Assert.assertEquals(2, futures3.size());

        Assert.assertSame(future1, futures3.get(0));
        Assert.assertSame(this.family1, futures3.get(0).getSecurityFamily());
        Assert.assertSame(1, futures3.get(0).getSubscriptions().size());
        Assert.assertSame(future2, futures3.get(1));
        Assert.assertSame(this.family1, futures3.get(1).getSecurityFamily());
        Assert.assertSame(1, futures3.get(1).getSubscriptions().size());
    }

    @Test
    public void testFindBySecurityFamily() {

        LocalDate today = LocalDate.now();

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(DateTimeLegacy.toLocalDate(today));
        future1.setMonthYear(DateTimePatterns.MONTH_YEAR.format(today));

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.flush();

        List<Future> futures1 = this.dao.findBySecurityFamily(0);

        Assert.assertEquals(0, futures1.size());

        List<Future> futures2 = this.dao.findBySecurityFamily(this.family1.getId());

        Assert.assertEquals(1, futures2.size());

        Assert.assertSame(future1, futures2.get(0));
        Assert.assertSame(this.family1, futures2.get(0).getSecurityFamily());
    }

}
