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
package ch.algotrader.dao.security;

import ch.algotrader.entity.security.ImpliedVolatility;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.hibernate.ReadWriteDao;

/**
 * DAO for {@link ch.algotrader.entity.security.ImpliedVolatility} objects.
 *
 * @see ch.algotrader.entity.security.ImpliedVolatility
 */
public interface ImpliedVolatilityDao extends ReadWriteDao<ImpliedVolatility> {

    /**
     * Finds an ImpliedVolatility by the {@code duration}, {@code delta} and {@code optionType}
     * @param duration
     * @param delta
     * @param type
     * @return ImpliedVolatility
     */
    ImpliedVolatility findByDurationDeltaAndType(Duration duration, double delta, OptionType type);

    /**
     * Finds an ImpliedVolatility by the {@code duration}, {@code moneyness} and {@code optionType}.
     * @param duration
     * @param moneyness
     * @param type
     * @return ImpliedVolatility
     */
    ImpliedVolatility findByDurationMoneynessAndType(Duration duration, double moneyness, OptionType type);

    // spring-dao merge-point

}
