package com.algoTrader.entity.security;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.enumeration.Currency;

public class ForexDaoImpl extends ForexDaoBase {

    @Override
    protected double handleGetRateDouble(Currency baseCurrency, Currency transactionCurrency) {

        if (baseCurrency.equals(transactionCurrency)) {
            return 1.0;
        }

        Forex forex = getForex(baseCurrency, transactionCurrency);

        Tick tick = forex.getLastTick();

        if (tick == null) {
            throw new RuntimeException("cannot get exchangeRate for " + baseCurrency + "." + transactionCurrency + " because no last tick is available");
        }

        if (forex.getBaseCurrency().equals(baseCurrency)) {
            // expected case
            return tick.getCurrentValueDouble();
        } else {
            // reverse case
            return 1.0 / tick.getCurrentValueDouble();
        }
    }

    @Override
    protected Forex handleGetForex(Currency baseCurrency, Currency transactionCurrency) {

        Forex forex = find(baseCurrency, transactionCurrency);

        if (forex == null) {

            // reverse lookup
            forex = find(transactionCurrency, baseCurrency);

            if (forex == null) {
                throw new IllegalArgumentException("Forex does not exist: " + transactionCurrency + "." + baseCurrency);
            }
        }

        return forex;
    }
}
