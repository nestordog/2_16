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
package ch.algotrader.wiring.daos;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.PositionDao;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.TransactionDao;
import ch.algotrader.dao.exchange.ExchangeDao;
import ch.algotrader.dao.exchange.HolidayDao;
import ch.algotrader.dao.exchange.TradingHoursDao;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.marketData.GenericTickDao;
import ch.algotrader.dao.marketData.MarketDataEventDao;
import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.dao.property.PropertyDao;
import ch.algotrader.dao.property.PropertyHolderDao;
import ch.algotrader.dao.security.BondDao;
import ch.algotrader.dao.security.BondFamilyDao;
import ch.algotrader.dao.security.BrokerParametersDao;
import ch.algotrader.dao.security.CombinationDao;
import ch.algotrader.dao.security.CommodityDao;
import ch.algotrader.dao.security.ComponentDao;
import ch.algotrader.dao.security.EasyToBorrowDao;
import ch.algotrader.dao.security.ForexDao;
import ch.algotrader.dao.security.FundDao;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.FutureFamilyDao;
import ch.algotrader.dao.security.GenericFutureDao;
import ch.algotrader.dao.security.GenericFutureFamilyDao;
import ch.algotrader.dao.security.IndexDao;
import ch.algotrader.dao.security.IntrestRateDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.OptionFamilyDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.security.SecurityFamilyDao;
import ch.algotrader.dao.security.StockDao;
import ch.algotrader.dao.strategy.CashBalanceDao;
import ch.algotrader.dao.strategy.MeasurementDao;
import ch.algotrader.dao.strategy.PortfolioValueDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.trade.AllocationDao;
import ch.algotrader.dao.trade.LimitOrderDao;
import ch.algotrader.dao.trade.MarketOrderDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderPreferenceDao;
import ch.algotrader.dao.trade.OrderPropertyDao;
import ch.algotrader.dao.trade.OrderStatusDao;
import ch.algotrader.dao.trade.SimpleOrderDao;
import ch.algotrader.dao.trade.StopLimitOrderDao;
import ch.algotrader.dao.trade.StopOrderDao;
import ch.algotrader.wiring.HibernateNoCachingWiring;
import ch.algotrader.wiring.common.CommonConfigWiring;
import ch.algotrader.wiring.server.DaoWiring;
import ch.algotrader.wiring.server.ServerEngineWiring;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class DaoWiringTest {

    @Test
    public void testDaosWiring() {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        EmbeddedDatabaseFactory dbFactory = new EmbeddedDatabaseFactory();
        dbFactory.setDatabaseType(EmbeddedDatabaseType.H2);
        dbFactory.setDatabaseName("testdb;MODE=MYSQL;DATABASE_TO_UPPER=FALSE");

        EmbeddedDatabase database = dbFactory.getDatabase();
        context.getDefaultListableBeanFactory().registerSingleton("dataSource", database);

        context.register(CommonConfigWiring.class, ServerEngineWiring.class, HibernateNoCachingWiring.class, DaoWiring.class);
        context.refresh();

        Assert.assertNotNull(context.getBean(AccountDao.class));
        Assert.assertNotNull(context.getBean(PositionDao.class));
        Assert.assertNotNull(context.getBean(SubscriptionDao.class));
        Assert.assertNotNull(context.getBean(TransactionDao.class));
        Assert.assertNotNull(context.getBean(ExchangeDao.class));
        Assert.assertNotNull(context.getBean(HolidayDao.class));
        Assert.assertNotNull(context.getBean(TradingHoursDao.class));
        Assert.assertNotNull(context.getBean(BarDao.class));
        Assert.assertNotNull(context.getBean(GenericTickDao.class));
        Assert.assertNotNull(context.getBean(MarketDataEventDao.class));
        Assert.assertNotNull(context.getBean(TickDao.class));
        Assert.assertNotNull(context.getBean(PropertyDao.class));
        Assert.assertNotNull(context.getBean(PropertyHolderDao.class));
        Assert.assertNotNull(context.getBean(BondDao.class));
        Assert.assertNotNull(context.getBean(BondFamilyDao.class));
        Assert.assertNotNull(context.getBean(BrokerParametersDao.class));
        Assert.assertNotNull(context.getBean(CombinationDao.class));
        Assert.assertNotNull(context.getBean(CommodityDao.class));
        Assert.assertNotNull(context.getBean(ComponentDao.class));
        Assert.assertNotNull(context.getBean(EasyToBorrowDao.class));
        Assert.assertNotNull(context.getBean(ForexDao.class));
        Assert.assertNotNull(context.getBean(FundDao.class));
        Assert.assertNotNull(context.getBean(FutureDao.class));
        Assert.assertNotNull(context.getBean(FutureFamilyDao.class));
        Assert.assertNotNull(context.getBean(GenericFutureDao.class));
        Assert.assertNotNull(context.getBean(GenericFutureFamilyDao.class));
        Assert.assertNotNull(context.getBean(IndexDao.class));
        Assert.assertNotNull(context.getBean(IntrestRateDao.class));
        Assert.assertNotNull(context.getBean(OptionDao.class));
        Assert.assertNotNull(context.getBean(OptionFamilyDao.class));
        Assert.assertNotNull(context.getBean(SecurityDao.class));
        Assert.assertNotNull(context.getBean(SecurityFamilyDao.class));
        Assert.assertNotNull(context.getBean(StockDao.class));
        Assert.assertNotNull(context.getBean(AllocationDao.class));
        Assert.assertNotNull(context.getBean(CashBalanceDao.class));
        Assert.assertNotNull(context.getBean(MeasurementDao.class));
        Assert.assertNotNull(context.getBean(OrderPreferenceDao.class));
        Assert.assertNotNull(context.getBean(PortfolioValueDao.class));
        Assert.assertNotNull(context.getBean(StrategyDao.class));
        Assert.assertNotNull(context.getBean(LimitOrderDao.class));
        Assert.assertNotNull(context.getBean(MarketOrderDao.class));
        Assert.assertNotNull(context.getBean(OrderDao.class));
        Assert.assertNotNull(context.getBean(OrderPropertyDao.class));
        Assert.assertNotNull(context.getBean(OrderStatusDao.class));
        Assert.assertNotNull(context.getBean(SimpleOrderDao.class));
        Assert.assertNotNull(context.getBean(StopLimitOrderDao.class));
        Assert.assertNotNull(context.getBean(StopOrderDao.class));

        context.close();
    }

}
