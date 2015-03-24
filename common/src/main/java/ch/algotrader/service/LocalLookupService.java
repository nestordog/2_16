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
package ch.algotrader.service;

import java.math.BigDecimal;

import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.enumeration.Currency;


/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface LocalLookupService {

    /**
     * returns the currentMarketDataEvent of the specified security by consulting the local Engine first,
     * then the SERVER Engine and then the database
     */
    MarketDataEvent getCurrentMarketDataEvent(int securityId);

    /**
     * returns the current value of the specified security by consulting the local Engine first,
     * then the SERVER Engine and then the database
     */
    BigDecimal getCurrentValue(int securityId);

    /**
     * returns the current value of the specified security by consulting the local Engine first,
     * then the SERVER Engine and then the database
     */
    double getCurrentValueDouble(int securityId);

    /**
     * Gets the current Exchange Rate between the {@code baseCurrency} and {@code
     * transactionCurrency} by consulting the local Engine first, then the SERVER Engine and then the database
     */
    double getForexRate(Currency baseCurrency, Currency transactionCurrency);

    /**
     * Gets the current Exchange Rate between the {@code baseCurrency} and Portfolio Base Currency.
     */
    double getForexRateBase(Currency baseCurrency);

    /**
     * Gets the relevant exchange rate for the specified Security related to the specified {@link Currency}
     */
    double getForexRate(int securityId, Currency transactionCurrency);

    /**
     * Gets the relevant exchange rate for the specified Security related to the Portfolio Base Currency
     */
    double getForexRateBase(int securityId);

    /**
     * Returns true if any {@link ch.algotrader.entity.marketData.MarketDataEvent MarketDataEvent} has been received.
     */
    boolean hasCurrentMarketDataEvents();


}
