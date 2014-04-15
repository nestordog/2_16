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

import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.property.Property;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.security.FutureDao;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.Strategy;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ResetServiceImpl extends ResetServiceBase {

    private @Value("${statement.simulateOptions}") boolean simulateOptions;
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
            component.getCombination().getComponents().remove(component);
        }

        // delete all non-persistent properties
        Collection<Property> nonPersistentProperties = getPropertyDao().findNonPersistent();
        getPropertyDao().remove(nonPersistentProperties);
        for (Property property : nonPersistentProperties) {
            property.getPropertyHolder().removeProps(property.getName());
        }

        // delete all measurements
        getMeasurementDao().remove(getMeasurementDao().loadAll());

        // delete all Options if they are beeing simulated
        if (this.simulateOptions) {
            getSecurityDao().remove((Collection<Security>) getOptionDao().loadAll(OptionDao.TRANSFORM_NONE));
        }

        // delete all Futures if they are beeing simulated
        if (this.simulateFuturesByUnderlying || this.simulateFuturesByGenericFutures) {
            getSecurityDao().remove((Collection<Security>) getFutureDao().loadAll(FutureDao.TRANSFORM_NONE));
        }

    }
}
