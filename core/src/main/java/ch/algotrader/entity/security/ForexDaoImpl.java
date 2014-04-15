/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.entity.security;

import java.util.Date;
import java.util.List;

import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.ForexDaoBase;
import ch.algotrader.enumeration.Currency;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ForexDaoImpl extends ForexDaoBase {

    private static final int intervalDays = 4;

    @Override
    protected double handleGetRateDouble(Currency baseCurrency, Currency transactionCurrency) {

        if (baseCurrency.equals(transactionCurrency)) {
            return 1.0;
        }

        Forex forex = getForex(baseCurrency, transactionCurrency);

        MarketDataEvent marketDataEvent = forex.getCurrentMarketDataEvent();

        if (marketDataEvent == null) {
            throw new IllegalStateException("cannot get exchangeRate for " + baseCurrency + "." + transactionCurrency + " because no marketDataEvent is available");
        }

        if (forex.getBaseCurrency().equals(baseCurrency)) {
            // expected case
            return marketDataEvent.getCurrentValueDouble();
        } else {
            // reverse case
            return 1.0 / marketDataEvent.getCurrentValueDouble();
        }
    }

    @Override
    protected double handleGetRateDoubleByDate(Currency baseCurrency, Currency transactionCurrency, Date date) throws Exception {

        if (baseCurrency.equals(transactionCurrency)) {
            return 1.0;
        }

        Forex forex = getForex(baseCurrency, transactionCurrency);

        List<Tick> ticks = getTickDao().findTicksBySecurityAndMaxDate(1, 1, forex.getId(), date, intervalDays);
        if (ticks.isEmpty()) {
            throw new IllegalStateException("cannot get exchangeRate for " + baseCurrency + "." + transactionCurrency + " because no last tick is available for date " + date);
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
