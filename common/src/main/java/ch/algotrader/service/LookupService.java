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
import java.util.List;
import java.util.Map;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.Transaction;
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
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.vo.OrderStatusVO;
import ch.algotrader.vo.PositionVO;
import ch.algotrader.vo.TransactionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface LookupService {

    /**
     * Gets a Security by its {@code id}.
     */
    public Security getSecurity(int id);

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
    public Security getSecurityInclFamilyAndUnderlying(int id);

    /**
     * Gets a Security by its {@code id} and initializes {@link Subscription Subscriptions}, {@link
     * Position Positions}, Underlying {@link Security} and {@link SecurityFamily} to make sure that
     * they are available when the Hibernate Session is closed and this Security is in a detached
     * state.
     */
    public Security getSecurityInitialized(int id);

    /**
     * Gets multiple Securities by their {@code ids}.
     */
    public List<Security> getSecuritiesByIds(Collection<Integer> ids);

    /**
     * Gets the securityId by the specified securityString, by checking fields
     * in the following order:<br>
     * <ul>
     * <li>symbol</li>
     * <li>isin</li>
     * <li>bbgid</li>
     * <li>ric</li>
     * <li>conid</li>
     * <li>id</li>
     * </ul>
     */
    public int getSecurityIdBySecurityString(String securityString);

    /**
     * Returns all Securities that are defined in the system.
     */
    public Collection<Security> getAllSecurities();

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
    public List getSubscribedSecuritiesAndFeedTypeForAutoActivateStrategiesInclComponents();

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
    public Collection<Combination> getSubscribedCombinationsByStrategyAndUnderlying(String strategyName, int underlyingId);

    /**
     * Gets Combinations that are subscribed by the specified Strategy and have a Component with the
     * specified {@code securityId}
     */
    public Collection<Combination> getSubscribedCombinationsByStrategyAndComponent(String strategyName, int securityId);

    /**
     * Gets Combinations that are subscribed by the specified Strategy and have a Component with the
     * specified Security Type.
     */
    public Collection<Combination> getSubscribedCombinationsByStrategyAndComponentClass(String strategyName, Class type);

    /**
     * Gets Combinations that are subscribed by the specified Strategy, have a Component with the
     * specified Security Type and a Component quantity of zero.
     */
    public Collection<Combination> getSubscribedCombinationsByStrategyAndComponentClassWithZeroQty(String strategyName, Class type);

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
    public Collection<Component> getSubscribedComponentsBySecurityInclSecurity(int securityId);

    /**
     * Gets all Components where the Combination is subscribed by the defined Strategy and where the
     * Security is of the specified {@code securityId}.  In addition the Security and Combination
     * are initialized.
     */
    public Collection<Component> getSubscribedComponentsByStrategyAndSecurityInclSecurity(String strategyName, int securityId);

    /**
     * Gets a Subscriptions by the defined {@code strategyName} and {@code securityId}.
     */
    public Subscription getSubscriptionByStrategyAndSecurity(String strategyName, int securityId);

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
    public Strategy getStrategy(int id);

    /**
     * Gets a Strategy by its {@code strategyName}.
     */
    public Strategy getStrategyByName(String name);

    /**
     * Gets all Strategies which are marked as {@code autoActivate}
     */
    public Collection<Strategy> getAutoActivateStrategies();

    /**
     * Gets a {@code SecurityFamily} by its {@code id}.
     */
    public SecurityFamily getSecurityFamily(int id);

    /**
     * Gets a {@code SecurityFamily} by its {@code id}
     */
    public SecurityFamily getSecurityFamilyByName(String name);

    /**
     * Gets a {@link OptionFamily} by the {@code id} of the Underlying {@link Security}.
     */
    public OptionFamily getOptionFamilyByUnderlying(int id);

    /**
     * Gets a {@link FutureFamily} by the {@code id} of the Underlying {@link Security}.
     */
    public FutureFamily getFutureFamilyByUnderlying(int id);

    /**
     * Gets all Positions.
     */
    public Collection<Position> getAllPositions();

    /**
     * Gets a Position by its {@code id}.
     */
    public Position getPosition(int id);

    /**
     * Gets a Position by its {@code id}. In addition its Security and {@link SecurityFamily} will
     * be initialized.
     */
    public Position getPositionInclSecurityAndSecurityFamily(int id);

    /**
     * Gets all Positions of a Strategy defined by {@code strategyName}.
     */
    public List<Position> getPositionsByStrategy(String strategyName);

    /**
     * Gets a Position by Security and Strategy.
     */
    public Position getPositionBySecurityAndStrategy(int securityId, String strategyName);

    /**
     * Gets {@link ch.algotrader.vo.OpenPositionVO OpenPositionVOs} corresponding to the specified
     * Strategy. If {@code displayClosedPositions} is selected, Positions will also be displayed if
     * they are (currently) closed (qty=0).
     */
    public List<PositionVO> getPositionsVO(String strategyName, boolean openPositions);

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
    public List<Position> getOpenPositionsBySecurity(int securityId);

    /**
     * Gets open Positions for the specified Strategy and SecurityType.
     */
    public List<Position> getOpenPositionsByStrategyAndType(String strategyName, Class type);

    /**
     * Gets open Positions for the specified Strategy and SecurityType.
     */
    public List<Position> getOpenPositionsByStrategyTypeAndUnderlyingType(String strategyName, Class type, Class underlyingType);

    /**
     * Gets open Positions for the specified Strategy and SecurityFamily.
     */
    public List<Position> getOpenPositionsByStrategyAndSecurityFamily(String strategyName, int securityFamilyId);

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
    public Transaction getTransaction(int id);

    /**
     * Gets all Trades (either  {@code BUY} or {@code SELL}) between {@code minDate} and {@code
     * maxDate}
     */
    public List<Transaction> getTradesByMinDateAndMaxDate(Date minDate, Date maxDate);

    /**
     * Gets {@link TransactionVO TransactionVOs} corresponding to the specified Strategy. Only the
     * latest {@code transactionDisplayCount} will be returned.
     */
    public List<TransactionVO> getTransactionsVO(String strategyName);

    /**
     * Gets all {@link Order open Orders} for to the specified Strategy.
     */
    public Collection<Order> getOpenOrdersByStrategy(String strategyName);

    /**
     * Gets all {@link Order open Orders} for to the specified Strategy and Security.
     */
    public Collection<Order> getOpenOrdersByStrategyAndSecurity(String strategyName, int securityId);

    /**
     * Gets an open order by its {@code intId} by querying the OpenOrderWindow
     */
    public Order getOpenOrderByIntId(String intId);

    /**
     * Gets an open order by its {@code rootIntId} by querying the OpenOrderWindow
     */
    public Order getOpenOrderByRootIntId(String intId);

    /**
     * Gets an open order by its {@code extId} by querying the OpenOrderWindow
     */
    public Order getOpenOrderByExtId(String extId);

    /**
     * Gets all {@link OrderStatusVO OrderStatusVOs} for open Orders corresponding to the specified
     * Strategy.
     */
    public Collection<OrderStatusVO> getOpenOrdersVOByStrategy(String strategyName);


    public BigDecimal getLastIntOrderId(String sessionQualifier);

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
     */
    public Tick getLastTick(int securityId, Date dateTime);

    /**
     * Gets all Ticks of the defined Security that are before the {@code maxDate} and after {@code
     * minDate} - {@code intervalDays}
     */
    public List<Tick> getTicksByMaxDate(int securityId, Date maxDate);

    /**
     * Gets all Ticks of the defined Security that are after the {@code minDate} and before {@code
     * minDate} + {@code intervalDays}
     */
    public List<Tick> getTicksByMinDate(int securityId, Date minDate);

    /**
     * Gets one Tick-Id per day of the defined Security that is just before the specified {@code
     * time}.
     */
    public List<Tick> getDailyTicksBeforeTime(int securityId, Date time);

    /**
     * Gets one Tick-Id per day of the defined Security that is just after the specified {@code
     * time}.
     */
    public List<Tick> getDailyTicksAfterTime(int securityId, Date time);

    /**
     * Gets one Tick-Id per hour of the defined Security that is just before the specified number of
     * {@code minutes} and after the specified {@code minDate}.
     */
    public List<Tick> getHourlyTicksBeforeMinutesByMinDate(int securityId, int minutes, Date minDate);

    /**
     * Gets one Tick-Id per hour of the defined Security that is just after the specified number of
     * {@code minutes} and after the specified {@code minDate}.
     */
    public List<Tick> getHourlyTicksAfterMinutesByMinDate(int securityId, int minutes, Date minDate);

    /**
     * Gets all Ticks for Securities that are subscribed by any Strategy between {@code minDate} and
     * {@code maxDate}
     */
    public List<Tick> getSubscribedTicksByTimePeriod(Date startDate, Date endDate);

    /**
     * Gets all Ticks for Securities that are subscribed by any Strategy between {@code minDate} and
     * {@code maxDate}
     */
    public Tick getFirstSubscribedTick();

    /**
     * Gets the first Tick of the defined Security that is before the maxDate (but not earlier than
     * one minute before that the maxDate).
     */
    public Tick getTickBySecurityAndMaxDate(int securityId, Date date);

    /**
     * Gets daily Bars created based on Ticks between {@code minDate} and {@code maxDate}
     */
    public List<Bar> getDailyBarsFromTicks(int securityId, Date fromDate, Date toDate);

    /**
     * Returns the last nl Bars of the specified Security and {@code barSize}
     */
    public List<Bar> getLastNBarsBySecurityAndBarSize(int n, int securityId, Duration barSize);

    /**
     * Returns the Bars of the specified Security {@code barSize} after the specified {@code
     * minDate}
     */
    public List<Bar> getBarsBySecurityBarSizeAndMinDate(int securityId, Duration barSize, Date minDate);

    /**
     * Gets all Ticks for Securities that are subscribed by any Strategy between {@code minDate} and
     * {@code maxDate}
     */
    public List<Bar> getSubscribedBarsByTimePeriodAndBarSize(Date startDate, Date endDate, Duration barSize);

    /**
     * Gets all Ticks for Securities that are subscribed by any Strategy between {@code minDate} and
     * {@code maxDate}
     */
    public Bar getFirstSubscribedBarByBarSize(Duration barSize);

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
    public Map getMeasurementsByMaxDate(String strategyName, String name, Date maxDate);

    /**
     * Gets all Measurements before the specified Date
     */
    public Map getAllMeasurementsByMaxDate(String strategyName, Date maxDate);

    /**
     * Gets all Measurements after the specified Date with the specified name
     */
    public Map getMeasurementsByMinDate(String strategyName, String name, Date minDate);

    /**
     * Gets all Measurements after the specified Date
     */
    public Map getAllMeasurementsByMinDate(String strategyName, Date minDate);

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
     * Retrieves an arbitrary list of Entities or values based on a Hibernate query. Example:
     * from Strategy
     * or
     * select name from Strategy where id = 1
     */
    public List<?> get(String query);

    /**
     * Retrieves an arbitrary list of Entities or values based on a Hibernate query. In addition a
     * Map containing named parameters can be passed. Example:
     * from Strategy where name = :strategyName
     * and the Map containing an entry with {@code key='strategyName'} and {@code value='SERVER'}
     */
    public List<?> get(String query, Map namedParameters);

    /**
     * Retrieves a unique Object based on a Hibernate query. Example:
     * from Strategy where id = 1
     */
    public Object getUnique(String query);

    /**
     * Retrieves a unique Object based on a Hibernate query. In addition a
     * Map containing named parameters can be passed. Example:
     * from Strategy where name = :strategyName
     * and the Map containing an entry with {@code key='strategyName'} and {@code value='SERVER'}
     */
    public Object getUnique(String query, Map namedParameters);

    /**
     * initialize all security Strings for subscribed Securities
     */
    public void initSecurityStrings();

}
