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

    @Override
    protected void handleProcessTransaction(Transaction transaction) throws Exception {

        if (transaction.getSecurity() instanceof Forex) {

            // gross transaction value is booked in transaction currency
            processAmount(transaction.getStrategy(), transaction.getCurrency(), transaction.getGrossValue());

            // commission is booked in baseCurrency (commission is also stored in base currency in db)
            processAmount(transaction.getStrategy(), this.portfolioBaseCurrency, transaction.getCommission().negate());
        } else {

            // the entire transaction (price + commission) is booked in transaction currency
            processAmount(transaction.getStrategy(), transaction.getCurrency(), transaction.getNetValue());
        }
    }

    @Override
    protected void handleProcessAmount(Strategy strategy, Currency currency, BigDecimal amount) throws Exception {

        CashBalance cashBalance = getCashBalanceDao().findByStrategyAndCurrency(strategy, currency);

        // create the cashBalance, if it does not exist yet
        if (cashBalance == null) {

            cashBalance = CashBalance.Factory.newInstance();

            cashBalance.setStrategy(strategy);
            cashBalance.setCurrency(currency);
            cashBalance.setAmount(amount);

            strategy.getCashBalances().add(cashBalance);

            getCashBalanceDao().create(cashBalance);
            getStrategyDao().update(strategy);

        } else {

            cashBalance.setAmount(cashBalance.getAmount().add(amount));

            getCashBalanceDao().update(cashBalance);
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

                // commission is booked in baseCurrency (commission is also stored in base currency in db)
                addAmount(map, transaction.getStrategy(), this.portfolioBaseCurrency, transaction.getCommission().negate());
            } else {

                // the entire transaction (price + commission) is booked in transaction currency
                addAmount(map, transaction.getStrategy(), transaction.getCurrency(), transaction.getNetValue());
            }
        }

        // create cash balances
        for (Map.Entry<Pair<Strategy, Currency>, BigDecimal> entry : map.entrySet()) {

            Strategy strategy = entry.getKey().getFirst();
            Currency currency = entry.getKey().getSecond();
            BigDecimal amount = entry.getValue();

            CashBalance cashBalance = getCashBalanceDao().findByStrategyAndCurrency(strategy, currency);

            if (cashBalance != null) {

                existingCashBalances.remove(cashBalance);

                BigDecimal oldAmount = cashBalance.getAmount();
                if (oldAmount.doubleValue() != amount.doubleValue()) {

                    cashBalance.setAmount(amount);
                    getCashBalanceDao().update(cashBalance);

                    logger.info("adjusted cashBalance: " + cashBalance + " from: " + oldAmount);
                } else {

                    logger.info("no change on cashBalance: " + cashBalance);
                }

            } else {

                cashBalance = CashBalance.Factory.newInstance();
                cashBalance.setStrategy(strategy);
                cashBalance.setCurrency(currency);
                cashBalance.setAmount(amount);

                strategy.getCashBalances().add(cashBalance);

                getCashBalanceDao().create(cashBalance);
                getStrategyDao().update(strategy);

                logger.info("created cashBalance: " + cashBalance);
            }
        }

        // remove all obsolete cashBalances
        for (CashBalance cashBalance : existingCashBalances) {

            Strategy strategy = cashBalance.getStrategy();
            strategy.getCashBalances().remove(cashBalance);
            getStrategyDao().update(strategy);

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
