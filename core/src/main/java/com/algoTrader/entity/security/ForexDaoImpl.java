package com.algoTrader.entity.security;

import java.util.Date;
import java.util.List;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.enumeration.Currency;

public class ForexDaoImpl extends ForexDaoBase {

    private static final int intervalDays = 3;

    @Override
    protected double handleGetRateDouble(Currency baseCurrency, Currency transactionCurrency) {

        if (baseCurrency.equals(transactionCurrency)) {
            return 1.0;
        }

        Forex forex = getForex(baseCurrency, transactionCurrency);

        Tick tick = forex.getLastTick();

        if (tick == null) {
            throw new IllegalStateException("cannot get exchangeRate for " + baseCurrency + "." + transactionCurrency + " because no last tick is available");
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
    protected double handleGetRateDoubleByDate(Currency baseCurrency, Currency transactionCurrency, Date date) throws Exception {

        if (baseCurrency.equals(transactionCurrency)) {
            return 1.0;
        }

        Forex forex = getForex(baseCurrency, transactionCurrency);

        List<Tick> ticks = getTickDao().findTicksForSecurityAndMaxDate(1, 1, forex.getId(), date, intervalDays);
        if (ticks.isEmpty()) {
            throw new IllegalStateException("cannot get exchangeRate for " + baseCurrency + "." + transactionCurrency + " because no last tick is available");
        }

        Tick tick = ticks.get(0);
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

        Forex forex = findByBaseAndTransactionCurrency(baseCurrency, transactionCurrency);

        if (forex == null) {

            // reverse lookup
            forex = findByBaseAndTransactionCurrency(transactionCurrency, baseCurrency);

            if (forex == null) {
                throw new IllegalArgumentException("Forex does not exist: " + transactionCurrency + "." + baseCurrency);
            }
        }

        return forex;
    }
}
