package com.algoTrader.service;

import java.math.BigDecimal;

import com.algoTrader.entity.CashBalance;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.ConfigurationUtil;

public class CashBalanceServiceImpl extends CashBalanceServiceBase {

    private static Currency portfolioBaseCurrency = Currency.fromString(ConfigurationUtil.getBaseConfig().getString("portfolioBaseCurrency"));

    @Override
    protected void handleAddAmount(Transaction transaction) throws Exception {

        if (transaction.getSecurity() instanceof Forex) {

            // gross transaction value is booked int transaction currency
            addAmount(transaction.getStrategy(), transaction.getCurrency(), transaction.getGrossValue());

            // commission is booked in baseCurrency
            addAmount(transaction.getStrategy(), portfolioBaseCurrency, transaction.getCommission());
        } else {

            // the entire transaction (price + commission) is booked in transaction currency
            addAmount(transaction.getStrategy(), transaction.getCurrency(), transaction.getNetValue());
        }
    }

    private void addAmount(Strategy strategy, Currency currency, BigDecimal amount) {

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
}
