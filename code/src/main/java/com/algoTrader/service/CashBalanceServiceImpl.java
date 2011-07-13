package com.algoTrader.service;

import java.math.BigDecimal;

import com.algoTrader.entity.CashBalance;
import com.algoTrader.entity.Strategy;
import com.algoTrader.enumeration.Currency;

public class CashBalanceServiceImpl extends CashBalanceServiceBase {

    protected void handleAddAmount(Strategy strategy, Currency currency, BigDecimal amount) throws Exception {

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
