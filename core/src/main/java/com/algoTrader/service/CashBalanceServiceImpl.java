package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.Pair;

public class CashBalanceServiceImpl extends CashBalanceServiceBase {

    private static Logger logger = MyLogger.getLogger(CashBalanceServiceImpl.class.getName());

    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private @Value("${misc.portfolioDigits}") int portfolioDigits;

    @Override
    protected void handleProcessTransaction(Transaction transaction) throws Exception {

        if (transaction.getSecurity() instanceof Forex) {

            // gross transaction value is booked in transaction currency
            processAmount(transaction.getStrategy().getName(), transaction.getCurrency(), transaction.getGrossValue());

            // execution commission is booked in baseCurrency (this is IB specific!)
            if (transaction.getExecutionCommission() != null) {
                processAmount(transaction.getStrategy().getName(), this.portfolioBaseCurrency, transaction.getExecutionCommission().negate());
            }

            // clearing commission is booked in transaction currency
            if (transaction.getClearingCommission() != null) {
                processAmount(transaction.getStrategy().getName(), transaction.getCurrency(), transaction.getClearingCommission().negate());
            }
        } else {

            // the entire transaction (price + commission) is booked in transaction currency
            processAmount(transaction.getStrategy().getName(), transaction.getCurrency(), transaction.getNetValue());
        }
    }

    @Override
    protected void handleProcessAmount(String strategyName, Currency currency, BigDecimal amount) throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);
        CashBalance cashBalance = getCashBalanceDao().findByStrategyAndCurrencyLocked(strategy, currency);
        // create the cashBalance, if it does not exist yet
        if (cashBalance == null) {

            cashBalance = CashBalance.Factory.newInstance();

            cashBalance.setCurrency(currency);
            cashBalance.setAmount(amount);

            // associate with strategy
            strategy.addCashBalances(cashBalance);

            getCashBalanceDao().create(cashBalance);

        } else {

            cashBalance.setAmount(cashBalance.getAmount().add(amount));
        }
    }

    @Override
    protected void handleResetCashBalances() throws Exception {

        // get all existing cashBalances
        Collection<CashBalance> existingCashBalances = getCashBalanceDao().loadAll();

        // sum all transactions
        Collection<Transaction> transactions = getTransactionDao().loadAll();
        Map<Pair<Strategy, Currency>, BigDecimal> map = new HashMap<Pair<Strategy, Currency>, BigDecimal>();
        for (Transaction transaction : transactions) {

            if (transaction.getSecurity() instanceof Forex) {

                // gross transaction value is booked in transaction currency
                addAmount(map, transaction.getStrategy(), transaction.getCurrency(), transaction.getGrossValue());

                // execution commission is booked in baseCurrency (this is IB specific!)
                if (transaction.getExecutionCommission() != null) {
                    addAmount(map, transaction.getStrategy(), this.portfolioBaseCurrency, transaction.getExecutionCommission().negate());
                }

                // clearing commission is booked in transaction currency
                if (transaction.getClearingCommission() != null) {
                    addAmount(map, transaction.getStrategy(), transaction.getCurrency(), transaction.getClearingCommission().negate());
                }
            } else {

                // the entire transaction (price + commission) is booked in transaction currency
                addAmount(map, transaction.getStrategy(), transaction.getCurrency(), transaction.getNetValue());
            }
        }

        // create cash balances
        for (Map.Entry<Pair<Strategy, Currency>, BigDecimal> entry : map.entrySet()) {

            Strategy strategy = entry.getKey().getFirst();
            Currency currency = entry.getKey().getSecond();
            BigDecimal amount = entry.getValue().setScale(this.portfolioDigits, BigDecimal.ROUND_HALF_UP);

            CashBalance cashBalance = getCashBalanceDao().findByStrategyAndCurrencyLocked(strategy, currency);

            if (cashBalance != null) {

                existingCashBalances.remove(cashBalance);

                BigDecimal oldAmount = cashBalance.getAmount();
                if (oldAmount.doubleValue() != amount.doubleValue()) {

                    cashBalance.setAmount(amount);

                    logger.info("adjusted cashBalance: " + cashBalance + " from: " + oldAmount);
                }

            } else {

                cashBalance = CashBalance.Factory.newInstance();
                cashBalance.setCurrency(currency);
                cashBalance.setAmount(amount);

                // associate with strategy
                strategy.addCashBalances(cashBalance);

                getCashBalanceDao().create(cashBalance);

                logger.info("created cashBalance: " + cashBalance);
            }
        }

        // remove all obsolete cashBalances
        for (CashBalance cashBalance : existingCashBalances) {

            Strategy strategy = cashBalance.getStrategy();
            strategy.getCashBalances().remove(cashBalance);

            logger.info("removed cashBalance: " + cashBalance);
        }

        getCashBalanceDao().remove(existingCashBalances);
    }

    private void addAmount(Map<Pair<Strategy, Currency>, BigDecimal> map, Strategy strategy, Currency currency, BigDecimal value) {

        Pair<Strategy, Currency> pair = new Pair<Strategy, Currency>(strategy, currency);

        BigDecimal balance = map.get(pair);
        if (balance == null) {
            balance = new BigDecimal(0);
        }

        map.put(pair, balance.add(value));
    }
}
