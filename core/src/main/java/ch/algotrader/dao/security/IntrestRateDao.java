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

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.security.IntrestRate;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;

/**
 * DAO for {@link ch.algotrader.entity.security.IntrestRate} objects.
 *
 * @see ch.algotrader.entity.security.IntrestRate
 */
public interface IntrestRateDao  extends ReadWriteDao<IntrestRate> {

    /**
     * Finds an IntrestRate by {@code currency} and {@code duration}.
     * @param currency
     * @param duration
     * @return IntrestRate
     */
    IntrestRate findByCurrencyAndDuration(Currency currency, Duration duration);

    // spring-dao merge-point
}