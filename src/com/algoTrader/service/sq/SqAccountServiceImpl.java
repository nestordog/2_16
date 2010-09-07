package com.algoTrader.service.sq;

import com.algoTrader.entity.Account;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.PropertiesUtil;

public class SqAccountServiceImpl extends SqAccountServiceBase {

    private static Currency currency = Currency.fromString(PropertiesUtil.getProperty("strategie.currency"));

    protected double[] handleGetAvailableAmountsDouble() throws Exception {

        Account account = getAccountDao().findByCurrency(currency);
        return new double[] { account.getAvailableAmountDouble() };
    }
}
