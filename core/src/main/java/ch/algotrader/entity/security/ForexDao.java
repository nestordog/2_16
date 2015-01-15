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

import ch.algotrader.enumeration.Currency;
import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.security.Forex} objects.
 *
 * @see ch.algotrader.entity.security.Forex
 */
public interface ForexDao extends ReadWriteDao<Forex> {

    /**
     * Gets the current Exchange Rate between the {@code baseCurrency} and {@code
     * transactionCurrency}.
     * @param baseCurrency
     * @param transactionCurrency
     * @return double
     */
    double getRateDouble(Currency baseCurrency, Currency transactionCurrency);

    /**
     * Gets the historical Exchange Rate between the {@code baseCurrency} and {@code
     * transactionCurrency} on the specified {@code date}
     * @param baseCurrency
     * @param transactionCurrency
     * @param date
     * @return double
     */
    double getRateDoubleByDate(Currency baseCurrency, Currency transactionCurrency, Date date);

    /**
     * Gets the Forex defined by the {@code baseCurrency} and {@code transactionCurrency}. If the
     * Forex was not found, the method will also issue a lookup with {@code baseCurrency} and {@code
     * transactionCurrency} reversed.
     * @param baseCurrency
     * @param transactionCurrency
     * @return Forex
     */
    Forex getForex(Currency baseCurrency, Currency transactionCurrency);

    // spring-dao merge-point
}