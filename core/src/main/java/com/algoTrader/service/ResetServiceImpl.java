package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.Property;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Component;
import com.algoTrader.entity.security.FutureDao;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOptionDao;
import com.algoTrader.entity.strategy.CashBalance;

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
            Collection<Transaction> transactions = strategy.getTransactions();
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
            strategy.setTransactions(toKeepTransactions);

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

            // delete all positions and references to them
            Collection<Position> positions = strategy.getPositions();
            getPositionDao().remove(positions);
            strategy.getPositions().removeAll(positions);
        }

        // delete all non-presistent subscriptions and references to them
        List<Subscription> nonPersistentSubscriptions = getSubscriptionDao().findNonPersistent();
        getSubscriptionDao().remove(nonPersistentSubscriptions);
        for (Subscription subscription : nonPersistentSubscriptions) {
            subscription.getSecurity().getSubscriptions().remove(subscription);
            subscription.getStrategy().getSubscriptions().remove(subscription);
        }

        // delete all non-persistent combinations
        getCombinationDao().remove(getCombinationDao().findNonPersistent());

        // delete all non-persistent components and references to them
        List<Component> nonPersistentComponents = getComponentDao().findNonPersistent();
        getComponentDao().remove(nonPersistentComponents);
        for (Component component : nonPersistentComponents) {
            component.getParentSecurity().getComponents().remove(component);
        }

        // delete all non-persistent properties
        List<Property> nonPersistentProperties = getPropertyDao().findNonPersistent();
        getPropertyDao().remove(nonPersistentProperties);
        for (Property property : nonPersistentProperties) {
            property.getPropertyHolder().getProperties().remove(property);
        }

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
