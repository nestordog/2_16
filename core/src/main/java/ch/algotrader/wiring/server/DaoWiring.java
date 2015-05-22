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

import ch.algotrader.entity.AccountDao;
import ch.algotrader.entity.AccountDaoImpl;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.PositionDaoImpl;
import ch.algotrader.entity.SubscriptionDao;
import ch.algotrader.entity.SubscriptionDaoImpl;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.TransactionDaoImpl;
import ch.algotrader.entity.exchange.ExchangeDao;
import ch.algotrader.entity.exchange.ExchangeDaoImpl;
import ch.algotrader.entity.exchange.HolidayDao;
import ch.algotrader.entity.exchange.HolidayDaoImpl;
import ch.algotrader.entity.exchange.TradingHoursDao;
import ch.algotrader.entity.exchange.TradingHoursDaoImpl;
import ch.algotrader.entity.marketData.BarDao;
import ch.algotrader.entity.marketData.BarDaoImpl;
import ch.algotrader.entity.marketData.GenericTickDao;
import ch.algotrader.entity.marketData.GenericTickDaoImpl;
import ch.algotrader.entity.marketData.MarketDataEventDao;
import ch.algotrader.entity.marketData.MarketDataEventDaoImpl;
import ch.algotrader.entity.marketData.TickDao;
import ch.algotrader.entity.marketData.TickDaoImpl;
import ch.algotrader.entity.property.PropertyDao;
import ch.algotrader.entity.property.PropertyDaoImpl;
import ch.algotrader.entity.property.PropertyHolderDao;
import ch.algotrader.entity.property.PropertyHolderDaoImpl;
import ch.algotrader.entity.security.BondDao;
import ch.algotrader.entity.security.BondDaoImpl;
import ch.algotrader.entity.security.BondFamilyDao;
import ch.algotrader.entity.security.BondFamilyDaoImpl;
import ch.algotrader.entity.security.BrokerParametersDao;
import ch.algotrader.entity.security.BrokerParametersDaoImpl;
import ch.algotrader.entity.security.CombinationDao;
import ch.algotrader.entity.security.CombinationDaoImpl;
import ch.algotrader.entity.security.CommodityDao;
import ch.algotrader.entity.security.CommodityDaoImpl;
import ch.algotrader.entity.security.ComponentDao;
import ch.algotrader.entity.security.ComponentDaoImpl;
import ch.algotrader.entity.security.EasyToBorrowDao;
import ch.algotrader.entity.security.EasyToBorrowDaoImpl;
import ch.algotrader.entity.security.ForexDao;
import ch.algotrader.entity.security.ForexDaoImpl;
import ch.algotrader.entity.security.FundDao;
import ch.algotrader.entity.security.FundDaoImpl;
import ch.algotrader.entity.security.FutureDao;
import ch.algotrader.entity.security.FutureDaoImpl;
import ch.algotrader.entity.security.FutureFamilyDao;
import ch.algotrader.entity.security.FutureFamilyDaoImpl;
import ch.algotrader.entity.security.GenericFutureDao;
import ch.algotrader.entity.security.GenericFutureDaoImpl;
import ch.algotrader.entity.security.GenericFutureFamilyDao;
import ch.algotrader.entity.security.GenericFutureFamilyDaoImpl;
import ch.algotrader.entity.security.ImpliedVolatilityDao;
import ch.algotrader.entity.security.ImpliedVolatilityDaoImpl;
import ch.algotrader.entity.security.IndexDao;
import ch.algotrader.entity.security.IndexDaoImpl;
import ch.algotrader.entity.security.IntrestRateDao;
import ch.algotrader.entity.security.IntrestRateDaoImpl;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.OptionDaoImpl;
import ch.algotrader.entity.security.OptionFamilyDao;
import ch.algotrader.entity.security.OptionFamilyDaoImpl;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.security.SecurityDaoImpl;
import ch.algotrader.entity.security.SecurityFamilyDao;
import ch.algotrader.entity.security.SecurityFamilyDaoImpl;
import ch.algotrader.entity.security.StockDao;
import ch.algotrader.entity.security.StockDaoImpl;
import ch.algotrader.entity.strategy.CashBalanceDao;
import ch.algotrader.entity.strategy.CashBalanceDaoImpl;
import ch.algotrader.entity.strategy.MeasurementDao;
import ch.algotrader.entity.strategy.MeasurementDaoImpl;
import ch.algotrader.entity.strategy.PortfolioValueDao;
import ch.algotrader.entity.strategy.PortfolioValueDaoImpl;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.entity.strategy.StrategyDaoImpl;
import ch.algotrader.entity.trade.AllocationDao;
import ch.algotrader.entity.trade.AllocationDaoImpl;
import ch.algotrader.entity.trade.LimitOrderDao;
import ch.algotrader.entity.trade.LimitOrderDaoImpl;
import ch.algotrader.entity.trade.MarketOrderDao;
import ch.algotrader.entity.trade.MarketOrderDaoImpl;
import ch.algotrader.entity.trade.OrderDao;
import ch.algotrader.entity.trade.OrderDaoImpl;
import ch.algotrader.entity.trade.OrderPreferenceDao;
import ch.algotrader.entity.trade.OrderPreferenceDaoImpl;
import ch.algotrader.entity.trade.OrderPropertyDao;
import ch.algotrader.entity.trade.OrderPropertyDaoImpl;
import ch.algotrader.entity.trade.OrderStatusDao;
import ch.algotrader.entity.trade.OrderStatusDaoImpl;
import ch.algotrader.entity.trade.SimpleOrderDao;
import ch.algotrader.entity.trade.SimpleOrderDaoImpl;
import ch.algotrader.entity.trade.StopLimitOrderDao;
import ch.algotrader.entity.trade.StopLimitOrderDaoImpl;
import ch.algotrader.entity.trade.StopOrderDao;
import ch.algotrader.entity.trade.StopOrderDaoImpl;
import ch.algotrader.esper.Engine;

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
    public TickDao createTickDao(final SessionFactory sessionFactory, final SubscriptionDao subscriptionDao, final SecurityDao securityDao, final Engine serverEngine) {

        return new TickDaoImpl(sessionFactory, subscriptionDao, securityDao, serverEngine);
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

    @Bean(name = "impliedVolatilityDao")
    public ImpliedVolatilityDao createImpliedVolatilityDao(final SessionFactory sessionFactory) {

        return new ImpliedVolatilityDaoImpl(sessionFactory);
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
    public OrderDao createOrderDao(final SessionFactory sessionFactory, final Engine serverEngine) {

        return new OrderDaoImpl(sessionFactory, serverEngine);
    }

    @Bean(name = "orderPropertyDao")
    public OrderPropertyDao createOrderPropertyDao(final SessionFactory sessionFactory) {

        return new OrderPropertyDaoImpl(sessionFactory);
    }

    @Bean(name = "orderStatusDao")
    public OrderStatusDao createOrderStatusDao(final SessionFactory sessionFactory, final Engine serverEngine) {

        return new OrderStatusDaoImpl(sessionFactory, serverEngine);
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
