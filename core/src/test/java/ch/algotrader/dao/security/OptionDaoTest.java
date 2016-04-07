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
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionImpl;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionImpl;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.FeedType;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.hibernate.InMemoryDBTest;

/**
 * Unit tests for {@link OptionDaoImpl}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class OptionDaoTest extends InMemoryDBTest {

    private OptionDao dao;

    private SecurityFamily family1;

    private SecurityFamily family2;

    private Forex forex1;

    private Strategy strategy1;

    public OptionDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {

        super.setup();

        this.dao = new OptionDaoImpl(this.sessionFactory);

        this.family1 = new SecurityFamilyImpl();
        this.family1.setName("Family1");
        this.family1.setTickSizePattern("0<0.1");
        this.family1.setCurrency(Currency.USD);

        this.family2 = new SecurityFamilyImpl();
        this.family2.setName("Family2");
        this.family2.setTickSizePattern("0<0.1");
        this.family2.setCurrency(Currency.INR);

        this.forex1 = new ForexImpl();
        this.forex1.setSymbol("EUR.USD");
        this.forex1.setBaseCurrency(Currency.EUR);
        this.forex1.setSecurityFamily(this.family1);

        this.strategy1 = new StrategyImpl();
        this.strategy1.setName("Strategy1");
    }

    @Test
    public void testFindByMinExpirationAndMinStrikeDistance() {

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Option option1 = new OptionImpl();
        option1.setSecurityFamily(this.family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setOptionType(OptionType.CALL);
        option1.setUnderlying(this.forex1);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        Option option2 = new OptionImpl();
        option2.setSecurityFamily(this.family2);
        option2.setExpiration(cal2.getTime());
        option2.setStrike(new BigDecimal(111));
        option2.setOptionType(OptionType.CALL);
        option2.setUnderlying(this.forex1);

        this.session.save(this.family1);
        this.session.save(this.family2);
        this.session.save(this.forex1);
        this.session.save(option1);
        this.session.save(option2);
        this.session.flush();

        List<Option> options1 = this.dao.findByMinExpirationAndMinStrikeDistance(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.PUT);

        Assert.assertEquals(0, options1.size());

        List<Option> options2 = this.dao.findByMinExpirationAndMinStrikeDistance(1, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL);

        Assert.assertEquals(1, options2.size());

        Assert.assertSame(option1, options2.get(0));

        List<Option> options3 = this.dao.findByMinExpirationAndMinStrikeDistance(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL);

        Assert.assertEquals(2, options3.size());

        Assert.assertSame(this.family1, options3.get(0).getSecurityFamily());
        Assert.assertSame(option1, options3.get(0));
        Assert.assertSame(this.family2, options3.get(1).getSecurityFamily());
        Assert.assertSame(option2, options3.get(1));
    }

    @Test
    public void testFindByMinExpirationAndStrikeLimit() {

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Option option1 = new OptionImpl();
        option1.setSecurityFamily(this.family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setOptionType(OptionType.CALL);
        option1.setUnderlying(this.forex1);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        Option option2 = new OptionImpl();
        option2.setSecurityFamily(this.family2);
        option2.setExpiration(cal2.getTime());
        option2.setStrike(new BigDecimal(111));
        option2.setOptionType(OptionType.CALL);
        option2.setUnderlying(this.forex1);

        this.session.save(this.family1);
        this.session.save(this.family2);
        this.session.save(this.forex1);
        this.session.save(option1);
        this.session.save(option2);
        this.session.flush();

        List<Option> options1 = this.dao.findByMinExpirationAndStrikeLimit(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.PUT);

        Assert.assertEquals(0, options1.size());

        List<Option> options2 = this.dao.findByMinExpirationAndStrikeLimit(1, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL);

        Assert.assertEquals(1, options2.size());

        Assert.assertSame(option1, options2.get(0));

        List<Option> options3 = this.dao.findByMinExpirationAndStrikeLimit(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL);

        Assert.assertEquals(2, options3.size());

        Assert.assertSame(this.family1, options3.get(0).getSecurityFamily());
        Assert.assertSame(option1, options3.get(0));
        Assert.assertSame(this.family2, options3.get(1).getSecurityFamily());
        Assert.assertSame(option2, options3.get(1));
    }

    @Test
    public void testFindByMinExpirationAndMinStrikeDistanceWithTicks() {

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Option option1 = new OptionImpl();
        option1.setSecurityFamily(this.family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setOptionType(OptionType.CALL);
        option1.setUnderlying(this.forex1);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        Option option2 = new OptionImpl();
        option2.setSecurityFamily(this.family2);
        option2.setExpiration(cal2.getTime());
        option2.setStrike(new BigDecimal(111));
        option2.setOptionType(OptionType.CALL);
        option2.setUnderlying(this.forex1);

        Tick tick1 = new TickImpl();
        tick1.setFeedType(FeedType.BB.name());
        tick1.setSecurity(this.forex1);
        tick1.setDateTime(cal2.getTime());

        Tick tick2 = new TickImpl();
        tick2.setFeedType(FeedType.DC.name());
        tick2.setSecurity(this.forex1);
        tick2.setDateTime(cal2.getTime());

        this.session.save(this.family1);
        this.session.save(this.family2);
        this.session.save(this.forex1);
        this.session.save(option1);
        this.session.save(option2);
        this.session.save(tick1);
        this.session.save(tick2);

        this.session.flush();

        List<Option> options1 = this.dao.findByMinExpirationAndMinStrikeDistanceWithTicks(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL, cal2.getTime());

        Assert.assertEquals(0, options1.size());

        tick1.setSecurity(option1);

        this.session.flush();

        List<Option> options2 = this.dao.findByMinExpirationAndMinStrikeDistanceWithTicks(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL, cal2.getTime());

        Assert.assertEquals(1, options2.size());
        Assert.assertSame(this.family1, options2.get(0).getSecurityFamily());
        Assert.assertSame(option1, options2.get(0));

        tick2.setSecurity(option2);

        this.session.flush();

        List<Option> options3 = this.dao.findByMinExpirationAndMinStrikeDistanceWithTicks(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL, cal2.getTime());

        Assert.assertEquals(2, options3.size());

        Assert.assertSame(this.family1, options3.get(0).getSecurityFamily());
        Assert.assertSame(option1, options3.get(0));
        Assert.assertSame(this.family2, options3.get(1).getSecurityFamily());
        Assert.assertSame(option2, options3.get(1));
    }

    @Test
    public void testFindByMinExpirationAndStrikeLimitWithTicks() {

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Option option1 = new OptionImpl();
        option1.setSecurityFamily(this.family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setOptionType(OptionType.CALL);
        option1.setUnderlying(this.forex1);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);
        cal2.set(Calendar.HOUR_OF_DAY, 0);
        cal2.set(Calendar.MINUTE, 0);
        cal2.set(Calendar.SECOND, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        Option option2 = new OptionImpl();
        option2.setSecurityFamily(this.family2);
        option2.setExpiration(cal2.getTime());
        option2.setStrike(new BigDecimal(111));
        option2.setOptionType(OptionType.CALL);
        option2.setUnderlying(this.forex1);

        Tick tick1 = new TickImpl();
        tick1.setFeedType(FeedType.BB.name());
        tick1.setSecurity(this.forex1);
        tick1.setDateTime(cal2.getTime());

        Tick tick2 = new TickImpl();
        tick2.setFeedType(FeedType.DC.name());
        tick2.setSecurity(this.forex1);
        tick2.setDateTime(cal2.getTime());

        this.session.save(this.family1);
        this.session.save(this.family2);
        this.session.save(this.forex1);
        this.session.save(option1);
        this.session.save(option2);
        this.session.save(tick1);
        this.session.save(tick2);
        this.session.flush();

        List<Option> options1 = this.dao.findByMinExpirationAndStrikeLimitWithTicks(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL, cal2.getTime());

        Assert.assertEquals(0, options1.size());

        tick1.setSecurity(option1);

        this.session.flush();

        List<Option> options2 = this.dao.findByMinExpirationAndStrikeLimitWithTicks(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL, cal2.getTime());

        Assert.assertEquals(1, options2.size());
        Assert.assertSame(this.family1, options2.get(0).getSecurityFamily());
        Assert.assertSame(option1, options2.get(0));

        tick2.setSecurity(option2);

        this.session.flush();

        List<Option> options3 = this.dao.findByMinExpirationAndStrikeLimitWithTicks(2, this.forex1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL, cal2.getTime());

        Assert.assertEquals(2, options3.size());

        Assert.assertSame(this.family1, options3.get(0).getSecurityFamily());
        Assert.assertSame(option1, options3.get(0));
        Assert.assertSame(this.family2, options3.get(1).getSecurityFamily());
        Assert.assertSame(option2, options3.get(1));
    }

    @Test
    public void testFindSubscribedOptions() {

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 1);

        Option option1 = new OptionImpl();
        option1.setSecurityFamily(this.family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setOptionType(OptionType.CALL);
        option1.setUnderlying(this.forex1);

        Subscription subscription1 = new SubscriptionImpl();
        subscription1.setFeedType(FeedType.SIM.name());
        subscription1.setSecurity(option1);
        subscription1.setStrategy(this.strategy1);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);
        this.session.save(option1);
        this.session.save(subscription1);

        this.session.flush();

        List<Option> options1 = this.dao.findSubscribedOptions();

        Assert.assertEquals(1, options1.size());

        Assert.assertSame(option1, options1.get(0));
    }

    @Test
    public void testFindBySecurityFamily() {

        Calendar cal1 = Calendar.getInstance();
        cal1.add(Calendar.DAY_OF_MONTH, 1);

        Option option1 = new OptionImpl();
        option1.setSecurityFamily(this.family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setOptionType(OptionType.CALL);
        option1.setUnderlying(this.forex1);

        Option option2 = new OptionImpl();
        option2.setSecurityFamily(this.family2);
        option2.setExpiration(cal1.getTime());
        option2.setStrike(new BigDecimal(222));
        option2.setOptionType(OptionType.CALL);
        option2.setUnderlying(this.forex1);

        this.session.save(this.family1);
        this.session.save(this.family2);
        this.session.save(this.forex1);
        this.session.save(option1);
        this.session.save(option2);
        this.session.flush();

        List<Option> options1 = this.dao.findBySecurityFamily(0);

        Assert.assertEquals(0, options1.size());

        List<Option> options2 = this.dao.findBySecurityFamily(this.family1.getId());

        Assert.assertEquals(1, options2.size());

        Assert.assertSame(this.family1, options2.get(0).getSecurityFamily());
        Assert.assertSame(option1, options2.get(0));

        option2.setSecurityFamily(this.family1);

        this.session.flush();

        List<Option> options3 = this.dao.findBySecurityFamily(this.family1.getId());

        Assert.assertEquals(2, options3.size());

        Assert.assertSame(this.family1, options3.get(0).getSecurityFamily());
        Assert.assertSame(option1, options3.get(0));
        Assert.assertSame(this.family1, options3.get(1).getSecurityFamily());
        Assert.assertSame(option2, options3.get(1));
    }

    @Test
    public void testFindExpirationsByUnderlyingAndDate() {

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Option option1 = new OptionImpl();
        option1.setSecurityFamily(this.family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setOptionType(OptionType.CALL);
        option1.setUnderlying(this.forex1);

        Calendar cal2 = Calendar.getInstance();

        Tick tick = new TickImpl();
        tick.setDateTime(cal2.getTime());
        tick.setFeedType(FeedType.BB.name());
        tick.setSecurity(option1);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(option1);
        this.session.save(tick);
        this.session.flush();

        List<Date> dates1 = this.dao.findExpirationsByUnderlyingAndDate(0, cal2.getTime());

        Assert.assertEquals(0, dates1.size());

        Calendar cal3 = Calendar.getInstance();
        cal3.add(Calendar.DAY_OF_MONTH, 1);

        List<Date> dates2 = this.dao.findExpirationsByUnderlyingAndDate(this.forex1.getId(), cal3.getTime());

        Assert.assertEquals(0, dates2.size());

        List<Date> dates3 = this.dao.findExpirationsByUnderlyingAndDate(this.forex1.getId(), cal2.getTime());

        Assert.assertEquals(1, dates3.size());
    }

    @Test
    public void testFindByExpirationStrikeAndType() {

        Calendar cal1 = Calendar.getInstance();
        cal1.set(Calendar.HOUR_OF_DAY, 0);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND, 0);
        cal1.set(Calendar.MILLISECOND, 0);

        Option option1 = new OptionImpl();
        option1.setSecurityFamily(this.family1);
        option1.setExpiration(cal1.getTime());
        option1.setStrike(new BigDecimal(111));
        option1.setOptionType(OptionType.CALL);
        option1.setUnderlying(this.forex1);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(option1);
        this.session.flush();

        Option option2 = this.dao.findByExpirationStrikeAndType(0, cal1.getTime(), new BigDecimal(111), OptionType.CALL);

        Assert.assertNull(option2);

        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_MONTH, 1);

        Option option3 = this.dao.findByExpirationStrikeAndType(this.family1.getId(), cal2.getTime(), new BigDecimal(111), OptionType.CALL);

        Assert.assertNull(option3);

        Option option4 = this.dao.findByExpirationStrikeAndType(this.family1.getId(), cal1.getTime(), new BigDecimal(222), OptionType.CALL);

        Assert.assertNull(option4);

        Option option5 = this.dao.findByExpirationStrikeAndType(this.family1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.PUT);

        Assert.assertNull(option5);

        Option option6 = this.dao.findByExpirationStrikeAndType(this.family1.getId(), cal1.getTime(), new BigDecimal(111), OptionType.CALL);

        Assert.assertNotNull(option6);

        Assert.assertSame(option1, option6);
    }

}
