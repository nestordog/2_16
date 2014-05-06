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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.IllegalStateException;

import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.security.EasyToBorrow;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.IntrestRate;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.DefaultOrderPreference;
import ch.algotrader.entity.strategy.Measurement;
import ch.algotrader.entity.strategy.OrderPreference;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.HibernateUtil;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.vo.OrderStatusVO;
import ch.algotrader.vo.PositionVO;
import ch.algotrader.vo.TransactionVO;

@SuppressWarnings("unchecked")
/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LookupServiceImpl extends LookupServiceBase {

    private @Value("${simulation}") boolean simulation;
    private @Value("${statement.simulateOptions}") boolean simulateOptions;
    private @Value("${statement.simulateFuturesByUnderlying}") boolean simulateFuturesByUnderlying;
    private @Value("${statement.simulateFuturesByGenericFutures}") boolean simulateFuturesByGenericFutures;
    private @Value("${misc.transactionDisplayCount}") int transactionDisplayCount;
    private @Value("${misc.intervalDays}") int intervalDays;

    @Override
    protected Security handleGetSecurity(int id) throws java.lang.Exception {

        return getSecurityDao().get(id);
    }

    @Override
    protected Security handleGetSecurityByIsin(String isin) throws Exception {

        return getSecurityDao().findByIsin(isin);
    }

    @Override
    protected Security handleGetSecurityBySymbol(String symbol) throws Exception {

        return getSecurityDao().findBySymbol(symbol);
    }

    @Override
    protected Security handleGetSecurityByBbgid(String bbgid) throws Exception {

        return getSecurityDao().findByBbgid(bbgid);
    }

    @Override
    protected Security handleGetSecurityByRic(String ric) throws Exception {

        return getSecurityDao().findByRic(ric);
    }

    @Override
    protected Security handleGetSecurityByConid(String conid) throws Exception {

        return getSecurityDao().findByConid(conid);
    }

    @Override
    protected Security handleGetSecurityInclFamilyAndUnderlying(int securityId) throws Exception {

        return getSecurityDao().findByIdInclFamilyAndUnderlying(securityId);
    }

    @Override
    protected Security handleGetSecurityInitialized(int id) throws java.lang.Exception {

        Security security = getSecurityDao().get(id);

        // initialize the security
        if (security != null) {
            security.initialize();
        }

        return security;
    }

    @Override
    protected Collection<Security> handleGetAllSecurities() throws java.lang.Exception {

        return getSecurityDao().loadAll();
    }

    @Override
    protected List<Security> handleGetSubscribedSecuritiesForAutoActivateStrategies() throws Exception {

        return getSecurityDao().findSubscribedForAutoActivateStrategies();
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List<Map> handleGetSubscribedSecuritiesAndFeedTypeForAutoActivateStrategiesInclComponents() throws Exception {

        List<Map> subscriptions = getSecurityDao().findSubscribedAndFeedTypeForAutoActivateStrategies();

        // initialize components
        for (Map<String, Object> subscription : subscriptions) {

            Security security = (Security) subscription.get("security");
            if (security instanceof Combination) {
                ((Combination) security).getComponentsInitialized();
            }
        }

        return subscriptions;
    }

    @Override
    protected List<Security> handleGetSecuritiesByIds(Collection<Integer> ids) throws Exception {

        return getSecurityDao().findByIds(ids);
    }

    @Override
    protected Collection<Stock> handleGetStocksBySector(String code) throws Exception {

        return getStockDao().findBySectory(code);
    }

    @Override
    protected Collection<Stock> handleGetStocksByIndustryGroup(String code) throws Exception {

        return getStockDao().findByIndustryGroup(code);
    }

    @Override
    protected Collection<Stock> handleGetStocksByIndustry(String code) throws Exception {

        return getStockDao().findByIndustry(code);
    }

    @Override
    protected Collection<Stock> handleGetStocksBySubIndustry(String code) throws Exception {

        return getStockDao().findBySubIndustry(code);
    }

    @Override
    protected Collection<Combination> handleGetSubscribedCombinationsByStrategyAndComponent(String strategyName, int securityId) throws Exception {

        return getCombinationDao().findSubscribedByStrategyAndComponent(strategyName, securityId);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Collection<Combination> handleGetSubscribedCombinationsByStrategyAndComponentClass(String strategyName, final Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        return getCombinationDao().findSubscribedByStrategyAndComponentType(strategyName, discriminator);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Collection<Combination> handleGetSubscribedCombinationsByStrategyAndComponentClassWithZeroQty(String strategyName, final Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        return getCombinationDao().findSubscribedByStrategyAndComponentTypeWithZeroQty(strategyName, discriminator);
    }

    @Override
    protected Option handleGetOptionByMinExpirationAndMinStrikeDistance(int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot,
            OptionType optionType) throws Exception {

        Option option = CollectionUtil.getSingleElementOrNull(getOptionDao().findByMinExpirationAndMinStrikeDistance(1, 1, underlyingId, targetExpirationDate, underlyingSpot, optionType));

        // if no stock option was found, create it if simulating options
        if (this.simulation && this.simulateOptions) {

            OptionFamily family = getOptionFamilyDao().findByUnderlying(underlyingId);
            if ((option == null) || Math.abs(option.getStrike().doubleValue() - underlyingSpot.doubleValue()) > family.getStrikeDistance()) {

                option = getOptionService().createDummyOption(family.getId(), targetExpirationDate, underlyingSpot, optionType);
            }
        }

        if (option == null) {
            throw new LookupServiceException("no option available for expiration " + targetExpirationDate + " strike " + underlyingSpot + " type " + optionType);
        } else {
            return option;
        }
    }

    @Override
    protected Option handleGetOptionByMinExpirationAndMinStrikeDistanceWithTicks(int underlyingId, Date targetExpirationDate,
            BigDecimal underlyingSpot, OptionType optionType, Date date) throws Exception {

        return CollectionUtil.getSingleElementOrNull(getOptionDao().findByMinExpirationAndMinStrikeDistanceWithTicks(1, 1, underlyingId, targetExpirationDate, underlyingSpot, optionType, date));
    }

    @Override
    protected Option handleGetOptionByMinExpirationAndStrikeLimit(int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot,
            OptionType optionType)
            throws Exception {

        OptionFamily family = getOptionFamilyDao().findByUnderlying(underlyingId);

        Option option = CollectionUtil.getSingleElementOrNull(getOptionDao().findByMinExpirationAndStrikeLimit(1, 1, underlyingId, targetExpirationDate, underlyingSpot, optionType));

        // if no future was found, create it if simulating options
        if (this.simulation && this.simulateOptions) {
            if ((option == null) || Math.abs(option.getStrike().doubleValue() - underlyingSpot.doubleValue()) > family.getStrikeDistance()) {

                option = getOptionService().createDummyOption(family.getId(), targetExpirationDate, underlyingSpot, optionType);
            }
        }

        if (option == null) {
            throw new LookupServiceException("no option available for expiration " + targetExpirationDate + " strike " + underlyingSpot + " type "
                    + optionType);
        } else {
            return option;
        }
    }

    @Override
    protected Option handleGetOptionByMinExpirationAndStrikeLimitWithTicks(int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot,
            OptionType optionType,
            Date date) throws Exception {

        return CollectionUtil.getSingleElementOrNull(getOptionDao().findByMinExpirationAndStrikeLimitWithTicks(1, 1, underlyingId, targetExpirationDate, underlyingSpot, optionType, date));
    }

    @Override
    protected List<Option> handleGetSubscribedOptions() throws Exception {

        return getOptionDao().findSubscribedOptions();
    }

    @Override
    protected Future handleGetFutureByMinExpiration(int futureFamilyId, Date expirationDate) throws Exception {

        Future future = CollectionUtil.getSingleElementOrNull(getFutureDao().findByMinExpiration(1, 1, futureFamilyId, expirationDate));

        // if no future was found, create the missing part of the future-chain
        if (this.simulation && future == null && (this.simulateFuturesByUnderlying || this.simulateFuturesByGenericFutures)) {

            getFutureService().createDummyFutures(futureFamilyId);

            future = CollectionUtil.getSingleElementOrNull(getFutureDao().findByMinExpiration(1, 1, futureFamilyId, expirationDate));
        }

        if (future == null) {
            throw new LookupServiceException("no future available for expiration " + expirationDate);
        } else {
            return future;
        }
    }

    @Override
    protected Future handleGetFutureByExpiration(int futureFamilyId, Date expirationDate) throws Exception {

        Future future = getFutureDao().findByExpirationInclSecurityFamily(futureFamilyId, expirationDate);

        // if no future was found, create the missing part of the future-chain
        if (this.simulation && future == null && (this.simulateFuturesByUnderlying || this.simulateFuturesByGenericFutures)) {

            getFutureService().createDummyFutures(futureFamilyId);
            future = getFutureDao().findByExpirationInclSecurityFamily(futureFamilyId, expirationDate);
        }

        if (future == null) {
            throw new LookupServiceException("no future available targetExpiration " + expirationDate);
        } else {

            return future;
        }
    }

    @Override
    protected Future handleGetFutureByDuration(int futureFamilyId, Date targetExpirationDate, int duration) throws Exception {

        FutureFamily futureFamily = getFutureFamilyDao().get(futureFamilyId);

        Date expirationDate = DateUtil.getExpirationDateNMonths(futureFamily.getExpirationType(), targetExpirationDate, duration);
        Future future = getFutureDao().findByExpirationInclSecurityFamily(futureFamilyId, expirationDate);

        // if no future was found, create the missing part of the future-chain
        if (this.simulation && future == null && (this.simulateFuturesByUnderlying || this.simulateFuturesByGenericFutures)) {

            getFutureService().createDummyFutures(futureFamily.getId());
            future = getFutureDao().findByExpirationInclSecurityFamily(futureFamilyId, expirationDate);
        }

        if (future == null) {
            throw new LookupServiceException("no future available targetExpiration " + targetExpirationDate + " and duration " + duration);
        } else {
            return future;
        }
    }

    @Override
    protected List<Future> handleGetFuturesByMinExpiration(int futureFamilyId, Date minExpirationDate) throws Exception {

        return getFutureDao().findByMinExpiration(futureFamilyId, minExpirationDate);
    }

    @Override
    protected List<Future> handleGetSubscribedFutures() throws Exception {

        return getFutureDao().findSubscribedFutures();
    }

    @Override
    protected Subscription handleGetSubscriptionByStrategyAndSecurity(String strategyName, int securityId) throws Exception {

        return getSubscriptionDao().findByStrategyAndSecurity(strategyName, securityId);
    }

    @Override
    protected List<Subscription> handleGetSubscriptionsByStrategyInclComponents(String strategyName) throws Exception {

        List<Subscription> subscriptions = getSubscriptionDao().findByStrategy(strategyName);

        // initialize components
        for (Subscription subscription : subscriptions) {

            if (subscription.getSecurityInitialized() instanceof Combination) {
                ((Combination)subscription.getSecurity()).getComponentsInitialized();
            }
        }

        return subscriptions;
    }

    @Override
    protected Collection<Subscription> handleGetNonPositionSubscriptions(String strategyName) throws Exception {

        return getSubscriptionDao().findNonPositionSubscriptions(strategyName);
    }

    @Override
    protected Collection<Strategy> handleGetAllStrategies() throws Exception {

        return getStrategyDao().loadAll();
    }

    @Override
    protected Strategy handleGetStrategy(int id) throws java.lang.Exception {

        return getStrategyDao().get(id);
    }

    @Override
    protected Strategy handleGetStrategyByName(String name) throws Exception {

        return getStrategyDao().findByName(name);
    }

    @Override
    protected List<Strategy> handleGetAutoActivateStrategies() throws Exception {

        return getStrategyDao().findAutoActivateStrategies();
    }

    @Override
    protected SecurityFamily handleGetSecurityFamily(int id) throws Exception {

        return getSecurityFamilyDao().get(id);
    }

    @Override
    protected SecurityFamily handleGetSecurityFamilyByName(String name) throws Exception {

        return getSecurityFamilyDao().findByName(name);
    }

    @Override
    protected OptionFamily handleGetOptionFamilyByUnderlying(int id) throws Exception {

        return getOptionFamilyDao().findByUnderlying(id);
    }

    @Override
    protected FutureFamily handleGetFutureFamilyByUnderlying(int id) throws Exception {

        return getFutureFamilyDao().findByUnderlying(id);
    }

    @Override
    protected Collection<Position> handleGetAllPositions() throws Exception {

        return getPositionDao().loadAll();
    }

    @Override
    protected Position handleGetPosition(int id) throws java.lang.Exception {

        return getPositionDao().get(id);
    }

    @Override
    protected Position handleGetPositionInclSecurityAndSecurityFamily(int id) throws Exception {

        return getPositionDao().findByIdInclSecurityAndSecurityFamily(id);
    }

    @Override
    protected List<Position> handleGetPositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findByStrategy(strategyName);
    }

    @Override
    protected Position handleGetPositionBySecurityAndStrategy(int securityId, String strategyName) throws Exception {

        return getPositionDao().findBySecurityAndStrategy(securityId, strategyName);
    }

    @Override
    protected List<Position> handleGetOpenPositions() throws Exception {

        return getPositionDao().findOpenPositions();
    }

    @Override
    protected List<Position> handleGetOpenTradeablePositions() throws Exception {

        return getPositionDao().findOpenTradeablePositions();
    }

    @Override
    protected List<Position> handleGetOpenPositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findOpenPositionsByStrategy(strategyName);
    }

    @Override
    protected List<Position> handleGetOpenTradeablePositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findOpenTradeablePositionsByStrategy(strategyName);
    }

    @Override
    protected List<Position> handleGetOpenPositionsBySecurity(int securityId) throws Exception {

        return getPositionDao().findOpenPositionsBySecurity(securityId);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List<Position> handleGetOpenPositionsByStrategyAndType(String strategyName, final Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        return getPositionDao().findOpenPositionsByStrategyAndType(strategyName, discriminator);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected List<Position> handleGetOpenPositionsByStrategyTypeAndUnderlyingType(String strategyName, Class type, Class underlyingType) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        int underlyingDiscriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), underlyingType);
        return getPositionDao().findOpenPositionsByStrategyTypeAndUnderlyingType(strategyName, discriminator, underlyingDiscriminator);
    }

    @Override
    protected List<Position> handleGetOpenPositionsByStrategyAndSecurityFamily(String strategyName, int securityFamilyId) throws Exception {

        return getPositionDao().findOpenPositionsByStrategyAndSecurityFamily(strategyName, securityFamilyId);
    }

    @Override
    protected List<Position> handleGetOpenFXPositions() throws Exception {

        return getPositionDao().findOpenFXPositions();
    }

    @Override
    protected List<Position> handleGetOpenFXPositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findOpenFXPositionsByStrategy(strategyName);
    }

    @Override
    protected List<PositionVO> handleGetPositionsVO(String strategyName, boolean displayClosedPositions) throws Exception {

        if (strategyName.equals(StrategyImpl.BASE)) {
            if (displayClosedPositions) {
                return (List<PositionVO>) getPositionDao().loadAll(PositionDao.TRANSFORM_POSITIONVO);
            } else {
                return (List<PositionVO>) getPositionDao().findOpenPositions(PositionDao.TRANSFORM_POSITIONVO);
            }
        } else {
            if (displayClosedPositions) {
                return (List<PositionVO>) getPositionDao().findByStrategy(PositionDao.TRANSFORM_POSITIONVO, strategyName);
            } else {
                return (List<PositionVO>) getPositionDao().findOpenPositionsByStrategy(PositionDao.TRANSFORM_POSITIONVO, strategyName);
            }
        }
    }

    @Override
    protected Transaction handleGetTransaction(int id) throws java.lang.Exception {

        return getTransactionDao().get(id);
    }

    @Override
    protected List<Transaction> handleGetTradesByMinDateAndMaxDate(Date minDate, Date maxDate) throws Exception {

        return getTransactionDao().findTradesByMinDateAndMaxDate(minDate, maxDate);
    }

    @Override
    protected List<TransactionVO> handleGetTransactionsVO(String strategyName) throws Exception {

        if (strategyName.equals(StrategyImpl.BASE)) {
            return (List<TransactionVO>) getTransactionDao().findTransactionsDesc(TransactionDao.TRANSFORM_TRANSACTIONVO, 1, this.transactionDisplayCount);
        } else {
            return (List<TransactionVO>) getTransactionDao().findTransactionsByStrategyDesc(TransactionDao.TRANSFORM_TRANSACTIONVO, 1, this.transactionDisplayCount, strategyName);
        }
    }

    @Override
    protected Collection<Order> handleGetOpenOrdersByStrategy(String strategyName) throws Exception {

        return getOrderDao().findOpenOrdersByStrategy(strategyName);
    }

    @Override
    protected Order handleGetOpenOrderByIntId(String intId) throws Exception {

        return getOrderDao().findOpenOrderByIntId(intId);
    }

    @Override
    protected Order handleGetOpenOrderByRootIntId(String intId) throws Exception {

        return getOrderDao().findOpenOrderByRootIntId(intId);
    }

    @Override
    protected Order handleGetOpenOrderByExtId(String extId) throws Exception {

        return getOrderDao().findOpenOrderByExtId(extId);
    }

    @Override
    protected Collection<OrderStatusVO> handleGetOpenOrdersVOByStrategy(String strategyName) throws Exception {

        if (strategyName.equals(StrategyImpl.BASE)) {
            return getOrderStatusDao().findAllOrderStati();
        } else {
            return getOrderStatusDao().findOrderStatiByStrategy(strategyName);
        }

    }

    @Override
    protected Order handleGetOrderByName(String name) throws Exception {

        OrderPreference orderPreference = getOrderPreferenceDao().findByName(name);

        if (orderPreference == null) {
            throw new IllegalArgumentException("unknown OrderType or OrderPreference");
        }

        return orderPreference.createOrder();
    }

    @Override
    protected Order handleGetOrderByStrategyAndSecurityFamily(String strategyName, int securityFamilyId) throws Exception {

        DefaultOrderPreference defaultOrderPreference = getDefaultOrderPreferenceDao().findByStrategyAndSecurityFamilyInclOrderPreference(strategyName, securityFamilyId);
        if (defaultOrderPreference == null) {
            throw new IllegalStateException("no default order preference defined for securityFamilyId " + securityFamilyId + " and " + strategyName);
        }

        return defaultOrderPreference.getOrderPreference().createOrder();
    }

    @Override
    protected BigDecimal handleGetLastIntOrderId(String sessionQualifier) throws Exception {

        return getTransactionDao().findLastIntOrderId(sessionQualifier);
    }

    @Override
    protected Account handleGetAccountByName(String accountName) throws Exception {

        return getAccountDao().findByName(accountName);
    }

    @Override
    protected Collection<String> handleGetActiveSessionsByOrderServiceType(OrderServiceType orderServiceType) throws Exception {

        return getAccountDao().findActiveSessionsByOrderServiceType(orderServiceType);
    }

    @Override
    protected Tick handleGetLastTick(int securityId) throws Exception {

        Tick tick = CollectionUtil.getSingleElementOrNull(getTickDao().findTicksBySecurityAndMaxDate(1, 1, securityId, DateUtil.getCurrentEPTime(), this.intervalDays));

        if (tick != null) {
            tick.getSecurity().initialize();
        }

        return tick;
    }

    @Override
    protected List<Tick> handleGetTicksByMaxDate(int securityId, Date maxDate) throws Exception {

        return getTickDao().findTicksBySecurityAndMaxDate(securityId, maxDate, this.intervalDays);
    }

    @Override
    protected List<Tick> handleGetTicksByMinDate(int securityId, Date minDate) throws Exception {

        return getTickDao().findTicksBySecurityAndMinDate(securityId, minDate, this.intervalDays);
    }

    @Override
    protected List<Tick> handleGetDailyTicksBeforeTime(int securityId, Date time) {

        List<Integer> ids = getTickDao().findDailyTickIdsBeforeTime(securityId, time);
        if (ids.size() > 0) {
            return getTickDao().findByIdsInclSecurityAndUnderlying(ids);
        } else {
            return new ArrayList<Tick>();
        }
    }

    @Override
    protected List<Tick> handleGetDailyTicksAfterTime(int securityId, Date time) {

        List<Integer> ids = getTickDao().findDailyTickIdsAfterTime(securityId, time);
        if (ids.size() > 0) {
            return getTickDao().findByIdsInclSecurityAndUnderlying(ids);
        } else {
            return new ArrayList<Tick>();
        }
    }

    @Override
    protected List<Tick> handleGetHourlyTicksBeforeMinutesByMinDate(int securityId, int minutes, Date minDate) throws Exception {

        List<Integer> ids = getTickDao().findHourlyTickIdsBeforeMinutesByMinDate(securityId, minutes, minDate);
        if (ids.size() > 0) {
            return getTickDao().findByIdsInclSecurityAndUnderlying(ids);
        } else {
            return new ArrayList<Tick>();
        }
    }

    @Override
    protected List<Tick> handleGetHourlyTicksAfterMinutesByMinDate(int securityId, int minutes, Date minDate) throws Exception {

        List<Integer> ids = getTickDao().findHourlyTickIdsAfterMinutesByMinDate(securityId, minutes, minDate);
        if (ids.size() > 0) {
            return getTickDao().findByIdsInclSecurityAndUnderlying(ids);
        } else {
            return new ArrayList<Tick>();
        }
    }

    @Override
    protected List<Tick> handleGetSubscribedTicksByTimePeriod(Date startDate, Date endDate) throws Exception {

        return getTickDao().findSubscribedByTimePeriod(startDate, endDate);
    }

    @Override
    protected Tick handleGetTickBySecurityAndMaxDate(int securityId, Date date) {

        return getTickDao().findBySecurityAndMaxDate(securityId, date);
    }

    @Override
    protected List<Bar> handleGetDailyBarsFromTicks(int securityId, Date fromDate, Date toDate) {

        return getBarDao().findDailyBarsFromTicks(securityId, fromDate, toDate);
    }

    @Override
    protected List<Bar> handleGetLastNBarsBySecurityAndBarSize(int n, int securityId, Duration barSize) throws Exception {

        return getBarDao().findBarsBySecurityAndBarSize(1, n, securityId, barSize);
    }

    @Override
    protected List<Bar> handleGetBarsBySecurityBarSizeAndMinDate(int securityId, Duration barSize, Date minDate) throws Exception {

        return getBarDao().findBarsBySecurityBarSizeAndMinDate(securityId, barSize, minDate);
    }

    @Override
    protected double handleGetForexRateDouble(Currency baseCurrency, Currency transactionCurrency) throws Exception {

        return getForexDao().getRateDouble(baseCurrency, transactionCurrency);
    }

    @Override
    protected double handleGetForexRateDoubleByDate(Currency baseCurrency, Currency transactionCurrency, Date date) throws Exception {

        return getForexDao().getRateDoubleByDate(baseCurrency, transactionCurrency, date);
    }

    @Override
    protected IntrestRate handleGetInterestRateByCurrencyAndDuration(Currency currency, Duration duration) throws Exception {

        return getIntrestRateDao().findByCurrencyAndDuration(currency, duration);
    }

    @Override
    protected double handleGetInterestRateByCurrencyDurationAndDate(Currency currency, Duration duration, Date date) throws Exception {

        IntrestRate intrestRate = getIntrestRateDao().findByCurrencyAndDuration(currency, duration);

        List<Tick> ticks = getTickDao().findTicksBySecurityAndMaxDate(1, 1, intrestRate.getId(), date, this.intervalDays);
        if (ticks.isEmpty()) {
            throw new IllegalStateException("cannot get intrestRate for " + currency + " and duration " + duration + " because no last tick is available for date " + date);
        }

        return CollectionUtil.getFirstElement(ticks).getCurrentValueDouble();
    }

    @Override
    protected Collection<Currency> handleGetHeldCurrencies() throws Exception {

        return getCashBalanceDao().findHeldCurrencies();
    }

    @Override
    protected Collection<CashBalance> handleGetCashBalancesByStrategy(String strategyName) throws Exception {

        return getCashBalanceDao().findCashBalancesByStrategy(strategyName);
    }

    @Override
    protected Collection<Combination> handleGetSubscribedCombinationsByStrategy(String strategyName) throws Exception {

        return getCombinationDao().findSubscribedByStrategy(strategyName);
    }

    @Override
    protected Collection<Combination> handleGetSubscribedCombinationsByStrategyAndUnderlying(String strategyName, int underlyingId) throws Exception {

        return getCombinationDao().findSubscribedByStrategyAndUnderlying(strategyName, underlyingId);
    }

    @Override
    protected Collection<Component> handleGetSubscribedComponentsByStrategyInclSecurity(String strategyName) throws Exception {

        return getComponentDao().findSubscribedByStrategyInclSecurity(strategyName);
    }

    @Override
    protected Collection<Component> handleGetSubscribedComponentsBySecurityInclSecurity(int securityId) throws Exception {

        return getComponentDao().findSubscribedBySecurityInclSecurity(securityId);
    }

    @Override
    protected Collection<Component> handleGetSubscribedComponentsByStrategyAndSecurityInclSecurity(String strategyName, int securityId) throws Exception {

        return getComponentDao().findSubscribedByStrategyAndSecurityInclSecurity(strategyName, securityId);
    }

    @Override
    protected Map<Date, Object> handleGetMeasurementsByMaxDate(String strategyName, String name, Date maxDate) throws Exception {

        List<Measurement> measurements = getMeasurementDao().findMeasurementsByMaxDate(strategyName, name, maxDate);

        return getValuesByDate(measurements);
    }

    @Override
    protected Map<Date, Map<String, Object>> handleGetAllMeasurementsByMaxDate(String strategyName, Date maxDate) throws Exception {

        List<Measurement> measurements = getMeasurementDao().findAllMeasurementsByMaxDate(strategyName, maxDate);

        return getNameValuePairsByDate(measurements);
    }

    @Override
    protected Map<Date, Object> handleGetMeasurementsByMinDate(String strategyName, String name, Date minDate) throws Exception {

        List<Measurement> measurements = getMeasurementDao().findMeasurementsByMinDate(strategyName, name, minDate);

        return getValuesByDate(measurements);
    }

    @Override
    protected Map<Date, Map<String, Object>> handleGetAllMeasurementsByMinDate(String strategyName, Date minDate) throws Exception {

        List<Measurement> measurements = getMeasurementDao().findAllMeasurementsByMinDate(strategyName, minDate);

        return getNameValuePairsByDate(measurements);
    }

    @Override
    protected Object handleGetMeasurementByMaxDate(String strategyName, String name, Date maxDate) throws Exception {

        Measurement measurement = CollectionUtil.getSingleElementOrNull(getMeasurementDao().findMeasurementsByMaxDate(1, 1, strategyName, name, maxDate));
        return measurement != null ? measurement.getValue() : null;
    }

    @Override
    protected Object handleGetMeasurementByMinDate(String strategyName, String name, Date minDate) throws Exception {

        Measurement measurement = CollectionUtil.getSingleElementOrNull(getMeasurementDao().findMeasurementsByMinDate(1, 1, strategyName, name, minDate));
        return measurement != null ? measurement.getValue() : null;
    }

    @Override
    protected Collection<EasyToBorrow> handleGetEasyToBorrowByDateAndBroker(Date date, Broker broker) throws Exception {

        return getEasyToBorrowDao().findByDateAndBroker(date, broker);
    }

    @Override
    protected Date handleGetCurrentDBTime() throws Exception {

        return getStrategyDao().findCurrentDBTime();
    }

    private Map<Date, Object> getValuesByDate(List<Measurement> measurements) {

        Map<Date, Object> valuesByDate = new HashMap<Date, Object>();
        for (Measurement measurement : measurements) {
            valuesByDate.put(measurement.getDate(), measurement.getValue());
        }

        return valuesByDate;
    }

    private Map<Date, Map<String, Object>> getNameValuePairsByDate(List<Measurement> measurements) {

        // group Measurements by date
        MultiValueMap measurementsByDate = new MultiValueMap();
        for (Measurement measurement : measurements) {
            measurementsByDate.put(measurement.getDate(), measurement);
        }

        // create a nameValuePair Map per date
        Map<Date, Map<String, Object>> nameValuePairsByDate = new HashMap<Date, Map<String, Object>>();
        for (Date dt : (Set<Date>) measurementsByDate.keySet()) {

            Map<String, Object> nameValuePairs = new HashMap<String, Object>();
            for (Measurement measurement : (Collection<Measurement>) measurementsByDate.get(dt)) {
                nameValuePairs.put(measurement.getName(), measurement.getValue());
            }
            nameValuePairsByDate.put(dt, nameValuePairs);
        }

        return nameValuePairsByDate;
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected List handleGet(String query) throws Exception {

        return getGenericDao().find(query);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected List handleGet(String query, Map namedParameters) throws Exception {

        return getGenericDao().find(query, namedParameters);
    }
}
