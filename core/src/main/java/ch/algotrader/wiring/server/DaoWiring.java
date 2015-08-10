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

package ch.algotrader.wiring.server;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.AccountDaoImpl;
import ch.algotrader.dao.PositionDao;
import ch.algotrader.dao.PositionDaoImpl;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.SubscriptionDaoImpl;
import ch.algotrader.dao.TransactionDao;
import ch.algotrader.dao.TransactionDaoImpl;
import ch.algotrader.dao.exchange.ExchangeDao;
import ch.algotrader.dao.exchange.ExchangeDaoImpl;
import ch.algotrader.dao.exchange.HolidayDao;
import ch.algotrader.dao.exchange.HolidayDaoImpl;
import ch.algotrader.dao.exchange.TradingHoursDao;
import ch.algotrader.dao.exchange.TradingHoursDaoImpl;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.marketData.BarDaoImpl;
import ch.algotrader.dao.marketData.GenericTickDao;
import ch.algotrader.dao.marketData.GenericTickDaoImpl;
import ch.algotrader.dao.marketData.MarketDataEventDao;
import ch.algotrader.dao.marketData.MarketDataEventDaoImpl;
import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.dao.marketData.TickDaoImpl;
import ch.algotrader.dao.property.PropertyDao;
import ch.algotrader.dao.property.PropertyDaoImpl;
import ch.algotrader.dao.property.PropertyHolderDao;
import ch.algotrader.dao.property.PropertyHolderDaoImpl;
import ch.algotrader.dao.security.BondDao;
import ch.algotrader.dao.security.BondDaoImpl;
import ch.algotrader.dao.security.BondFamilyDao;
import ch.algotrader.dao.security.BondFamilyDaoImpl;
import ch.algotrader.dao.security.BrokerParametersDao;
import ch.algotrader.dao.security.BrokerParametersDaoImpl;
import ch.algotrader.dao.security.CombinationDao;
import ch.algotrader.dao.security.CombinationDaoImpl;
import ch.algotrader.dao.security.CommodityDao;
import ch.algotrader.dao.security.CommodityDaoImpl;
import ch.algotrader.dao.security.ComponentDao;
import ch.algotrader.dao.security.ComponentDaoImpl;
import ch.algotrader.dao.security.EasyToBorrowDao;
import ch.algotrader.dao.security.EasyToBorrowDaoImpl;
import ch.algotrader.dao.security.ForexDao;
import ch.algotrader.dao.security.ForexDaoImpl;
import ch.algotrader.dao.security.FundDao;
import ch.algotrader.dao.security.FundDaoImpl;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.FutureDaoImpl;
import ch.algotrader.dao.security.FutureFamilyDao;
import ch.algotrader.dao.security.FutureFamilyDaoImpl;
import ch.algotrader.dao.security.GenericFutureDao;
import ch.algotrader.dao.security.GenericFutureDaoImpl;
import ch.algotrader.dao.security.GenericFutureFamilyDao;
import ch.algotrader.dao.security.GenericFutureFamilyDaoImpl;
import ch.algotrader.dao.security.IndexDao;
import ch.algotrader.dao.security.IndexDaoImpl;
import ch.algotrader.dao.security.IntrestRateDao;
import ch.algotrader.dao.security.IntrestRateDaoImpl;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.OptionDaoImpl;
import ch.algotrader.dao.security.OptionFamilyDao;
import ch.algotrader.dao.security.OptionFamilyDaoImpl;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.security.SecurityDaoImpl;
import ch.algotrader.dao.security.SecurityFamilyDao;
import ch.algotrader.dao.security.SecurityFamilyDaoImpl;
import ch.algotrader.dao.security.SecurityReferenceDao;
import ch.algotrader.dao.security.SecurityReferenceDaoImpl;
import ch.algotrader.dao.security.StockDao;
import ch.algotrader.dao.security.StockDaoImpl;
import ch.algotrader.dao.strategy.CashBalanceDao;
import ch.algotrader.dao.strategy.CashBalanceDaoImpl;
import ch.algotrader.dao.strategy.MeasurementDao;
import ch.algotrader.dao.strategy.MeasurementDaoImpl;
import ch.algotrader.dao.strategy.PortfolioValueDao;
import ch.algotrader.dao.strategy.PortfolioValueDaoImpl;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.strategy.StrategyDaoImpl;
import ch.algotrader.dao.trade.AllocationDao;
import ch.algotrader.dao.trade.AllocationDaoImpl;
import ch.algotrader.dao.trade.LimitOrderDao;
import ch.algotrader.dao.trade.LimitOrderDaoImpl;
import ch.algotrader.dao.trade.MarketOrderDao;
import ch.algotrader.dao.trade.MarketOrderDaoImpl;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderDaoImpl;
import ch.algotrader.dao.trade.OrderPreferenceDao;
import ch.algotrader.dao.trade.OrderPreferenceDaoImpl;
import ch.algotrader.dao.trade.OrderPropertyDao;
import ch.algotrader.dao.trade.OrderPropertyDaoImpl;
import ch.algotrader.dao.trade.OrderStatusDao;
import ch.algotrader.dao.trade.OrderStatusDaoImpl;
import ch.algotrader.dao.trade.SimpleOrderDao;
import ch.algotrader.dao.trade.SimpleOrderDaoImpl;
import ch.algotrader.dao.trade.StopLimitOrderDao;
import ch.algotrader.dao.trade.StopLimitOrderDaoImpl;
import ch.algotrader.dao.trade.StopOrderDao;
import ch.algotrader.dao.trade.StopOrderDaoImpl;

