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
package ch.algotrader.dao.strategy;

import java.util.List;

import ch.algotrader.dao.ReadWriteDao;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;

/**
 * DAO for {@link ch.algotrader.entity.strategy.CashBalance} objects.
 *
 * @see ch.algotrader.entity.strategy.CashBalance
 */
public interface CashBalanceDao extends ReadWriteDao<CashBalance> {

    /**
     * Finds all {@link CashBalance CashBalances} of the specified Strategy.
     * @param strategyName
     * @return List<CashBalance>
     */
    List<CashBalance> findCashBalancesByStrategy(String strategyName);

    /**
     * Finds a CashBalance by the specified Strategy and Currency and places a database lock on this
     * CashBalance.
     * @param strategy
     * @param currency
     * @return CashBalance
     */
    CashBalance findByStrategyAndCurrency(Strategy strategy, Currency currency);

    /**
     * Finds a CashBalance by the specified Strategy and Currency and places a database lock on this
     * CashBalance.
     * @param strategy
     * @param currency
     * @return CashBalance
     */
    CashBalance findByStrategyAndCurrencyLocked(Strategy strategy, Currency currency);

    /**
     * Returns all {@link Currency Currencies} used by the System
     * @return List<Currency>
     */
    List<Currency> findHeldCurrencies();

    /**
     * Returns all {@link Currency Currencies} used by the specified Strategy
     * @param strategyName
     * @return List<Currency>
     */
    List<Currency> findHeldCurrenciesByStrategy(String strategyName);

    // spring-dao merge-point
}
