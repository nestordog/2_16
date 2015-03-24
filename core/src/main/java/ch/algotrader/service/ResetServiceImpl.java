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
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CoreConfig;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.SubscriptionDao;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.property.PropertyDao;
import ch.algotrader.entity.security.CombinationDao;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.security.ComponentDao;
import ch.algotrader.entity.security.FutureDao;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.CashBalanceDao;
import ch.algotrader.entity.strategy.MeasurementDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyDao;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ResetServiceImpl implements ResetService {

    private final CoreConfig coreConfig;

    private final SecurityDao securityDao;

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

    public ResetServiceImpl(final CoreConfig coreConfig,
            final SecurityDao securityDao,
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
            final MeasurementDao measurementDao) {

        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(securityDao, "SecurityDao is null");
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

        this.coreConfig = coreConfig;
        this.securityDao = securityDao;
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void resetDB() {

        // process all strategies
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

        // delete all non-persistent positions and references to them
        Collection<Position> nonPersistentPositions = this.positionDao.findNonPersistent();
        for (Position position : nonPersistentPositions) {
            position.getSecurity().removePositions(position);
        }
        this.positionDao.deleteAll(nonPersistentPositions);

        // reset persistent positions
        Collection<Position> persistentPositions = this.positionDao.findPersistent();
        for (Position position : persistentPositions) {
            position.setQuantity(0);
            position.setExitValue(null);
            position.setMaintenanceMargin(null);
        }

        // delete all non-presistent subscriptions and references to them
        Collection<Subscription> nonPersistentSubscriptions = this.subscriptionDao.findNonPersistent();
        this.subscriptionDao.deleteAll(nonPersistentSubscriptions);
        for (Subscription subscription : nonPersistentSubscriptions) {
            subscription.getSecurity().getSubscriptions().remove(subscription);
        }

        // delete all non-persistent combinations
        this.combinationDao.deleteAll(this.combinationDao.findNonPersistent());

        // delete all non-persistent components and references to them
        Collection<Component> nonPersistentComponents = this.componentDao.findNonPersistent();
        this.componentDao.deleteAll(nonPersistentComponents);
        for (Component component : nonPersistentComponents) {
            component.getCombination().getComponents().remove(component);
        }

        // delete all non-persistent properties
        Collection<Property> nonPersistentProperties = this.propertyDao.findNonPersistent();
        this.propertyDao.deleteAll(nonPersistentProperties);
        for (Property property : nonPersistentProperties) {
            property.getPropertyHolder().removeProps(property.getName());
        }

        // delete all measurements
        this.measurementDao.deleteAll(this.measurementDao.loadAll());

        // delete all Options if they are beeing simulated
        if (this.coreConfig.isSimulateOptions()) {
            this.optionDao.deleteAll(this.optionDao.loadAll());
        }

        // delete all Futures if they are beeing simulated
        if (this.coreConfig.isSimulateFuturesByUnderlying() || this.coreConfig.isSimulateFuturesByGenericFutures()) {
            this.futureDao.deleteAll(this.futureDao.loadAll());
        }

    }
}