/**
 * Dao configuration.
 *
 * @version $Revision$ $Date$
 */
@Configuration
public class DaoWiring {

    private final int intervalDays = 4;

    @Bean(name = "accountDao")
    public AccountDao createAccountDao(final SessionFactory sessionFactory) {

        return new AccountDaoImpl(sessionFactory);
    }

    @Bean(name = "positionDao")
    public PositionDao createPositionDao(final SessionFactory sessionFactory) {

        return new PositionDaoImpl(sessionFactory);
    }

    @Bean(name = "subscriptionDao")
    public SubscriptionDao createSubscriptionDao(final SessionFactory sessionFactory) {

        return new SubscriptionDaoImpl(sessionFactory);
    }

    @Bean(name = "transactionDao")
    public TransactionDao createTransactionDao(final SessionFactory sessionFactory) {

        return new TransactionDaoImpl(sessionFactory);
    }

    @Bean(name = "exchangeDao")
    public ExchangeDao createExchangeDao(final SessionFactory sessionFactory) {

        return new ExchangeDaoImpl(sessionFactory);
    }

    @Bean(name = "holidayDao")
    public HolidayDao createHolidayDao(final SessionFactory sessionFactory) {

        return new HolidayDaoImpl(sessionFactory);
    }

    @Bean(name = "tradingHoursDao")
    public TradingHoursDao createTradingHoursDao(final SessionFactory sessionFactory) {

        return new TradingHoursDaoImpl(sessionFactory);
    }

    @Bean(name = "barDao")
    public BarDao createBarDao(final SessionFactory sessionFactory) {

        return new BarDaoImpl(sessionFactory);
    }

    @Bean(name = "genericTickDao")
    public GenericTickDao createGenericTickDao(final SessionFactory sessionFactory) {

        return new GenericTickDaoImpl(sessionFactory);
    }

    @Bean(name = "marketDataEventDao")
    public MarketDataEventDao createMarketDataEventDao(final SessionFactory sessionFactory) {

        return new MarketDataEventDaoImpl(sessionFactory);
    }

    @Bean(name = "tickDao")
    public TickDao createTickDao(final SessionFactory sessionFactory) {

        return new TickDaoImpl(sessionFactory);
    }

    @Bean(name = "propertyDao")
    public PropertyDao createPropertyDao(final SessionFactory sessionFactory) {

        return new PropertyDaoImpl(sessionFactory);
    }

    @Bean(name = "propertyHolderDao")
    public PropertyHolderDao createPropertyHolderDao(final SessionFactory sessionFactory) {

        return new PropertyHolderDaoImpl(sessionFactory);
    }

    @Bean(name = "bondDao")
    public BondDao createBondDao(final SessionFactory sessionFactory) {

        return new BondDaoImpl(sessionFactory);
    }

    @Bean(name = "bondFamilyDao")
    public BondFamilyDao createBondFamilyDao(final SessionFactory sessionFactory) {

        return new BondFamilyDaoImpl(sessionFactory);
    }

    @Bean(name = "brokerParametersDao")
    public BrokerParametersDao createBrokerParametersDao(final SessionFactory sessionFactory) {

        return new BrokerParametersDaoImpl(sessionFactory);
    }

    @Bean(name = "combinationDao")
    public CombinationDao createCombinationDao(final SessionFactory sessionFactory) {

        return new CombinationDaoImpl(sessionFactory);
    }

    @Bean(name = "commodityDao")
    public CommodityDao createCommodityDao(final SessionFactory sessionFactory) {

        return new CommodityDaoImpl(sessionFactory);
    }

    @Bean(name = "componentDao")
    public ComponentDao createComponentDao(final SessionFactory sessionFactory) {

        return new ComponentDaoImpl(sessionFactory);
    }

    @Bean(name = "easyToBorrowDao")
    public EasyToBorrowDao createEasyToBorrowDao(final SessionFactory sessionFactory) {

        return new EasyToBorrowDaoImpl(sessionFactory);
    }

    @Bean(name = "forexDao")
    public ForexDao createForexDao(final SessionFactory sessionFactory, final TickDao tickDao) {

        return new ForexDaoImpl(sessionFactory, tickDao, this.intervalDays);
    }

    @Bean(name = "fundDao")
    public FundDao createFundDao(final SessionFactory sessionFactory) {

        return new FundDaoImpl(sessionFactory);
    }

    @Bean(name = "futureDao")
    public FutureDao createFutureDao(final SessionFactory sessionFactory) {

        return new FutureDaoImpl(sessionFactory);
    }

