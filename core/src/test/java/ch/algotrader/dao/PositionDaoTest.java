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
package ch.algotrader.dao;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionImpl;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexImpl;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.FutureFamilyImpl;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.ExpirationType;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.hibernate.InMemoryDBTest;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimePatterns;
import ch.algotrader.util.HibernateUtil;

/**
* Unit tests for {@link ch.algotrader.entity.Position}.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*
* @version $Revision$ $Date$
*/
public class PositionDaoTest extends InMemoryDBTest {

    private PositionDao dao;

    private SecurityFamily family1;

    private Forex forex1;

    private Strategy strategy1;

    private SecurityFamily family2;

    private Forex forex2;

    private Strategy strategy2;

    private FutureFamily futurefamily1;

    public PositionDaoTest() throws IOException {

        super();
    }

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        this.dao = new PositionDaoImpl(this.sessionFactory);

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

        this.futurefamily1 = new FutureFamilyImpl();
        this.futurefamily1.setName("Future1");
        this.futurefamily1.setTickSizePattern("0<0.1");
        this.futurefamily1.setCurrency(Currency.USD);
        this.futurefamily1.setExpirationType(ExpirationType.NEXT_3_RD_FRIDAY);
        this.futurefamily1.setExpirationDistance(Duration.DAY_1);
    }

    @Test
    public void testFindByIdInclSecurityAndSecurityFamily() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        Position position2 = this.dao.findByIdInclSecurityAndSecurityFamily(1);

        Assert.assertNull(position2);

        Position position3 = this.dao.findByIdInclSecurityAndSecurityFamily(position1.getId());

        Assert.assertEquals(222, position3.getQuantity());
        Assert.assertSame(this.forex1, position3.getSecurity());
        Assert.assertSame(this.family1, position3.getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, position3.getStrategy());
    }

    @Test
    public void testFindByStrategy() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);

        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Position position2 = new PositionImpl();
        position2.setQuantity(222);
        position2.setSecurity(this.forex2);
        position2.setStrategy(this.strategy1);

        this.session.save(position2);
        this.session.flush();

        List<Position> positions1 = this.dao.findByStrategy("Dummy");

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findByStrategy("Strategy1");

        Assert.assertEquals(2, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());

        Assert.assertEquals(222, positions2.get(1).getQuantity());
        Assert.assertSame(this.forex2, positions2.get(1).getSecurity());
        Assert.assertSame(this.family2, positions2.get(1).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(1).getStrategy());
    }

    @Test
    public void testFindBySecurityAndStrategy() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        Position position2 = this.dao.findBySecurityAndStrategy(position1.getSecurity().getId(), "Dummy");

        Assert.assertNull(position2);

        Position position3 = this.dao.findBySecurityAndStrategy(position1.getSecurity().getId(), "Strategy1");

        Assert.assertNotNull(position3);

        Assert.assertEquals(222, position3.getQuantity());
        Assert.assertSame(this.forex1, position3.getSecurity());
        Assert.assertSame(this.family1, position3.getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, position3.getStrategy());
    }

    @Test
    public void testFindBySecurityAndStrategyIdLocked() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        Position position2 = this.dao.findBySecurityAndStrategyIdLocked(position1.getSecurity().getId(), 1);

        Assert.assertNull(position2);

        Position position3 = this.dao.findBySecurityAndStrategyIdLocked(position1.getSecurity().getId(), position1.getStrategy().getId());

        Assert.assertNotNull(position3);

        Assert.assertEquals(222, position3.getQuantity());
        Assert.assertSame(this.forex1, position3.getSecurity());
        Assert.assertSame(this.family1, position3.getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, position3.getStrategy());
    }

    @Test
    public void testFindOpenPositions() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(0);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositions();

        Assert.assertEquals(0, positions1.size());

        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);

        Position position2 = new PositionImpl();
        position2.setQuantity(222);
        position2.setSecurity(this.forex2);
        position2.setStrategy(this.strategy2);

        this.session.save(position2);
        this.session.flush();

        List<Position> positions2 = this.dao.findOpenPositions();

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex2, positions2.get(0).getSecurity());
        Assert.assertSame(this.family2, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy2, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenPositionsByMaxDateAggregated() {

        Transaction transaction = new TransactionImpl();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setSecurity(this.forex1);
        transaction.setQuantity(222);
        transaction.setDateTime(new Date());
        transaction.setPrice(new BigDecimal(111));
        transaction.setCurrency(Currency.NZD);
        transaction.setType(TransactionType.BUY);
        transaction.setStrategy(this.strategy1);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        this.session.save(transaction);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsByMaxDateAggregated(new Date());

        Assert.assertEquals(1, positions1.size());

        Assert.assertEquals(222, positions1.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions1.get(0).getSecurity());
        Assert.assertTrue(positions1.get(0).getSecurity() instanceof Security);
    }

    @Test
    public void testFindOpenPositionsByStrategy() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsByStrategy("Dummy");

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findOpenPositionsByStrategy("Strategy1");

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenPositionsByStrategyAndMaxDate() {

        Transaction transaction = new TransactionImpl();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setSecurity(this.forex1);
        transaction.setQuantity(222);
        transaction.setDateTime(new Date());
        transaction.setPrice(new BigDecimal(111));
        transaction.setCurrency(Currency.NZD);
        transaction.setType(TransactionType.BUY);
        transaction.setStrategy(this.strategy1);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        this.session.save(transaction);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsByStrategyAndMaxDate("Dummy", new Date());

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findOpenPositionsByStrategyAndMaxDate("Strategy1", new Date());

        Assert.assertEquals(1, positions2.size());
        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
    }

    @Test
    public void testFindOpenPositionsByStrategyAndType() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsByStrategyAndType("Dummy", HibernateUtil.getDisriminatorValue(this.sessionFactory, Security.class));

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findOpenPositionsByStrategyAndType("Strategy1", HibernateUtil.getDisriminatorValue(this.sessionFactory, Forex.class));

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenPositionsByStrategyTypeAndUnderlyingType() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsByStrategyTypeAndUnderlyingType("Dummy", HibernateUtil.getDisriminatorValue(this.sessionFactory, Security.class),
                HibernateUtil.getDisriminatorValue(this.sessionFactory, SecurityFamily.class));

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findOpenPositionsByStrategyTypeAndUnderlyingType("Strategy1", HibernateUtil.getDisriminatorValue(this.sessionFactory, Forex.class),
                HibernateUtil.getDisriminatorValue(this.sessionFactory, SecurityFamily.class));

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenPositionsByStrategyAndSecurityFamily() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsByStrategyAndSecurityFamily("Dummy", 1);
        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findOpenPositionsByStrategyAndSecurityFamily("Strategy1", this.family1.getId());
        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenPositionsByUnderlying() {

        this.session.save(this.family2);
        this.session.save(this.forex2);

        this.forex1.setUnderlying(this.forex2);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsByUnderlying(this.forex1.getId());

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findOpenPositionsByUnderlying(this.forex2.getId());

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenPositionsBySecurity() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsBySecurity(0);

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findOpenPositionsBySecurity(this.forex1.getId());

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenTradeablePositions() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenTradeablePositions();

        Assert.assertEquals(0, positions1.size());

        this.family1.setTradeable(true);
        this.session.flush();

        List<Position> positions2 = this.dao.findOpenTradeablePositions();

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenTradeablePositionsAggregated() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        this.session.save(this.family2);
        this.session.save(this.strategy2);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);

        Position position2 = new PositionImpl();
        position2.setQuantity(222);
        position2.setSecurity(this.forex1);
        position2.setStrategy(this.strategy2);

        this.session.save(position2);

        this.session.flush();

        List<Position> positions1 = this.dao.findOpenTradeablePositionsAggregated();

        Assert.assertEquals(0, positions1.size());

        this.family1.setTradeable(true);
        this.family2.setTradeable(true);

        this.session.flush();

        List<Position> positions2 = this.dao.findOpenTradeablePositionsAggregated();

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(444, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertTrue(positions2.get(0).getSecurity() instanceof Security);
    }

    @Test
    public void testFindOpenTradeablePositionsByStrategy() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenTradeablePositionsByStrategy("Dummy");

        Assert.assertEquals(0, positions1.size());

        this.family1.setTradeable(true);
        this.session.flush();

        List<Position> positions2 = this.dao.findOpenTradeablePositionsByStrategy("Strategy1");

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindOpenFXPositions() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenFXPositions();

        Assert.assertEquals(1, positions1.size());

        Assert.assertEquals(222, positions1.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions1.get(0).getSecurity());
        Assert.assertSame(this.family1, positions1.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions1.get(0).getStrategy());
    }

    @Test
    public void testFindOpenFXPositionsAggregated() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        this.session.save(this.family2);
        this.session.save(this.strategy2);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);

        Position position2 = new PositionImpl();
        position2.setQuantity(222);
        position2.setSecurity(this.forex1);
        position2.setStrategy(this.strategy2);

        this.session.save(position2);

        this.session.flush();

        List<Position> positions1 = this.dao.findOpenFXPositionsAggregated();

        Assert.assertEquals(1, positions1.size());

        Assert.assertEquals(444, positions1.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions1.get(0).getSecurity());
        Assert.assertTrue(positions1.get(0).getSecurity() instanceof Security);
    }

    @Test
    public void testFindOpenFXPositionsByStrategy() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenFXPositionsByStrategy("Dummy");

        Assert.assertEquals(0, positions1.size());

        List<Position> positions2 = this.dao.findOpenFXPositionsByStrategy("Strategy1");

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void testFindExpirablePositionsPositionExpired() {

        Date expiration = new Date(System.currentTimeMillis() - 100000000L);

        Future future = new FutureImpl();

        future.setSecurityFamily(this.futurefamily1);
        future.setExpiration(expiration);
        future.setMonthYear(DateTimePatterns.MONTH_YEAR.format(DateTimeLegacy.toLocalDate(expiration)));

        this.session.save(this.futurefamily1);
        this.session.save(future);
        this.session.save(this.strategy1);

        Position position = new PositionImpl();
        position.setQuantity(222);
        position.setSecurity(future);
        position.setStrategy(this.strategy1);

        this.session.save(position);
        this.session.flush();

        this.session.save(future);
        this.session.flush();

        List<Position> positions1 = this.dao.findExpirablePositions(new Date());

        Assert.assertEquals(1, positions1.size());

        Assert.assertEquals(222, positions1.get(0).getQuantity());
        Assert.assertSame(future, positions1.get(0).getSecurity());
        Assert.assertSame(this.futurefamily1, positions1.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions1.get(0).getStrategy());

        future.setExpiration(new Date(System.currentTimeMillis() + 100000000L));
        this.session.flush();

        List<Position> positions2 = this.dao.findExpirablePositions(new Date());
        Assert.assertEquals(0, positions2.size());

        future.setExpiration(expiration);
        position.setQuantity(0);
        this.session.flush();

        List<Position> positions3 = this.dao.findExpirablePositions(new Date());
        Assert.assertEquals(0, positions3.size());
    }

    @Test
    public void testFindPersistent() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findPersistent();

        Assert.assertEquals(0, positions1.size());

        position1.setPersistent(true);
        this.session.flush();

        List<Position> positions2 = this.dao.findPersistent();

        Assert.assertEquals(1, positions2.size());

        Assert.assertEquals(222, positions2.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertSame(this.family1, positions2.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions2.get(0).getStrategy());
    }

    @Test
    public void tesFindNonPersistent() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);
        position1.setPersistent(false);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.findNonPersistent();

        Assert.assertEquals(1, positions1.size());
        Assert.assertEquals(222, positions1.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions1.get(0).getSecurity());
        Assert.assertSame(this.family1, positions1.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions1.get(0).getStrategy());

        position1.setPersistent(true);
        this.session.flush();

        List<Position> position2 = this.dao.findNonPersistent();

        Assert.assertEquals(0, position2.size());
    }

    @Test
    public void testLoadAll() {

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);

        Position position1 = new PositionImpl();
        position1.setQuantity(222);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        this.session.save(position1);
        this.session.flush();

        List<Position> positions1 = this.dao.loadAll();

        Assert.assertEquals(1, positions1.size());
        Assert.assertEquals(222, positions1.get(0).getQuantity());
        Assert.assertSame(this.forex1, positions1.get(0).getSecurity());
        Assert.assertSame(this.family1, positions1.get(0).getSecurity().getSecurityFamily());
        Assert.assertSame(this.strategy1, positions1.get(0).getStrategy());
    }

    @Test
    public void testFindOpenPositionsAggregated() {

        Position position1 = new PositionImpl();
        position1.setQuantity(0);
        position1.setSecurity(this.forex1);
        position1.setStrategy(this.strategy1);

        Position position2 = new PositionImpl();
        position2.setQuantity(0);
        position2.setSecurity(this.forex2);
        position2.setStrategy(this.strategy2);

        this.session.save(this.family1);
        this.session.save(this.forex1);
        this.session.save(this.strategy1);
        this.session.save(this.family2);
        this.session.save(this.forex2);
        this.session.save(this.strategy2);
        this.session.save(position1);
        this.session.save(position2);
        this.session.flush();

        List<Position> positions1 = this.dao.findOpenPositionsAggregated();

        Assert.assertEquals(0, positions1.size());

        position1.setQuantity(111);
        position2.setQuantity(222);
        this.session.flush();

        List<Position> positions2 = this.dao.findOpenPositionsAggregated();

        Assert.assertEquals(2, positions2.size());
        Assert.assertSame(this.forex1, positions2.get(0).getSecurity());
        Assert.assertEquals(position1.getQuantity(), positions2.get(0).getQuantity());
        Assert.assertSame(this.forex2, positions2.get(1).getSecurity());
        Assert.assertEquals(position2.getQuantity(), positions2.get(1).getQuantity());

        position2.setSecurity(this.forex1);
        this.session.flush();

        List<Position> positions3 = this.dao.findOpenPositionsAggregated();

        Assert.assertEquals(1, positions3.size());
        Assert.assertSame(this.forex1, positions3.get(0).getSecurity());
        Assert.assertEquals((111 + 222), positions3.get(0).getQuantity());
    }

}
