package com.algoTrader.entity.security;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.security.ForexDaoBase;
import com.algoTrader.enumeration.Currency;

public class ForexDaoImpl extends ForexDaoBase {

    protected double handleGetRateDouble(Currency baseCurrency, Currency transactionCurrency) {

        if (baseCurrency.equals(transactionCurrency))
            return 1.0;

        Forex forex = find(baseCurrency, transactionCurrency);

        if (forex != null) {

            Tick tick = forex.getLastTick();

            if (tick == null) {
                throw new RuntimeException("cannot get exchangeRate for " + baseCurrency + "." + transactionCurrency + " because no last tick is available");
            }

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