    @Bean(name = "futureFamilyDao")
    public FutureFamilyDao createFutureFamilyDao(final SessionFactory sessionFactory) {

        return new FutureFamilyDaoImpl(sessionFactory);
    }

    @Bean(name = "genericFutureDao")
    public GenericFutureDao createGenericFutureDao(final SessionFactory sessionFactory) {

        return new GenericFutureDaoImpl(sessionFactory);
    }

    @Bean(name = "genericFutureFamilyDao")
    public GenericFutureFamilyDao createGenericFutureFamilyDao(final SessionFactory sessionFactory) {

        return new GenericFutureFamilyDaoImpl(sessionFactory);
    }

    @Bean(name = "indexDao")
    public IndexDao createIndexDao(final SessionFactory sessionFactory) {

        return new IndexDaoImpl(sessionFactory);
    }

    @Bean(name = "intrestRateDao")
    public IntrestRateDao createIntrestRateDao(final SessionFactory sessionFactory) {

        return new IntrestRateDaoImpl(sessionFactory);
    }

    @Bean(name = "optionDao")
    public OptionDao createOptionDao(final SessionFactory sessionFactory) {

        return new OptionDaoImpl(sessionFactory);
    }

    @Bean(name = "optionFamilyDao")
    public OptionFamilyDao createOptionFamilyDao(final SessionFactory sessionFactory) {

        return new OptionFamilyDaoImpl(sessionFactory);
    }

    @Bean(name = "securityDao")
    public SecurityDao createSecurityDao(final SessionFactory sessionFactory) {

        return new SecurityDaoImpl(sessionFactory);
    }

    @Bean(name = "securityFamilyDao")
    public SecurityFamilyDao createSecurityFamilyDao(final SessionFactory sessionFactory) {

        return new SecurityFamilyDaoImpl(sessionFactory);
    }

    @Bean(name = "securityReferenceDao")
    public SecurityReferenceDao createSecurityReferenceDao(final SessionFactory sessionFactory) {

        return new SecurityReferenceDaoImpl(sessionFactory);
    }

    @Bean(name = "stockDao")
    public StockDao createStockDao(final SessionFactory sessionFactory) {

        return new StockDaoImpl(sessionFactory);
    }

    @Bean(name = "allocationDao")
    public AllocationDao createAllocationDao(final SessionFactory sessionFactory) {

        return new AllocationDaoImpl(sessionFactory);
    }

    @Bean(name = "cashBalanceDao")
    public CashBalanceDao createCashBalanceDao(final SessionFactory sessionFactory) {

        return new CashBalanceDaoImpl(sessionFactory);
    }

    @Bean(name = "measurementDao")
    public MeasurementDao createMeasurementDao(final SessionFactory sessionFactory) {

        return new MeasurementDaoImpl(sessionFactory);
    }

    @Bean(name = "orderPreferenceDao")
    public OrderPreferenceDao createOrderPreferenceDao(final SessionFactory sessionFactory) {

        return new OrderPreferenceDaoImpl(sessionFactory);
    }

    @Bean(name = "portfolioValueDao")
    public PortfolioValueDao createPortfolioValueDao(final SessionFactory sessionFactory) {

        return new PortfolioValueDaoImpl(sessionFactory);
    }

    @Bean(name = "strategyDao")
    public StrategyDao createStrategyDao(final SessionFactory sessionFactory) {

        return new StrategyDaoImpl(sessionFactory);
    }

    @Bean(name = "limitOrderDao")
    public LimitOrderDao createLimitOrderDao(final SessionFactory sessionFactory) {

        return new LimitOrderDaoImpl(sessionFactory);
    }

    @Bean(name = "marketOrderDao")
    public MarketOrderDao createMarketOrderDao(final SessionFactory sessionFactory) {

        return new MarketOrderDaoImpl(sessionFactory);
    }

    @Bean(name = "orderDao")
    public OrderDao createOrderDao(final SessionFactory sessionFactory) {

        return new OrderDaoImpl(sessionFactory);
    }

    @Bean(name = "orderPropertyDao")
    public OrderPropertyDao createOrderPropertyDao(final SessionFactory sessionFactory) {

        return new OrderPropertyDaoImpl(sessionFactory);
    }

    @Bean(name = "orderStatusDao")
    public OrderStatusDao createOrderStatusDao(final SessionFactory sessionFactory) {

        return new OrderStatusDaoImpl(sessionFactory);
    }

    @Bean(name = "simpleOrderDao")
    public SimpleOrderDao createSimpleOrderDao(final SessionFactory sessionFactory) {

        return new SimpleOrderDaoImpl(sessionFactory);
    }

    @Bean(name = "stopLimitOrderDao")
    public StopLimitOrderDao createStopLimitOrderDao(final SessionFactory sessionFactory) {

        return new StopLimitOrderDaoImpl(sessionFactory);
    }

    @Bean(name = "stopOrderDao")
    public StopOrderDao createStopOrderDao(final SessionFactory sessionFactory) {

        return new StopOrderDaoImpl(sessionFactory);
    }

}
