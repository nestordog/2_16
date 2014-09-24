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
package ch.algotrader.cache;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.collections15.map.SingletonMap;
import org.hibernate.proxy.HibernateProxy;
import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.LookupUtil;
import ch.algotrader.service.CombinationService;
import ch.algotrader.service.IBServiceTest;
import ch.algotrader.service.MarketDataService;
import ch.algotrader.service.PositionService;
import ch.algotrader.service.PropertyService;
import ch.algotrader.service.TransactionService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CacheTest extends IBServiceTest {

    @Test
    public void test() {

        ServiceLocator serviceLocator = ServiceLocator.instance();
        CacheManager cache = serviceLocator.getService("cacheManager", CacheManager.class);
        PositionService positionService = serviceLocator.getPositionService();
        MarketDataService marketDataService = serviceLocator.getMarketDataService();
        CombinationService combinationService = serviceLocator.getCombinationService();
        PropertyService propertyService = serviceLocator.getPropertyService();
        TransactionService transactionService = serviceLocator.getService("transactionService", TransactionService.class);

        // security lookup
        Security security1 = LookupUtil.getSecurityInitialized(10);
        Assert.assertNotNull(security1);

        // create transaction
        transactionService.createTransaction(10, "BASE", null, new Date(), 10000, new BigDecimal(1.0), null, null, null, Currency.USD, TransactionType.BUY, "IB_NATIVE_TEST", null);

        // lookup position
        Position position1 = LookupUtil.getPositionBySecurityAndStrategy(10, "BASE");
        Assert.assertNotNull(security1);

        Position position2 = cache.get(PositionImpl.class, position1.getId());
        Assert.assertNotNull(position2);
        Assert.assertEquals(position1, position2);
        Assert.assertSame(position1, position2);

        // security initialization
        Security security2 = position2.getSecurityInitialized();
        Assert.assertFalse(security2 instanceof HibernateProxy);

        // subscription
        Security security3 = cache.get(SecurityImpl.class, 8);
        Assert.assertEquals(security3.getSubscriptions().size(), 0);

        marketDataService.subscribe("BASE", 8);
        Assert.assertEquals(security3.getSubscriptions().size(), 1);

        marketDataService.unsubscribe("BASE", 8);
        Assert.assertEquals(security3.getSubscriptions().size(), 0);

        // hql query
        String queryString = "from PositionImpl as p join fetch p.security as s where s.id = :id";
        SingletonMap<String, Object> namedParameters = new SingletonMap<String, Object>("id", 10);

        Position position3 = (Position) cache.query(queryString, namedParameters).iterator().next();
        Assert.assertNotNull(position3);

        Position position4 = (Position) cache.query(queryString, namedParameters).iterator().next();
        Assert.assertNotNull(position3);
        Assert.assertEquals(position3, position4);
        Assert.assertSame(position3, position4);

        // exit Value
        Assert.assertNull(position1.getExitValue());

        BigDecimal exitValue = new BigDecimal(1);
        positionService.setExitValue(position1.getId(), exitValue, true);

        Position position5 = (Position) cache.query(queryString, namedParameters).iterator().next();
        Assert.assertTrue(position5.getExitValue().compareTo(exitValue) == 0);

        // non-tradeable position
        Security security4 = cache.get(SecurityImpl.class, 21);

        Assert.assertEquals(security4.getPositions().size(), 0);

        int positionId = positionService.createNonTradeablePosition("BASE", security4.getId(), 1000000).getId();
        Assert.assertEquals(security4.getPositions().size(), 1);

        positionService.modifyNonTradeablePosition(positionId, 2000000);
        Assert.assertEquals(security4.getPositions().iterator().next().getQuantity(), 2000000);

        positionService.deleteNonTradeablePosition(positionId, false);
        Assert.assertEquals(security4.getPositions().size(), 0);

        // combination / component modification
        int combinationId = combinationService.createCombination(CombinationType.BUTTERFLY, 42).getId();
        Combination combination1 = (Combination) cache.get(SecurityImpl.class, combinationId);
        Assert.assertNotNull(combination1);

        Combination combination2 = combinationService.addComponentQuantity(combinationId, 10, 1000);
        Assert.assertEquals(combination1.getComponentCount(), 1);
        Assert.assertEquals(combination1.getComponents().iterator().next().getQuantity(), 1000);
        Assert.assertEquals(combination2.getComponentCount(), 1);
        Assert.assertEquals(combination2.getComponents().iterator().next().getQuantity(), 1000);

        Combination combination3 = combinationService.addComponentQuantity(combinationId, 9, 5000);
        Assert.assertEquals(combination1.getComponentCount(), 2);
        Assert.assertEquals(combination1.getComponentTotalQuantity(), 6000);
        Assert.assertEquals(combination3.getComponentCount(), 2);
        Assert.assertEquals(combination3.getComponentTotalQuantity(), 6000);

        Combination combination4 = combinationService.removeComponent(combinationId, 10);
        Assert.assertEquals(combination1.getComponentCount(), 1);
        Assert.assertEquals(combination4.getComponentCount(), 1);

        combinationService.deleteCombination(combinationId);
        Combination combination5 = (Combination) cache.get(SecurityImpl.class, combinationId);
        Assert.assertNull(combination5);

        // property
        Strategy strategy = cache.get(StrategyImpl.class, 1);
        Assert.assertEquals(strategy.getProps().size(), 0);

        propertyService.addProperty(strategy.getId(), "test", 12, false);
        Assert.assertEquals(strategy.getProps().size(), 1);
        Assert.assertEquals(strategy.getIntProperty("test"), 12);

        propertyService.removeProperty(strategy.getId(), "test");
        Assert.assertEquals(strategy.getProps().size(), 0);
    }
}
