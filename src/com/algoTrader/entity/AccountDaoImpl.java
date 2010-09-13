package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;

import com.algoTrader.util.RoundUtil;

public class AccountDaoImpl extends AccountDaoBase {

    @SuppressWarnings("unchecked")
    protected BigDecimal handleGetCashBalanceAllAccounts() throws Exception {

        Collection<Account> accounts = loadAll();

        double cashBalance = 0;
        for (Account account : accounts) {
            cashBalance += account.getCashBalanceDouble();
        }

        return RoundUtil.getBigDecimal(cashBalance);
    }

    @SuppressWarnings("unchecked")
    protected BigDecimal handleGetSecuritiesCurrentValueAllAccounts() throws Exception {

        Collection<Account> accounts = loadAll();

        double currentValue = 0;
        for (Account account : accounts) {
            currentValue += account.getSecuritiesCurrentValueDouble();
        }

        return RoundUtil.getBigDecimal(currentValue);
    }

    @SuppressWarnings("unchecked")
    protected BigDecimal handleGetMaintenanceMarginAllAccounts() throws Exception {

        Collection<Account> accounts = loadAll();

        double maintenanceMargin = 0;
        for (Account account : accounts) {
            maintenanceMargin += account.getMaintenanceMarginDouble();
        }

        return RoundUtil.getBigDecimal(maintenanceMargin);
    }

    @SuppressWarnings("unchecked")
    protected BigDecimal handleGetNetLiqValueAllAccounts() throws Exception {

        Collection<Account> accounts = loadAll();

        double netLiqValue = 0;
        for (Account account : accounts) {
            netLiqValue += account.getNetLiqValueDouble();
        }

        return RoundUtil.getBigDecimal(netLiqValue);
    }
}
