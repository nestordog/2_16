package com.algoTrader.entity;

import com.algoTrader.enumeration.Currency;

public class ForexDaoImpl extends ForexDaoBase {

    protected double handleGetRateDouble(Currency baseCurrency, Currency transactionCurrency) {

        if (baseCurrency.equals(transactionCurrency))
            return 1.0;

        Forex forex = find(baseCurrency, transactionCurrency);

        if (forex != null) {

            Tick tick = forex.getLastTick();
            return tick.getCurrentValueDouble();

        } else {

            // reverse lookup
            forex = find(transactionCurrency, baseCurrency);

            if (forex != null) {

                Tick tick = forex.getLastTick();
                return 1.0 / tick.getCurrentValueDouble();

            } else {
                throw new IllegalArgumentException("Forex does not exist: " + transactionCurrency + "." + baseCurrency);
            }
        }
    }
}
