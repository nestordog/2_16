package com.algoTrader.entity.security;

import com.algoTrader.enumeration.Currency;

public class ForexFutureImpl extends ForexFuture {

    private static final long serialVersionUID = 5345554809367362451L;

    @Override
    public Currency getTransactionCurrency() {

        return getSecurityFamily().getCurrency();
    }
}
