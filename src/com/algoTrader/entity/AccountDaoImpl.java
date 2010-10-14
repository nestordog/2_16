package com.algoTrader.entity;

import java.util.Collection;

public class AccountDaoImpl extends AccountDaoBase {

    @SuppressWarnings("unchecked")
    protected double handleGetCashBalanceAllAccountsDouble() throws Exception {

        Collection<Account> accounts = loadAll();

        double cashBalance = 0;
        for (Account account : accounts) {
            cashBalance += account.getCashBalanceDouble();
        }

        return cashBalance;
    }

    @SuppressWarnings("unchecked")
    protected double handleGetSecuritiesCurrentValueAllAccountsDouble() throws Exception {

        Collection<Account> accounts = loadAll();

        double currentValue = 0;
        for (Account account : accounts) {
            currentValue += account.getSecuritiesCurrentValueDouble();
        }

        return currentValue;
    }

    @SuppressWarnings("unchecked")
    protected double handleGetMaintenanceMarginAllAccountsDouble() throws Exception {

        Collection<Account> accounts = loadAll();

        double maintenanceMargin = 0;
        for (Account account : accounts) {
            maintenanceMargin += account.getMaintenanceMarginDouble();
        }

        return maintenanceMargin;
    }

    @SuppressWarnings("unchecked")
    protected double handleGetNetLiqValueAllAccountsDouble() throws Exception {

        Collection<Account> accounts = loadAll();

        double netLiqValue = 0;
        for (Account account : accounts) {
            netLiqValue += account.getNetLiqValueDouble();
        }

        return netLiqValue;
    }
}
