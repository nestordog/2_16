/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.dao.security;

import java.util.Date;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.enumeration.Currency;

/**
 * DAO for {@link ch.algotrader.entity.security.Forex} objects.
 *
 * @see ch.algotrader.entity.security.Forex
 */
public interface ForexDao extends ReadWriteDao<Forex> {

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