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

package ch.algotrader.dao.marketData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.marketData.BarDaoImpl;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.BarImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
* Unit tests for {@link ch.algotrader.entity.marketData.Bar}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class BarDaoTest extends InMemoryDBTest {

    private BarDao dao;

    private SecurityFamily family1;

    private Forex forex1;

    private Strategy strategy1;

    public BarDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new BarDaoImpl(this.sessionFactory);

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
    }

    @Test
    public void testFindBarsBySecurityAndBarSize() {

        this.session.save(this.family1);
        this.session.save(this.forex1);

        Bar bar1 = new BarImpl();
        bar1.setDateTime(new Date());
        bar1.setBarSize(Duration.MIN_1);
        bar1.setOpen(new BigDecimal(222));
        bar1.setHigh(new BigDecimal(333));
        bar1.setLow(new BigDecimal(111));
        bar1.setClose(new BigDecimal(444));
        bar1.setFeedType(FeedType.CNX);
        bar1.setSecurity(this.forex1);

        Bar bar2 = new BarImpl();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -2);

        bar2.setDateTime(cal.getTime());
        bar2.setBarSize(Duration.MIN_1);
        bar2.setOpen(new BigDecimal(555));
        bar2.setHigh(new BigDecimal(666));
        bar2.setLow(new BigDecimal(777));
        bar2.setClose(new BigDecimal(888));
        bar2.setFeedType(FeedType.BB);
        bar2.setSecurity(this.forex1);

        this.session.save(bar1);
        this.session.save(bar2);
        this.session.flush();

        int limit1 = 1;
        List<Bar> bars1 = this.dao.findBarsBySecurityAndBarSize(limit1, this.forex1.getId(), Duration.MIN_1);

        Assert.assertEquals(1, bars1.size());
        Assert.assertSame(bar1, bars1.get(0));

        int limit2 = 2;
        List<Bar> bars2 = this.dao.findBarsBySecurityAndBarSize(limit2, this.forex1.getId(), Duration.MIN_1);

        Assert.assertEquals(2, bars2.size());
        Assert.assertSame(bar1, bars2.get(0));
        Assert.assertSame(bar2, bars2.get(1));
    }

    @Test
    public void testFindBarsBySecurityBarSizeAndMinDate() {

        Bar bar1 = new BarImpl();
        bar1.setDateTime(new Date());
        bar1.setBarSize(Duration.MIN_1);
        bar1.setOpen(new BigDecimal(222));
        bar1.setHigh(new BigDecimal(333));
        bar1.setLow(new BigDecimal(111));
        bar1.setClose(new BigDecimal(444));
        bar1.setFeedType(FeedType.CNX);
        bar1.setSecurity(this.forex1);

        Bar bar2 = new BarImpl();

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, -2);

        bar2.setDateTime(cal1.getTime());
        bar2.setBarSize(Duration.MIN_1);
        bar2.setOpen(new BigDecimal(555));
        bar2.setHigh(new BigDecimal(666));
        bar2.setLow(new BigDecimal(777));
        bar2.setClose(new BigDecimal(888));
        bar2.setFeedType(FeedType.BB);
        bar2.setSecurity(this.forex1);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(bar1);
        this.session.save(bar2);
        this.session.flush();

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, -1);

        List<Bar> bars1 = this.dao.findBarsBySecurityBarSizeAndMinDate(this.forex1.getId(), Duration.MIN_1, cal2.getTime());

        Assert.assertEquals(1, bars1.size());
        Assert.assertSame(bar1, bars1.get(0));

        Calendar cal3 = Calendar.getInstance();
        cal3.add(Calendar.DAY_OF_MONTH, -3);

        List<Bar> bars2 = this.dao.findBarsBySecurityBarSizeAndMinDate(this.forex1.getId(), Duration.MIN_1, cal3.getTime());

        Assert.assertEquals(2, bars2.size());
        Assert.assertSame(bar1, bars2.get(0));
        Assert.assertSame(bar2, bars2.get(1));
    }

    @Test
    public void testFindSubscribedByTimePeriodAndBarSize() {

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        Bar bar = new BarImpl();
        bar.setDateTime(new Date());
        bar.setBarSize(Duration.MIN_1);
        bar.setOpen(new BigDecimal(222));
        bar.setHigh(new BigDecimal(333));
        bar.setLow(new BigDecimal(111));
        bar.setClose(new BigDecimal(444));
        bar.setFeedType(FeedType.CNX);
        bar.setSecurity(this.forex1);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(subscription1);

        this.forex1.addSubscriptions(subscription1);

        this.session.save(bar);
        this.session.flush();

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, -1);

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 1);

        List<Bar> bars = this.dao.findSubscribedByTimePeriodAndBarSize(minDate.getTime(), maxDate.getTime(), Duration.MIN_1);

        Assert.assertEquals(1, bars.size());
        Assert.assertSame(bar, bars.get(0));
    }

    @Test
    public void testFindSubscribedByTimePeriodAndBarSizeWithLimit() {

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM);
        subscription1.setSecurity(this.forex1);
        subscription1.setStrategy(this.strategy1);

        Bar bar = new BarImpl();
        bar.setDateTime(new Date());
        bar.setBarSize(Duration.MIN_1);
        bar.setOpen(new BigDecimal(222));
        bar.setHigh(new BigDecimal(333));
        bar.setLow(new BigDecimal(111));
        bar.setClose(new BigDecimal(444));
        bar.setFeedType(FeedType.CNX);
        bar.setSecurity(this.forex1);

        Set<Subscription> subscriptions = new HashSet<>();
        subscriptions.add(subscription1);

        this.forex1.setSubscriptions(subscriptions);

        this.session.save(this.strategy1);
        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(subscription1);

        this.session.save(bar);
        this.session.flush();

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, -1);

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 1);

        int limit1 = 1;
        List<Bar> bars1 = this.dao.findSubscribedByTimePeriodAndBarSize(limit1, minDate.getTime(), maxDate.getTime(), Duration.MIN_1);

        Assert.assertEquals(1, bars1.size());

        int limit2 = 2;
        List<Bar> bars2 = this.dao.findSubscribedByTimePeriodAndBarSize(limit2, minDate.getTime(), maxDate.getTime(), Duration.MIN_1);

        Assert.assertEquals(1, bars2.size());
        Assert.assertSame(bar, bars2.get(0));
    }

}
