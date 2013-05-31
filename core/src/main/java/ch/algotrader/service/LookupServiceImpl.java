/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
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

import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.HibernateUtil;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.metric.MetricsUtil;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionDao;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionDao;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Combination;
import com.algoTrader.entity.security.Component;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.FutureFamily;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.entity.strategy.CashBalance;
import com.algoTrader.entity.strategy.DefaultOrderPreference;
import com.algoTrader.entity.strategy.Measurement;
import com.algoTrader.entity.strategy.OrderPreference;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.OrderServiceType;
import com.algoTrader.service.LookupServiceBase;
import com.algoTrader.service.LookupServiceException;
import com.algoTrader.vo.OrderStatusVO;
import com.algoTrader.vo.PositionVO;
import com.algoTrader.vo.RawBarVO;
import com.algoTrader.vo.RawTickVO;
import com.algoTrader.vo.TransactionVO;

@SuppressWarnings("unchecked")
/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LookupServiceImpl extends LookupServiceBase {

    private @Value("${simulation}") boolean simulation;
    private @Value("${statement.simulateStockOptions}") boolean simulateStockOptions;
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
    protected Security handleGetSecurityInclFamilyAndUnderlying(int securityId) throws Exception {

        return getSecurityDao().findByIdInclFamilyAndUnderlying(securityId);
    }

    @Override
    protected Security handleGetSecurityInitialized(int id) throws java.lang.Exception {

        Security security = getSecurityDao().get(id);

        // initialize the security
        security.initialize();

        return security;
    }

    @Override
    protected Combination handleGetCombinationInclComponentsInitialized(int id) throws java.lang.Exception {

        Combination combination = getCombinationDao().get(id);

        if (combination != null) {

            // initialize the security
            combination.initialize();

            // initialize components
            combination.getComponentsInitialized();
        }

        return combination;
    }

    @Override
    protected List<Security> handleGetSecuritiesByIds(Collection<Integer> ids) throws Exception {

        return getSecurityDao().findByIds(ids);
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
    protected List<Security> handleGetSubscribedSecuritiesForAutoActivateStrategiesInclFamily() throws Exception {

        return getSecurityDao().findSubscribedForAutoActivateStrategiesInclFamily();
    }

    @Override
    protected StockOption handleGetStockOptionByMinExpirationAndMinStrikeDistance(int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot,
            OptionType optionType) throws Exception {

        StockOption stockOption = CollectionUtil.getSingleElementOrNull(getStockOptionDao().findByMinExpirationAndMinStrikeDistance(1, 1, underlyingId, targetExpirationDate, underlyingSpot, optionType));

        // if no stock option was found, create it if simulating options
        if (this.simulation && this.simulateStockOptions) {

            StockOptionFamily family = getStockOptionFamilyDao().findByUnderlying(underlyingId);
            if ((stockOption == null) || Math.abs(stockOption.getStrike().doubleValue() - underlyingSpot.doubleValue()) > family.getStrikeDistance()) {

                stockOption = getStockOptionService().createDummyStockOption(family.getId(), targetExpirationDate, underlyingSpot, optionType);
            }
        }

        if (stockOption == null) {
            throw new LookupServiceException("no stockOption available for expiration " + targetExpirationDate + " strike " + underlyingSpot + " type " + optionType);
        } else {
            return stockOption;
        }
    }

    @Override
    protected StockOption handleGetStockOptionByMinExpirationAndMinStrikeDistanceWithTicks(int underlyingId, Date targetExpirationDate,
            BigDecimal underlyingSpot, OptionType optionType, Date date) throws Exception {

        return CollectionUtil.getSingleElementOrNull(getStockOptionDao().findByMinExpirationAndMinStrikeDistanceWithTicks(1, 1, underlyingId, targetExpirationDate, underlyingSpot, optionType, date));
    }

    @Override
    protected StockOption handleGetStockOptionByMinExpirationAndStrikeLimit(int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot,
            OptionType optionType)
            throws Exception {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlying(underlyingId);

        StockOption stockOption = CollectionUtil.getSingleElementOrNull(getStockOptionDao().findByMinExpirationAndStrikeLimit(1, 1, underlyingId, targetExpirationDate, underlyingSpot, optionType));

        // if no future was found, create it if simulating options
        if (this.simulation && this.simulateStockOptions) {
            if ((stockOption == null) || Math.abs(stockOption.getStrike().doubleValue() - underlyingSpot.doubleValue()) > family.getStrikeDistance()) {

                stockOption = getStockOptionService().createDummyStockOption(family.getId(), targetExpirationDate, underlyingSpot, optionType);
            }
        }

        if (stockOption == null) {
            throw new LookupServiceException("no stockOption available for expiration " + targetExpirationDate + " strike " + underlyingSpot + " type "
                    + optionType);
        } else {
            return stockOption;
        }
    }

    @Override
    protected StockOption handleGetStockOptionByMinExpirationAndStrikeLimitWithTicks(int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot,
            OptionType optionType,
            Date date) throws Exception {

        return CollectionUtil.getSingleElementOrNull(getStockOptionDao().findByMinExpirationAndStrikeLimitWithTicks(1, 1, underlyingId, targetExpirationDate, underlyingSpot, optionType, date));
    }

    @Override
    protected List<StockOption> handleGetSubscribedStockOptions() throws Exception {

        return getStockOptionDao().findSubscribedStockOptions();
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
    protected List<Subscription> handleGetSubscriptionsForAutoActivateStrategiesInclComponents() throws Exception {

        List<Subscription> subscriptions = getSubscriptionDao().findForAutoActivateStrategies();

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
    protected StockOptionFamily handleGetStockOptionFamilyByUnderlying(int id) throws Exception {

        return getStockOptionFamilyDao().findByUnderlying(id);
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
    protected List<TransactionVO> handleGetTransactionsVO(String strategyName) throws Exception {

        if (strategyName.equals(StrategyImpl.BASE)) {
            return (List<TransactionVO>) getTransactionDao().findTransactionsDesc(TransactionDao.TRANSFORM_TRANSACTIONVO, 1, this.transactionDisplayCount);
        } else {
            return (List<TransactionVO>) getTransactionDao().findTransactionsByStrategyDesc(TransactionDao.TRANSFORM_TRANSACTIONVO, 1, this.transactionDisplayCount, strategyName);
        }
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
    protected Collection<OrderStatusVO> handleGetOpenOrdersVO(String strategyName) throws Exception {

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
    protected List<Bar> handleGetDailyBarsFromTicks(int securityId, Date fromDate, Date toDate) {

        return getBarDao().findDailyBars(securityId, fromDate, toDate);
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
    protected double handleGetForexRateDouble(Currency baseCurrency, Currency transactionCurrency) throws Exception {

        return getForexDao().getRateDouble(baseCurrency, transactionCurrency);
    }

    @Override
    protected double handleGetForexRateDoubleByDate(Currency baseCurrency, Currency transactionCurrency, Date date) throws Exception {

        return getForexDao().getRateDoubleByDate(baseCurrency, transactionCurrency, date);
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
    protected Tick handleGetTickFromRawTick(RawTickVO rawTick) {

        long beforeCompleteRaw = System.nanoTime();
        Tick tick = getTickDao().rawTickVOToEntity(rawTick);
        long afterCompleteRawT = System.nanoTime();

        MetricsUtil.account("LookupService.getMarketDataEventFromRaw", (afterCompleteRawT - beforeCompleteRaw));

        return tick;
    }

    @Override
    protected Bar handleGetBarFromRawBar(RawBarVO barVO) {

        long beforeCompleteRaw = System.nanoTime();
        Bar bar = getBarDao().rawBarVOToEntity(barVO);
        long afterCompleteRawT = System.nanoTime();

        MetricsUtil.account("LookupService.getMarketDataEventFromRaw", (afterCompleteRawT - beforeCompleteRaw));

        return bar;
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
}
