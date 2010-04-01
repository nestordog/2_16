package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;

import com.algoTrader.util.RoundUtil;

public class AccountDaoImpl extends com.algoTrader.entity.AccountDaoBase {

    @SuppressWarnings("unchecked")
    protected BigDecimal handleGetPortfolioValueAllAccounts() throws Exception {

        Collection<Account> accounts = loadAll();

        double portfolioValue = 0;
        for (Account account : accounts) {
            portfolioValue += account.getPortfolioValueDouble();
        }

        return RoundUtil.getBigDecimal(portfolioValue);
    }
}
