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
package ch.algotrader.dao.security;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.FutureDaoImpl;
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

/**
 * Unit tests for {@link FutureDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
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

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(cal1.getTime());

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.flush();

        Future future2 = this.dao.findByExpirationInclSecurityFamily(0, cal1.getTime());

        Assert.assertNull(future2);

        Future future3 = this.dao.findByExpirationInclSecurityFamily(this.family1.getId(), new Date());

        Assert.assertNull(future3);

        Future future4 = this.dao.findByExpirationInclSecurityFamily(this.family1.getId(), cal1.getTime());

        Assert.assertNotNull(future4);

        Assert.assertSame(future1, future4);
        Assert.assertSame(this.family1, future4.getSecurityFamily());
    }

    @Test
    public void testFindByMinExpiration() {

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(cal1.getTime());

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        Future future2 = new FutureImpl();
        future2.setSecurityFamily(this.family1);
        future2.setExpiration(cal2.getTime());

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.save(future2);
        this.session.flush();

        Calendar cal3 = Calendar.getInstance();
        cal3.add(Calendar.DAY_OF_MONTH, 2);

        List<Future> futures1 = this.dao.findByMinExpiration(this.family1.getId(), cal3.getTime());

        Assert.assertEquals(0, futures1.size());

        List<Future> futures2 = this.dao.findByMinExpiration(this.family1.getId(), cal2.getTime());

        Assert.assertEquals(1, futures2.size());

        Assert.assertSame(future2, futures2.get(0));
        Assert.assertSame(this.family1, futures2.get(0).getSecurityFamily());

        List<Future> futures3 = this.dao.findByMinExpiration(this.family1.getId(), cal1.getTime());

        Assert.assertEquals(2, futures3.size());

        Assert.assertSame(future1, futures3.get(0));
        Assert.assertSame(this.family1, futures3.get(0).getSecurityFamily());
        Assert.assertSame(future2, futures3.get(1));
        Assert.assertSame(this.family1, futures3.get(1).getSecurityFamily());
    }

    @Test
    public void testFindByMinExpirationByLimit() {

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(cal1.getTime());

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        Future future2 = new FutureImpl();
        future2.setSecurityFamily(this.family1);
        future2.setExpiration(cal2.getTime());

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.save(future2);
        this.session.flush();

        List<Future> futures3 = this.dao.findByMinExpiration(1, this.family1.getId(), cal1.getTime());

        Assert.assertEquals(1, futures3.size());

        Assert.assertSame(future1, futures3.get(0));
        Assert.assertSame(this.family1, futures3.get(0).getSecurityFamily());

        List<Future> futures4 = this.dao.findByMinExpiration(2, this.family1.getId(), cal1.getTime());

        Assert.assertEquals(2, futures4.size());

        Assert.assertSame(future1, futures4.get(0));
        Assert.assertSame(this.family1, futures4.get(0).getSecurityFamily());
        Assert.assertSame(future2, futures4.get(1));
        Assert.assertSame(this.family1, futures4.get(1).getSecurityFamily());
    }

    @Test
    public void testFindSubscribedFutures() {

        Calendar cal1 = Calendar.getInstance();

        Future future1 = new FutureImpl();
        future1.setSecurityFamily(this.family1);
        future1.setExpiration(cal1.getTime());

        Calendar cal2 = Calendar.getInstance();

        Future future2 = new FutureImpl();
        future2.setSecurityFamily(this.family1);
        future2.setExpiration(cal2.getTime());

        this.session.save(this.family1);
        this.session.save(future1);
        this.session.save(future2);
        this.session.flush();

        List<Future> futures1 = this.dao.findSubscribedFutures();

        Assert.assertEquals(0, futures1.size());

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
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
        subscription2.setFeedType(FeedType.BB);
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

        Future future = new FutureImpl();
        future.setSecurityFamily(this.family1);
        future.setExpiration(new Date());

        this.session.save(this.family1);
        this.session.save(future);
        this.session.flush();

        List<Future> futures1 = this.dao.findBySecurityFamily(0);

        Assert.assertEquals(0, futures1.size());

        List<Future> futures2 = this.dao.findBySecurityFamily(this.family1.getId());

        Assert.assertEquals(1, futures2.size());

        Assert.assertSame(future, futures2.get(0));
        Assert.assertSame(this.family1, futures2.get(0).getSecurityFamily());
    }

    @Test
    public void testFindByExpirationMonth() {

        // Could not execute query in test environment
        // Caused by: org.h2.jdbc.JdbcSQLException: Function "last_day" not found
    }

}
