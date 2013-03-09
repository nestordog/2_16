/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.property.Property;
import com.algoTrader.entity.security.Component;
import com.algoTrader.entity.security.FutureDao;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOptionDao;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.entity.strategy.Strategy;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ResetServiceImpl extends ResetServiceBase {

    private @Value("${statement.simulateStockOptions}") boolean simulateStockOptions;
    private @Value("${statement.simulateFuturesByUnderlying}") boolean simulateFuturesByUnderlying;
    private @Value("${statement.simulateFuturesByGenericFutures}") boolean simulateFuturesByGenericFutures;

    @Override
    @SuppressWarnings("unchecked")
    protected void handleResetDB() throws Exception {

        // process all strategies
        Collection<Strategy> strategies = getStrategyDao().loadAll();
        for (Strategy strategy : strategies) {

            // delete all transactions except the initial CREDIT
            Collection<Transaction> transactions = getTransactionDao().findByStrategy(strategy.getName());
            Set<Transaction> toRemoveTransactions = new HashSet<Transaction>();
            Set<Transaction> toKeepTransactions = new HashSet<Transaction>();
            BigDecimal initialAmount = new BigDecimal(0);
            for (Transaction transaction : transactions) {
                if (transaction.getId() == 1) {
                    toKeepTransactions.add(transaction);
                    initialAmount = transaction.getPrice();
                } else {
                    toRemoveTransactions.add(transaction);
                }
            }
            getTransactionDao().remove(toRemoveTransactions);

            // delete all cashBalances except the initial CREDIT
            Collection<CashBalance> cashBalances = strategy.getCashBalances();
            Set<CashBalance> toRemoveCashBalance = new HashSet<CashBalance>();
            Set<CashBalance> toKeepCashBalances = new HashSet<CashBalance>();
            for (CashBalance cashBalance : cashBalances) {
                if (cashBalance.getId() == 1) {
                    toKeepCashBalances.add(cashBalance);
                    cashBalance.setAmount(initialAmount);
                } else {
                    toRemoveCashBalance.add(cashBalance);
                }
            }
            getCashBalanceDao().remove(toRemoveCashBalance);
            strategy.setCashBalances(toKeepCashBalances);
        }

        // delete all non-persistent positions and references to them
        Collection<Position> nonPersistentPositions = getPositionDao().findNonPersistent();
        for (Position position : nonPersistentPositions) {
            position.getSecurity().removePositions(position);
        }
        getPositionDao().remove(nonPersistentPositions);

        // reset persistent positions
        Collection<Position> persistentPositions = getPositionDao().findPersistent();
        for (Position position : persistentPositions) {
            position.setQuantity(0);
            position.setExitValue(null);
            position.setMaintenanceMargin(null);
        }

        // delete all non-presistent subscriptions and references to them
        Collection<Subscription> nonPersistentSubscriptions = getSubscriptionDao().findNonPersistent();
        getSubscriptionDao().remove(nonPersistentSubscriptions);
        for (Subscription subscription : nonPersistentSubscriptions) {
            subscription.getSecurity().getSubscriptions().remove(subscription);
        }

        // delete all non-persistent combinations
        getCombinationDao().remove(getCombinationDao().findNonPersistent());

        // delete all non-persistent components and references to them
        Collection<Component> nonPersistentComponents = getComponentDao().findNonPersistent();
        getComponentDao().remove(nonPersistentComponents);
        for (Component component : nonPersistentComponents) {
            component.getParentSecurity().getComponents().remove(component);
        }

        // delete all non-persistent properties
        Collection<Property> nonPersistentProperties = getPropertyDao().findNonPersistent();
        getPropertyDao().remove(nonPersistentProperties);
        for (Property property : nonPersistentProperties) {
            property.getPropertyHolder().removeProperties(property.getName());
        }

        // delete all measurements
        getMeasurementDao().remove(getMeasurementDao().loadAll());

        // delete all StockOptions if they are beeing simulated
        if (this.simulateStockOptions) {
            getSecurityDao().remove((Collection<Security>) getStockOptionDao().loadAll(StockOptionDao.TRANSFORM_NONE));
        }

        // delete all Futures if they are beeing simulated
        if (this.simulateFuturesByUnderlying || this.simulateFuturesByGenericFutures) {
            getSecurityDao().remove((Collection<Security>) getFutureDao().loadAll(FutureDao.TRANSFORM_NONE));
        }

    }
}
