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
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CoreConfig;
import ch.algotrader.dao.PositionDao;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.TransactionDao;
import ch.algotrader.dao.marketData.BarDao;
import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.dao.property.PropertyDao;
import ch.algotrader.dao.security.CombinationDao;
import ch.algotrader.dao.security.ComponentDao;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.strategy.CashBalanceDao;
import ch.algotrader.dao.strategy.MeasurementDao;
import ch.algotrader.dao.strategy.PortfolioValueDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderPropertyDao;
import ch.algotrader.dao.trade.OrderStatusDao;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.ResetType;


/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class ResetServiceImpl implements ResetService {

    private final CoreConfig coreConfig;

    private final OrderDao orderDao;

    private final OrderStatusDao orderStatusDao;

    private final OrderPropertyDao orderPropertyDao;

    private final FutureDao futureDao;

    private final TransactionDao transactionDao;

    private final PositionDao positionDao;

    private final SubscriptionDao subscriptionDao;

    private final OptionDao optionDao;

    private final StrategyDao strategyDao;

    private final CashBalanceDao cashBalanceDao;

    private final CombinationDao combinationDao;

    private final ComponentDao componentDao;

    private final PropertyDao propertyDao;

    private final MeasurementDao measurementDao;

    private final PortfolioValueDao portfolioValueDao;

    private final BarDao barDao;

    private final TickDao tickDao;

    public ResetServiceImpl(final CoreConfig coreConfig,
            final OrderDao orderDao,
            final OrderStatusDao orderStatusDao,
            final OrderPropertyDao orderPropertyDao,
            final FutureDao futureDao,
            final TransactionDao transactionDao,
            final PositionDao positionDao,
            final SubscriptionDao subscriptionDao,
            final OptionDao optionDao,
            final StrategyDao strategyDao,
            final CashBalanceDao cashBalanceDao,
            final CombinationDao combinationDao,
            final ComponentDao componentDao,
            final PropertyDao propertyDao,
            final MeasurementDao measurementDao,
            final PortfolioValueDao portfolioValueDao, final BarDao barDao,
            final TickDao tickDao) {

        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(orderStatusDao, "OrderStatusDao is null");
        Validate.notNull(orderPropertyDao, "OrderPropertyDao is null");
        Validate.notNull(futureDao, "FutureDao is null");
        Validate.notNull(transactionDao, "TransactionDao is null");
        Validate.notNull(positionDao, "PositionDao is null");
        Validate.notNull(subscriptionDao, "SubscriptionDao is null");
        Validate.notNull(optionDao, "OptionDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(cashBalanceDao, "CashBalanceDao is null");
        Validate.notNull(combinationDao, "CombinationDao is null");
        Validate.notNull(propertyDao, "PropertyDao is null");
        Validate.notNull(measurementDao, "MeasurementDao is null");
        Validate.notNull(portfolioValueDao, "PortfolioValueDao is null");
        Validate.notNull(barDao, "BarDao is null");
        Validate.notNull(tickDao, "TickDao is null");

        this.coreConfig = coreConfig;
        this.orderDao = orderDao;
        this.orderStatusDao = orderStatusDao;
        this.orderPropertyDao = orderPropertyDao;
        this.futureDao = futureDao;
        this.transactionDao = transactionDao;
        this.positionDao = positionDao;
        this.subscriptionDao = subscriptionDao;
        this.optionDao = optionDao;
        this.strategyDao = strategyDao;
        this.cashBalanceDao = cashBalanceDao;
        this.combinationDao = combinationDao;
        this.componentDao = componentDao;
        this.propertyDao = propertyDao;
        this.measurementDao = measurementDao;
        this.portfolioValueDao = portfolioValueDao;
        this.barDao = barDao;
        this.tickDao = tickDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void reset(EnumSet<ResetType> resetItems) {

        for (ResetType resetItem : resetItems) {

            switch (resetItem) {
                case TRADES:
                    resetTrades();
                    break;
                case ORDERS:
                    resetOrders();
                    break;
                case SUBSCRIPTIONS:
                    resetSubscriptions();
                    break;
                case COMBINATIONS_AND_COMPONENTS:
                    resetCombinationsAndComponents();
                    break;
                case PROPERTIES:
                    resetProperties();
                    break;
                case MEASUREMENTS:
                    resetMeasurements();
                    break;
                case PORTFOLIO_VALUES:
                    resetPortfolioValues();
                    break;
                case OPTIONS:
                    resetOptions();
                    break;
                case FUTURES:
                    resetFutures();
                    break;
                case MARKET_DATA:
                    resetMarketData();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void resetSimulation() {

        resetTrades();

        resetSubscriptions();

        resetCombinationsAndComponents();

        resetProperties();

        resetMeasurements();

        // delete all Options if they are being simulated
        if (this.coreConfig.isSimulateOptions()) {
            resetOptions();
        }

        // delete all Futures if they are being simulated
        if (this.coreConfig.isSimulateFuturesByUnderlying() || this.coreConfig.isSimulateFuturesByGenericFutures()) {
            resetFutures();
        }

    }

    /**
     * deletes all transactions (except the initial CREDIT)
     * resets all cash balances (except the one associated with the initial CREDIT)
     * deletes all non-persistent positions and resets all persistent ones
     */
    private void resetTrades() {

        Collection<Strategy> strategies = this.strategyDao.loadAll();
        for (Strategy strategy : strategies) {

            // delete all transactions except the initial CREDIT
            Collection<Transaction> transactions = this.transactionDao.findByStrategy(strategy.getName());
            Set<Transaction> toRemoveTransactions = new HashSet<>();
            Set<Transaction> toKeepTransactions = new HashSet<>();
            BigDecimal initialAmount = new BigDecimal(0);
            for (Transaction transaction : transactions) {
                if (transaction.getId() == 1) {
                    toKeepTransactions.add(transaction);
                    initialAmount = transaction.getPrice();
                } else {
                    toRemoveTransactions.add(transaction);
                }
            }
            this.transactionDao.deleteAll(toRemoveTransactions);

            // delete all cashBalances except the initial CREDIT
            Collection<CashBalance> cashBalances = strategy.getCashBalances();
            Set<CashBalance> toRemoveCashBalance = new HashSet<>();
            Set<CashBalance> toKeepCashBalances = new HashSet<>();
            for (CashBalance cashBalance : cashBalances) {
                if (cashBalance.getId() == 1) {
                    toKeepCashBalances.add(cashBalance);
                    cashBalance.setAmount(initialAmount);
                } else {
                    toRemoveCashBalance.add(cashBalance);
                }
            }
            this.cashBalanceDao.deleteAll(toRemoveCashBalance);
            strategy.setCashBalances(toKeepCashBalances);
        }

        Collection<Position> nonPersistentPositions = this.positionDao.findNonPersistent();
        for (Position position : nonPersistentPositions) {
            position.getSecurity().removePositions(position);
        }
        this.positionDao.deleteAll(nonPersistentPositions);

        // reset persistent positions
        Collection<Position> persistentPositions = this.positionDao.findPersistent();
        for (Position position : persistentPositions) {
            position.setQuantity(0);
        }
    }

    /**
     * delete all orders, orderStati as well as orderProperty
     */
    private void resetOrders() {

        // delete all order status
        this.orderStatusDao.deleteAll(this.orderStatusDao.loadAll());

        // delete all order properties
        this.orderPropertyDao.deleteAll(this.orderPropertyDao.loadAll());

        // delete all orders
        this.orderDao.deleteAll(this.orderDao.loadAll());
    }

    /**
     * reset non-persistent subscriptions
     */
    private void resetSubscriptions() {

        Collection<Subscription> nonPersistentSubscriptions = this.subscriptionDao.findNonPersistent();
        this.subscriptionDao.deleteAll(nonPersistentSubscriptions);
        for (Subscription subscription : nonPersistentSubscriptions) {
            subscription.getSecurity().getSubscriptions().remove(subscription);
        }
    }

    /**
     * reset non-persistent combinations and components
     */
    private void resetCombinationsAndComponents() {

        // delete all non-persistent combinations
        this.combinationDao.deleteAll(this.combinationDao.findNonPersistent());

        // delete all non-persistent components and references to them
        Collection<Component> nonPersistentComponents = this.componentDao.findNonPersistent();
        this.componentDao.deleteAll(nonPersistentComponents);
        for (Component component : nonPersistentComponents) {
            component.getCombination().getComponents().remove(component);
        }
    }

    /**
     * reset non-persistent properties
     */
    private void resetProperties() {

        Collection<Property> nonPersistentProperties = this.propertyDao.findNonPersistent();
        this.propertyDao.deleteAll(nonPersistentProperties);
        for (Property property : nonPersistentProperties) {
            property.getPropertyHolder().removeProps(property.getName());
        }
    }

    /**
     * reset measurements
     */
    private void resetMeasurements() {
        this.measurementDao.deleteAll(this.measurementDao.loadAll());
    }

    /**
     * reset portfolio values
     */
    private void resetPortfolioValues() {
        this.portfolioValueDao.deleteAll(this.portfolioValueDao.loadAll());
    }

    /**
     * reset options
     */
    private void resetOptions() {
        this.optionDao.deleteAll(this.optionDao.loadAll());
    }

    /**
     * reset futures
     */
    private void resetFutures() {
        this.futureDao.deleteAll(this.futureDao.loadAll());
    }

    /**
     * resets all bars and ticks
     */
    private void resetMarketData() {
        this.barDao.deleteAll(this.barDao.loadAll());
        this.tickDao.deleteAll(this.tickDao.loadAll());
    }
}
