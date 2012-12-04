package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.collection.BigDecimalMap;
import com.algoTrader.util.collection.Pair;
import com.algoTrader.vo.CurrencyAmountVO;

public class CashBalanceServiceImpl extends CashBalanceServiceBase {

    private static Logger logger = MyLogger.getLogger(CashBalanceServiceImpl.class.getName());

    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private @Value("${misc.portfolioDigits}") int portfolioDigits;

    @Override
    protected void handleProcessTransaction(Transaction transaction) throws Exception {

        // process all currenyAmounts
        for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
            processAmount(transaction.getStrategy().getName(), currencyAmount.getCurrency(), currencyAmount.getAmount());
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
        BigDecimalMap<Pair<Strategy, Currency>> map = new BigDecimalMap<Pair<Strategy, Currency>>();
        for (Transaction transaction : transactions) {

            // process all currenyAmounts
            for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
                map.increment(new Pair<Strategy, Currency>(transaction.getStrategy(), currencyAmount.getCurrency()), currencyAmount.getAmount());
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
}
