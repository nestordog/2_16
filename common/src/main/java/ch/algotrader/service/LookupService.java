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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.BaseEntityI;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.security.EasyToBorrow;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.IntrestRate;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface LookupService {

    /**
     * Gets a Security by its {@code id}.
     */
    public Security getSecurity(long id);

    /**
     * Gets a Security by its {@code isin}.
     */
    public Security getSecurityByIsin(String isin);

    /**
     * Gets a Security by its {@code symbol}.
     */
    public Security getSecurityBySymbol(String symbol);

    /**
     * Gets a Security by its {@code bbgid}.
     */
    public Security getSecurityByBbgid(String bbgid);

    /**
     * Gets a Security by its {@code ric}.
     */
    public Security getSecurityByRic(String ric);

    /**
     * Gets a Security by its {@code conid}.
     */
    public Security getSecurityByConid(String conid);

    /**
     * Gets a Security by its {@code id} incl. In addition the corresponding {@link SecurityFamily}
     * and Underlying {@link Security} are initialized.
     */
    public Security getSecurityInclFamilyAndUnderlying(long id);

    /**
     * Gets multiple Securities by their {@code ids}.
     */
    public List<Security> getSecuritiesByIds(Collection<Long> ids);

    /**
     * Returns all Securities that are defined in the system.
     */
    public Collection<Security> getAllSecurities();

     /**
     * Gets {@link SecurityFamily} associated with the security with the given id
     */
    public SecurityFamily getSecurityFamilyBySecurity(long securityId);

     /**
     * Get {@link Exchange} associated with the security with the given id
     */
    public Exchange getExchangeBySecurity(long securityId);

    /**
     * Returns a Security that is referenced by another Security and a reference name
     */
    public Security getSecurityReferenceTargetByOwnerAndName(long securityId, String name);

    /**
     * Gets all Securities that are subscribed by at least one Strategy which is marked as {@code
     * autoActive}.
     */
    public Collection<Security> getSubscribedSecuritiesForAutoActivateStrategies();

    /**
     * Gets all subscribed Securities and corresponding {@link ch.algotrader.enumeration.FeedType}
     * for Strategies that are marked {@code autoActivate}. If corresponding Securities are
     * Combinations, all Components will be initialized as well. The return value is a List of Maps
     * (containing key-value-pairs)
     */
    public List<Map> getSubscribedSecuritiesAndFeedTypeForAutoActivateStrategiesInclComponents();

    public Collection<Stock> getStocksBySector(String code);

    public Collection<Stock> getStocksByIndustryGroup(String code);

    public Collection<Stock> getStocksByIndustry(String code);

    public Collection<Stock> getStocksBySubIndustry(String code);

    /**
     * Gets all Options that are subscribed by at least one Strategy.
     */
    public List<Option> getSubscribedOptions();

    /**
     * Gets all Futures that are subscribed by at least one Strategy.
     */
    public List<Future> getSubscribedFutures();

    /**
     * Gets Combinations that are subscribed by the specified Strategy.
     */
    public Collection<Combination> getSubscribedCombinationsByStrategy(String strategyName);

    /**
     * Gets Combinations that are subscribed by the specified Strategy and have an Underlying
     * corresponding to {@code underlyingId}
     */
    public Collection<Combination> getSubscribedCombinationsByStrategyAndUnderlying(String strategyName, long underlyingId);

    /**
     * Gets Combinations that are subscribed by the specified Strategy and have a Component with the
     * specified {@code securityId}
     */
    public Collection<Combination> getSubscribedCombinationsByStrategyAndComponent(String strategyName, long securityId);

    /**
     * Gets Combinations that are subscribed by the specified Strategy and have a Component with the
     * specified Security Type.
     */
    public Collection<Combination> getSubscribedCombinationsByStrategyAndComponentClass(String strategyName, Class<?> type);

    /**
     * Gets all Components where the Combination is subscribed by the defined Strategy.  In addition
     * the Security and Combination are initialized.
     */
    public Collection<Component> getSubscribedComponentsByStrategyInclSecurity(String strategyName);

    /**
     * Gets all Components where the Combination is subscribed by at least one Strategy and where
     * the Security is of the specified {@code securityId}.  In addition the Security and
     * Combination are initialized.
     */
    public Collection<Component> getSubscribedComponentsBySecurityInclSecurity(long securityId);

    /**
     * Gets all Components where the Combination is subscribed by the defined Strategy and where the
     * Security is of the specified {@code securityId}.  In addition the Security and Combination
     * are initialized.
     */
    public Collection<Component> getSubscribedComponentsByStrategyAndSecurityInclSecurity(String strategyName, long securityId);

    /**
     * Gets a Subscriptions by the defined {@code strategyName} and {@code securityId}.
     */
    public Subscription getSubscriptionByStrategyAndSecurity(String strategyName, long securityId);

    /**
     * Gets all Subscriptions by the defined {@code strategyName}. If corresponding Securities are
     * Combinations, all Components will be initialized as well. In additional all Properties are initialized
     */
    public List<Subscription> getSubscriptionsByStrategyInclComponentsAndProps(String strategyName);

    /**
     * Gets Subscriptions for the specified Strategy that do not have any open {@link Position
     * Positions}
     */
    public Collection<Subscription> getNonPositionSubscriptions(String strategyName);

    /**
     * Gets all Strategies.
     */
    public Collection<Strategy> getAllStrategies();

    /**
     * Gets a Strategy by its {@code id}.
     */
    public Strategy getStrategy(long id);

    /**
     * Gets a Strategy by its {@code strategyName}.
     */
    public Strategy getStrategyByName(String name);

    /**
     * Gets a {@code SecurityFamily} by its {@code id}.
     */
    public SecurityFamily getSecurityFamily(long id);

    /**
     * Gets a {@code SecurityFamily} by its {@code id}
     */
    public SecurityFamily getSecurityFamilyByName(String name);

    /**
     * Gets a {@link OptionFamily} by the {@code id} of the Underlying {@link Security}.
     */
    public OptionFamily getOptionFamilyByUnderlying(long id);

    /**
     * Gets a {@link FutureFamily} by the {@code id} of the Underlying {@link Security}.
     */
    public FutureFamily getFutureFamilyByUnderlying(long id);

    /**
     * Gets a {@link Exchange} by the {@code name}
     */
    public Exchange getExchangeByName(String name);

    /**
     * Gets all Positions.
     */
    public Collection<Position> getAllPositions();

    /**
     * Gets a Position by its {@code id}.
     */
    public Position getPosition(long id);

    /**
     * Gets a Position by its {@code id}. In addition its Security and {@link SecurityFamily} will
     * be initialized.
     */
    public Position getPositionInclSecurityAndSecurityFamily(long id);

    /**
     * Gets all Positions of a Strategy defined by {@code strategyName}.
     */
    public List<Position> getPositionsByStrategy(String strategyName);

    /**
     * Gets a Position by Security and Strategy.
     */
    public Position getPositionBySecurityAndStrategy(long securityId, String strategyName);

    /**
     * Gets all open Position (with a quantity != 0).
     */
    public List<Position> getOpenPositions();

    /**
     * Gets open Positions for tradeable Securities
     */
    public List<Position> getOpenTradeablePositions();

    /**
     * Gets open Positions for the specified Strategy.
     */
    public List<Position> getOpenPositionsByStrategy(String strategyName);

    /**
     * Gets open Positions of the specified Strategy for tradeable Securities
     */
    public List<Position> getOpenTradeablePositionsByStrategy(String strategyName);

    /**
     * Gets open Positions for the specified Security
     */
    public List<Position> getOpenPositionsBySecurity(long securityId);

    /**
     * Gets open Positions for the specified Strategy and SecurityType.
     */
    public List<Position> getOpenPositionsByStrategyAndType(String strategyName, Class<? extends BaseEntityI> type);

    /**
     * Gets open Positions for the specified Strategy and SecurityType.
     */
    public List<Position> getOpenPositionsByStrategyTypeAndUnderlyingType(String strategyName, Class<? extends BaseEntityI> type, Class<? extends BaseEntityI> underlyingType);

    /**
     * Gets open Positions for the specified Strategy and SecurityFamily.
     */
    public List<Position> getOpenPositionsByStrategyAndSecurityFamily(String strategyName, long securityFamilyId);

    /**
     * Gets open Forex Positions
     */
    public List<Position> getOpenFXPositions();

    /**
     * Gets open Forex Positions of the specified Strategy
     */
    public List<Position> getOpenFXPositionsByStrategy(String strategyName);

    /**
     * Gets a Transaction by its {@code id}.
     */
    public Transaction getTransaction(long id);

    /**
     * Finds all Transactions of the current day in descending {@code dateTime} order.
     */
    public List<Transaction> getDailyTransactionsDesc();

    /**
     * Finds all Transactions of the current day of a specific Strategy in descending {@code dateTime} order.
     */
    public List<Transaction> getDailyTransactionsByStrategyDesc(String strategyName);

    /**
     * Gets an Account by its {@code accountName}.
     */
    public Account getAccountByName(String accountName);

    /**
     * Gets all active Accounts for the specified {@link OrderServiceType}.
     */
    public Collection<String> getActiveSessionsByOrderServiceType(OrderServiceType orderServiceType);

    /**
     * Gets the last Tick for the specified Security that is within the last {@code n}
     * (specified by the config param {@code intervalDays}) days before {@code dateTime}. In addition the Security is initialized.
     * @param intervalDays TODO
     */
    public Tick getLastTick(long securityId, Date dateTime, int intervalDays);

    /**
     * Gets all Ticks of the defined Security that are before the {@code maxDate} and after {@code
     * minDate} - {@code intervalDays}
     * @param intervalDays TODO
     */
    public List<Tick> getTicksByMaxDate(long securityId, Date maxDate, int intervalDays);

    /**
     * Gets all Ticks of the defined Security that are after the {@code minDate} and before {@code
     * minDate} + {@code intervalDays}
     * @param intervalDays TODO
     */
    public List<Tick> getTicksByMinDate(long securityId, Date minDate, int intervalDays);

    /**
     * Gets one Tick-Id per day of the defined Security that is just before the specified {@code
     * time}.
     */
    public List<Tick> getDailyTicksBeforeTime(long securityId, Date time);

    /**
     * Gets one Tick-Id per day of the defined Security that is just after the specified {@code
     * time}.
     */
    public List<Tick> getDailyTicksAfterTime(long securityId, Date time);

    /**
     * Gets one Tick-Id per hour of the defined Security that is just before the specified number of
     * {@code minutes} and after the specified {@code minDate}.
     */
    public List<Tick> getHourlyTicksBeforeMinutesByMinDate(long securityId, int minutes, Date minDate);

    /**
     * Gets one Tick-Id per hour of the defined Security that is just after the specified number of
     * {@code minutes} and after the specified {@code minDate}.
     */
    public List<Tick> getHourlyTicksAfterMinutesByMinDate(long securityId, int minutes, Date minDate);

    /**
     * Gets the first Tick of the defined Security that is before the maxDate (but not earlier than
     * one minute before that the maxDate).
     */
    public Tick getTickBySecurityAndMaxDate(long securityId, Date date);

    /**
     * Gets daily Bars created based on Ticks between {@code minDate} and {@code maxDate}
     */
    public List<Bar> getDailyBarsFromTicks(long securityId, Date fromDate, Date toDate);

    /**
     * Returns the last nl Bars of the specified Security and {@code barSize}
     */
    public List<Bar> getLastNBarsBySecurityAndBarSize(int n, long securityId, Duration barSize);

    /**
     * Returns the Bars of the specified Security {@code barSize} after the specified {@code
     * minDate}
     */
    public List<Bar> getBarsBySecurityBarSizeAndMinDate(long securityId, Duration barSize, Date minDate);

    /**
     * Gets the Forex associated with the{@code baseCurrency} and {@code transactionCurrency}.
     */
    public Forex getForex(Currency baseCurrency, Currency transactionCurrency);

    /**
     * Gets the historical Exchange Rate between the {@code baseCurrency} and {@code
     * transactionCurrency} on the specified {@code date}
     */
    public double getForexRateByDate(Currency baseCurrency, Currency transactionCurrency, Date date);

    /**
     * Gets the Interest Rate for the {@code currency} and {@code duration}.
     */
    public IntrestRate getInterestRateByCurrencyAndDuration(Currency currency, Duration duration);

    /**
     * Gets the Interest Rate for the {@code currency} and {@code duration} on the specified {@code
     * date}
     */
    public double getInterestRateByCurrencyDurationAndDate(Currency currency, Duration duration, Date date);

    /**
     * Gets all {@link Currency Currencies} used by the System
     */
    public Collection<Currency> getHeldCurrencies();

    /**
     * Gets all {@link CashBalance CashBalances} of the specified Strategy.
     */
    public Collection<CashBalance> getCashBalancesByStrategy(String strategyName);

    /**
     * Gets all Measurements before the specified Date with the specified name
     */
    public Map<Date, Object> getMeasurementsByMaxDate(String strategyName, String name, Date maxDate);

    /**
     * Gets all Measurements before the specified Date
     */
    public Map<Date, Map<String, Object>> getAllMeasurementsByMaxDate(String strategyName, Date maxDate);

    /**
     * Gets all Measurements after the specified Date with the specified name
     */
    public Map<Date, Object> getMeasurementsByMinDate(String strategyName, String name, Date minDate);

    /**
     * Gets all Measurements after the specified Date
     */
    public Map<Date, Map<String, Object>> getAllMeasurementsByMinDate(String strategyName, Date minDate);

    /**
     * Gets the first Measurement before the specified Date with the specified name
     */
    public Object getMeasurementByMaxDate(String strategyName, String name, Date maxDate);

    /**
     * Gets the first Measurement after the specified Date with the specified name
     */
    public Object getMeasurementByMinDate(String strategyName, String name, Date minDate);

    public Collection<EasyToBorrow> getEasyToBorrowByDateAndBroker(Date date, Broker broker);

    /**
     * Gets the current Esper-Time of the System.
     */
    public Date getCurrentDBTime();

    /**
     * Retrieves an arbitrary list of Entities or values based on a Hibernate query.
     * In addition named parameters can be passed. Example:
     * from Strategy where name = :strategyName
     * and a NamedParam with {@code name='strategyName'} and {@code value='SERVER'}
     */
    public <T> List<T> get(Class<T> clazz, String query, QueryType type, NamedParam... namedParams);

    /**
     * Retrieves an arbitrary list of Entities or values based on a Hibernate query.
     * In addition named parameters can be passed. Example:
     * from Strategy where name = :strategyName
     * and a NamedParam with {@code name='strategyName'} and {@code value='SERVER'}
     */
    public <T> List<T> get(Class<T> clazz, String query, int maxResults, QueryType type, NamedParam... namedParams);

    /**
     * Retrieves a unique Object based on a Hibernate query.
     * In addition named parameters can be passed. Example:
     * from Strategy where name = :strategyName
     * and a NamedParam with {@code name='strategyName'} and {@code value='SERVER'}
     */
    public <T> T getUnique(Class<T> clazz, String query, QueryType type, NamedParam... namedParams);


}
