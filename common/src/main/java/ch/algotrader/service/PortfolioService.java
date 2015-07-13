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
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.vo.BalanceVO;
import ch.algotrader.vo.PortfolioValueVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface PortfolioService {

    /**
     * Gets the Cash Balance of the entire System.
     */
    public BigDecimal getCashBalance();

    /**
     * Gets the Cash Balance of the specified Strategy.
     */
    public BigDecimal getCashBalance(String strategyName);

    /**
     * Gets the Cash Balance of the entire System on the specified Date.
     */
    public BigDecimal getCashBalance(Date date);

    /**
     * Gets the Cash Balance of the specified Strategy on the specified Date.
     */
    public BigDecimal getCashBalance(String strategyName, Date date);

    /**
     * Gets the Cash Balance on the specified Date by an arbitrary filter by aggregating all
     * relevant transactions.
     * The variable {@code t} can be used to reference the Transaction.
     * Examples:
     * {@code
     * t.account.name = 'IB_NATIVE_TEST'
     * t.account.name = :accountName // //specifying 'accountName' as a namedParameter
     * t.currency = 'USD'
     * }
     * <i>Note: The current value of {@link ch.algotrader.entity.security.Forex} Positions will not
     * be taken into account</i>
     */
    public BigDecimal getCashBalance(String filter, Map namedParameters, Date date);

    /**
     * Gets the Cash Balance of the entire System.
     */
    public double getCashBalanceDouble();

    /**
     * Gets the Cash Balance of the specified Strategy.
     */
    public double getCashBalanceDouble(String strategyName);

    /**
     * Gets the Cash Balance of the entire System on the specified Date.
     */
    public double getCashBalanceDouble(Date date);

    /**
     * Gets the Cash Balance of the specified Strategy on the specified Date.
     */
    public double getCashBalanceDouble(String strategyName, Date date);

    /**
     * Gets the Cash Balance on the specified Date by an arbitrary filter by aggregating all
     * relevant transactions.
     * The variable {@code t} can be used to reference the Transaction.
     * Examples:
     * {@code
     * t.account.name = 'IB_NATIVE_TEST'
     * t.account.name = :accountName // //specifying 'accountName' as a namedParameter
     * t.currency = 'USD'
     * }
     * <i>Note: The current value of {@link ch.algotrader.entity.security.Forex} Positions will not
     * be taken into account</i>
     */
    public double getCashBalanceDouble(String filter, Map namedParameters, Date date);

    /**
     * Gets the total Market Value of all Positions of the entire System.
     */
    public BigDecimal getSecuritiesCurrentValue();

    /**
     * Gets the total Market Value of all Positions of the specified Strategy.
     */
    public BigDecimal getSecuritiesCurrentValue(String strategyName);

    /**
     * Gets the total Market Value of all Positions of the entire System on the specified Date.
     */
    public BigDecimal getSecuritiesCurrentValue(Date date);

    /**
     * Gets the total Market Value of all Positions of the specified Strategy on the specified Date.
     */
    public BigDecimal getSecuritiesCurrentValue(String strategyName, Date date);

    /**
     * Gets the total Market Value of all Positions on the specified Date by an arbitrary filter by
     * aggregating all relevant transactions.
     * The variable {@code s} and {@code t} can be used to reference the Security and Transaction.
     * Examples:
     * {@code
     * s.symbol = 'IBM'
     * s.symbol = :symbol //specifying 'symbol' as a namedParameter
     * s.class = ForexImpl
     * s.securityFamily.currency = 'USD'
     * s.baseCurrency = 'EUR'
     * s.gics like '12______'
     * }
     */
    public BigDecimal getSecuritiesCurrentValue(String filter, Map namedParameters, Date date);

    /**
     * Gets the total Market Value of all Positions of the entire System.
     */
    public double getSecuritiesCurrentValueDouble();

    /**
     * Gets the total Market Value of all Positions of the specified Strategy.
     */
    public double getSecuritiesCurrentValueDouble(String strategyName);

    /**
     * Gets the total Market Value of all Positions of the entire System on the specified Date.
     */
    public double getSecuritiesCurrentValueDouble(Date date);

    /**
     * Gets the total Market Value of all Positions of the specified Strategy on the specified Date.
     */
    public double getSecuritiesCurrentValueDouble(String strategyName, Date date);

    /**
     * Gets the total Market Value of all Positions on the specified Date by an arbitrary filter by
     * aggregating all relevant transactions.
     * The variable {@code s} and {@code t} can be used to reference the Security and Transaction.
     * Examples:
     * {@code
     * s.symbol = 'IBM'
     * s.symbol = :symbol //specifying 'symbol' as a namedParameter
     * s.class = ForexImpl
     * s.securityFamily.currency = 'USD'
     * s.baseCurrency = 'EUR'
     * s.gics like '12______'
     * }
     */
    public double getSecuritiesCurrentValueDouble(String filter, Map namedParameters, Date date);

    /**
     * Gets the Net-Liquidation-Value of the entire System.
     */
    public BigDecimal getNetLiqValue();

    /**
     * Gets the Net-Liquidation-Value of the specified Strategy.
     */
    public BigDecimal getNetLiqValue(String strategyName);

    /**
     * Gets the Net-Liquidation-Value of the entire System.
     */
    public double getNetLiqValueDouble();

    /**
     * Gets the Net-Liquidation-Value of the specified Strategy.
     */
    public double getNetLiqValueDouble(String strategyName);

    /**
     * Gets the current Leverage of the entire System.
     */
    public double getLeverage();

    /**
     * Gets the current Leverage of the specified Strategy.
     */
    public double getLeverage(String strategyName);

    /**
     * Gets the performance since the beginning of the month of the entire System
     */
    public double getPerformance();

    /**
     * Gets the performance since the beginning of the month of the specified Strategy
     */
    public double getPerformance(String strategyName);

    /**
     * Gets the {@link PortfolioValue} of the entire System.
     */
    public PortfolioValue getPortfolioValue();

    /**
     * Gets the {@link PortfolioValue} of the specified Strategy.
     */
    public PortfolioValue getPortfolioValue(String strategyName);

    /**
     * Gets the {@link PortfolioValue} of the specified Strategy on the specified Date.
     */
    public PortfolioValue getPortfolioValue(String strategyName, Date date);

    /**
     * Gets the {@link PortfolioValue PortfolioValues} including the Performance of the specified
     * Strategy since the specified Date.
     */
    public Collection<PortfolioValueVO> getPortfolioValuesInclPerformanceSinceDate(String strategyName, Date date);

    /**
     * Gets the {@link BalanceVO CashBalances} of the entire system.
     */
    public Collection<BalanceVO> getBalances();

    /**
     * Gets the {@link BalanceVO CashBalances} for the specified Strategy.
     */
    public Collection<BalanceVO> getBalances(String strategyName);

    /**
     * Saves current Portfolio Values as a consequence for a performance relevant Transaction. See
     * {@link ch.algotrader.entity.Transaction#isPerformanceRelevant}. If there have been
     * PortfolioValues created since this Transaction, they are recreated (including PortfolioValues
     * of the AlgoTrader Server).
     */
    public void savePortfolioValue(Transaction transaction);
    /**
     * Saves current Portfolio Values for all Strategies marked as {@code autoActivate}
     */
    public void savePortfolioValues();

    /**
     * Restores all PortfolioValues of the specified Strategy after the {@code fromDate} up to and
     * including the {@code toDate}.
     */
    public void restorePortfolioValues(Strategy strategy, Date fromDate, Date toDate);

    /**
     * Prints portfolio values.
     */
    void printPortfolioValue(final PortfolioValue portfolioValue);

}
