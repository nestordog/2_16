package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;

import com.algoTrader.util.RoundUtil;

public class AccountDaoImpl extends AccountDaoBase {

    private Collection<Account> accounts; // cache this because it get's called very often

    protected BigDecimal handleGetTotalValueAllAccounts() throws Exception {

        Collection<Account> accounts = loadAll();

        double totalValue = 0;
        for (Account account : accounts) {
            totalValue += account.getTotalValueDouble();
        }

        return RoundUtil.getBigDecimal(totalValue);
    }

    protected BigDecimal handleGetAvailableAmountAllAccounts() throws Exception {

        Collection<Account> accounts = loadAll();

        double availableAmount = 0;
        for (Account account : accounts) {
            availableAmount += account.getAvailableAmountDouble();
        }

        return RoundUtil.getBigDecimal(availableAmount);
    }

    @SuppressWarnings("unchecked")
    public Collection<Account> loadAll() {

        if (this.accounts == null) {
            this.accounts = super.loadAll();
        }
        return this.accounts;
    }
}
