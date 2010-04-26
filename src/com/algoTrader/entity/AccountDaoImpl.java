package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;

import com.algoTrader.util.RoundUtil;

public class AccountDaoImpl extends AccountDaoBase {

    @SuppressWarnings("unchecked")
    protected BigDecimal handleGetTotalValueAllAccounts() throws Exception {

        Collection<Account> accounts = loadAll();

        double totalValue = 0;
        for (Account account : accounts) {
            totalValue += account.getTotalValueDouble();
        }

        return RoundUtil.getBigDecimal(totalValue);
    }
}
