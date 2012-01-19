package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.CashBalance;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyDao;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.WatchListItem;
import com.algoTrader.entity.combination.Allocation;
import com.algoTrader.entity.combination.Combination;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickDao;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.FutureFamily;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.Periodicity;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.vo.PortfolioValueVO;

@SuppressWarnings("unchecked")
public class LookupServiceImpl extends LookupServiceBase {

    private @Value("${simulation}") boolean simulation;
    private @Value("${statement.simulateStockOptions}") boolean simulateStockOptions;
    private @Value("${statement.simulateFuturesByUnderlaying}") boolean simulateFuturesByUnderlaying;
    private @Value("${statement.simulateFuturesByGenericFutures}") boolean simulateFuturesByGenericFutures;

    @Override
    protected Security handleGetSecurity(int id) throws java.lang.Exception {

        return getSecurityDao().load(id);
    }

    @Override
    protected Security handleGetSecurityByIsin(String isin) throws Exception {

        return getSecurityDao().findByIsin(isin);
    }

    @Override
    protected Security handleGetSecurityFetched(int securityId) throws Exception {

        return getSecurityDao().findByIdFetched(securityId);
    }

    @Override
    protected List<Security> handleGetSecuritiesByIds(Collection<Integer> ids) throws Exception {

        return getSecurityDao().findByIds(ids);
    }

    @Override
    protected List<Security> handleGetSecuritiesOnWatchlist(String strategyName) throws Exception {

        return getSecurityDao().findSecuritiesOnWatchlistByStrategy(strategyName);
    }

    @Override
    protected List<Security> handleGetSecuritiesOnWatchlistByPeriodicity(Periodicity periodicity) throws Exception {

        return getSecurityDao().findSecuritiesOnWatchlistByPeriodicity(periodicity);
    }

    @Override
    protected WatchListItem handleGetWatchListItem(String strategyName, int securityId) throws Exception {

        return getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId);
    }

    @Override
    protected List<WatchListItem> handleGetNonPositionWatchListItem(String strategyName) throws Exception {

        return getWatchListItemDao().findNonPositionWatchListItem(strategyName);
    }

    @Override
    protected StockOption handleGetNearestStockOption(int underlayingId, Date expirationDate, BigDecimal underlayingSpot, OptionType optionType)
            throws Exception {

        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlaying(underlayingId);

        StockOption stockOption = getStockOptionDao().findNearestStockOption(underlayingId, expirationDate, underlayingSpot, optionType.getValue());

        // if no future was found, create it if simulating options
        if (this.simulation && this.simulateStockOptions) {
            if ((stockOption == null) || Math.abs(stockOption.getStrike().doubleValue() - underlayingSpot.doubleValue()) > family.getStrikeDistance()) {

                stockOption = getStockOptionService().createDummyStockOption(family.getId(), expirationDate, underlayingSpot, optionType);
            }
        }

        if (stockOption == null) {
            throw new LookupServiceException("no stockOption available for expiration " + expirationDate + " strike " + underlayingSpot + " type " + optionType);
        } else {
            return stockOption;
        }
    }

    @Override
    protected StockOption handleGetNearestStockOptionWithTicks(int underlayingId, Date expirationDate, BigDecimal underlayingSpot, OptionType optionType,
            Date date) throws Exception {

        return getStockOptionDao().findNearestStockOptionWithTicks(underlayingId, expirationDate, underlayingSpot, optionType.getValue(), date);
    }

    @Override
    protected Date handleGetNearestExpirationWithTicks(int underlayingId, Date date, Date targetDate) throws Exception {

        return getStockOptionDao().findNearestExpirationWithTicks(underlayingId, date, targetDate);
    }

    @Override
    protected Future handleGetNearestFuture(int futureFamilyId, Date expirationDate) throws Exception {

        Future future = getFutureDao().findNearestFuture(futureFamilyId, expirationDate);

        // if no future was found, create the missing part of the future-chain
        if (this.simulation && future == null && (this.simulateFuturesByUnderlaying || this.simulateFuturesByGenericFutures)) {

            FutureFamily futureFamily = getFutureFamilyDao().load(futureFamilyId);

            getFutureService().createDummyFutures(futureFamily.getId());
            future = getFutureDao().findNearestFuture(futureFamilyId, expirationDate);
        }

        if (future == null) {
            throw new LookupServiceException("no future available for expiration " + expirationDate);
        } else {
            return future;
        }

    }

    @Override
    protected Future handleGetFutureByDuration(int futureFamilyId, Date targetExpirationDate, int duration) throws Exception {

        FutureFamily futureFamily = getFutureFamilyDao().load(futureFamilyId);

        Date expirationDate = DateUtil.getExpirationDateNMonths(futureFamily.getExpirationType(), targetExpirationDate, duration);
        Future future = getFutureDao().findFutureByExpiration(futureFamilyId, expirationDate);

        // if no future was found, create the missing part of the future-chain
        if (this.simulation && future == null && (this.simulateFuturesByUnderlaying || this.simulateFuturesByGenericFutures)) {

            getFutureService().createDummyFutures(futureFamily.getId());
            future = getFutureDao().findFutureByExpiration(futureFamilyId, expirationDate);
        }

        if (future == null) {
            throw new LookupServiceException("no future available targetExpiration " + targetExpirationDate + " and duration " + duration);
        } else {
            return future;
        }
    }

    @Override
    protected List<Future> handleGetFuturesByMinExpiration(int futureFamilyId, Date minExpirationDate) throws Exception {

        return getFutureDao().findFutureByMinExpiration(futureFamilyId, minExpirationDate);
    }

    @Override
    protected Strategy handleGetStrategy(int id) throws java.lang.Exception {

        return getStrategyDao().load(id);
    }

    @Override
    protected Strategy handleGetStrategyByName(String name) throws Exception {

        return getStrategyDao().findByName(name);
    }

    @Override
    protected Strategy handleGetStrategyByNameFetched(String name) throws Exception {

        return getStrategyDao().findByNameFetched(name);
    }

    @Override
    protected SecurityFamily handleGetSecurityFamily(int id) throws Exception {

        return getSecurityFamilyDao().load(id);
    }

    @Override
    protected StockOptionFamily handleGetStockOptionFamilyByUnderlaying(int id) throws Exception {

        return getStockOptionFamilyDao().findByUnderlaying(id);
    }

    @Override
    protected FutureFamily handleGetFutureFamilyByUnderlaying(int id) throws Exception {

        return getFutureFamilyDao().findByUnderlaying(id);
    }

    @Override
    protected Position handleGetPosition(int id) throws java.lang.Exception {

        return getPositionDao().load(id);
    }

    @Override
    protected Position handleGetPositionFetched(int id) throws Exception {

        return getPositionDao().findByIdFetched(id);
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
    protected Transaction handleGetTransaction(int id) throws java.lang.Exception {

        return getTransactionDao().load(id);
    }

    @Override
    protected Collection<Security> handleGetAllSecurities() throws Exception {

        return getSecurityDao().loadAll();
    }

    @Override
    protected Collection<Strategy> handleGetAllStrategies() throws Exception {

        return getStrategyDao().loadAll();
    }

    @Override
    protected Collection<Position> handleGetAllPositions() throws Exception {

        return getPositionDao().loadAll();
    }

    @Override
    protected Collection<Transaction> handleGetAllTransactions() throws Exception {

        return getTransactionDao().loadAll();
    }

    @Override
    protected List<Transaction> handleGetAllTrades() throws Exception {

        return getTransactionDao().findAllTrades();
    }

    @Override
    protected List<Transaction> handleGetAllCashFlows() throws Exception {

        return getTransactionDao().findAllCashflows();
    }

    @Override
    protected List<Security> handleGetAllSecuritiesInPortfolio() throws Exception {

        return getSecurityDao().findSecuritiesInPortfolio();
    }

    @Override
    protected List<StockOption> handleGetStockOptionsOnWatchlist() throws Exception {

        return getStockOptionDao().findStockOptionsOnWatchlist();
    }

    @Override
    protected List<WatchListItem> handleGetWatchListItemsByStrategy(String strategyName) throws Exception {

        return getWatchListItemDao().findByStrategy(strategyName);
    }

    @Override
    protected List<Security> handleGetSecuritiesOnWatchlist() throws Exception {

        return getSecurityDao().findSecuritiesOnWatchlist();
    }

    @Override
    protected List<Future> handleGetFuturesOnWatchlist() throws Exception {

        return getFutureDao().findFuturesOnWatchlist();
    }

    @Override
    protected List<Position> handleGetOpenPositions() throws Exception {

        return getPositionDao().findOpenPositions();
    }

    @Override
    protected List<Position> handleGetOpenPositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findOpenPositionsByStrategy(strategyName);
    }

    @Override
    protected List<Position> handleGetOpenPositionsBySecurityId(int securityId) throws Exception {

        return getPositionDao().findOpenPositionsBySecurityId(securityId);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List<Position> handleGetOpenPositionsByStrategyAndType(String strategyName, final Class type) throws Exception {

        List<Position> positions = getPositionDao().findOpenPositionsByStrategy(strategyName);

        return new ArrayList(CollectionUtils.select(positions, new Predicate<Position>() {
            @Override
            public boolean evaluate(Position position) {
                return type.isAssignableFrom(position.getSecurity().getClass());
            }
        }));
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
    protected PortfolioValueVO handleGetPortfolioValue() throws Exception {

        double cashBalance = getStrategyDao().getPortfolioCashBalanceDouble();
        double securitiesCurrentValue = getStrategyDao().getPortfolioSecuritiesCurrentValueDouble();
        double maintenanceMargin = getStrategyDao().getPortfolioMaintenanceMarginDouble();
        double leverage = getStrategyDao().getPortfolioLeverage();

        PortfolioValueVO portfolioValueVO = new PortfolioValueVO();
        portfolioValueVO.setCashBalance(cashBalance);
        portfolioValueVO.setSecuritiesCurrentValue(securitiesCurrentValue);
        portfolioValueVO.setMaintenanceMargin(maintenanceMargin);
        portfolioValueVO.setNetLiqValue(cashBalance + securitiesCurrentValue);
        portfolioValueVO.setLeverage(leverage);

        return portfolioValueVO;
    }

    @Override
    protected Tick handleGetLastTick(int securityId) throws Exception {

        Tick tick = getTickDao().findLastTickForSecurityAndMaxDate(securityId, DateUtil.getCurrentEPTime());

        if (tick != null) {
            Hibernate.initialize(tick.getSecurity());
            Hibernate.initialize(tick.getSecurity().getSecurityFamily());
        }

        return tick;
    }

    @Override
    protected List<Tick> handleGetLastNTicks(int securityId, int numberOfTicks) {

        List<Integer> ids = (List<Integer>) getTickDao().findLastNTickIdsForSecurity(TickDao.TRANSFORM_NONE, securityId, numberOfTicks);
        if (ids.size() > 0) {
            return getTickDao().findByIdsFetched(ids);
        } else {
            return new ArrayList<Tick>();
        }
    }

    @Override
    protected List<Tick> handleGetDailyTicksBeforeTime(int securityId, Date maxDate, Date time) {

        List<Integer> ids = (List<Integer>) getTickDao().findDailyTickIdsBeforeTime(TickDao.TRANSFORM_NONE, securityId, maxDate, time);
        if (ids.size() > 0) {
            return getTickDao().findByIdsFetched(ids);
        } else {
            return new ArrayList<Tick>();
        }
    }

    @Override
    protected List<Tick> handleGetDailyTicksAfterTime(int securityId, Date maxDate, Date time) {

        List<Integer> ids = (List<Integer>) getTickDao().findDailyTickIdsAfterTime(TickDao.TRANSFORM_NONE, securityId, maxDate, time);
        if (ids.size() > 0) {
            return getTickDao().findByIdsFetched(ids);
        } else {
            return new ArrayList<Tick>();
        }
    }

    @Override
    protected List<Tick> handleGetTicksByTimePeriodOnWatchlist(Date startDate, Date endDate) throws Exception {

        return getTickDao().findByTimePeriodOnWatchlist(startDate, endDate);
    }

    @Override
    protected Tick handleGetTickByDateAndSecurity(Date date, int securityId) {

        return getTickDao().findByDateAndSecurity(date, securityId);
    }

    @Override
    protected List<Strategy> handleGetAutoActivateStrategies() throws Exception {

        return getStrategyDao().findAutoActivateStrategies();
    }

    @Override
    protected double handleGetForexRateDouble(Currency baseCurrency, Currency transactionCurrency) throws Exception {

        return getForexDao().getRateDouble(baseCurrency, transactionCurrency);
    }

    @Override
    protected List<Currency> handleGetHeldCurrencies(String strategyName) throws Exception {

        return (List<Currency>) getStrategyDao().findHeldCurrencies(StrategyDao.TRANSFORM_NONE, strategyName);
    }

    @Override
    protected List<Currency> handleGetHeldCurrencies() throws Exception {

        return (List<Currency>) getStrategyDao().findPortfolioHeldCurrencies(StrategyDao.TRANSFORM_NONE);
    }

    @Override
    protected List<CashBalance> handleGetCashBalancesByStrategy(Strategy strategy) throws Exception {

        return getCashBalanceDao().findCashBalancesByStrategy(strategy);
    }

    @Override
    protected Collection<Combination> handleGetAllCombinations() throws Exception {

        return getCombinationDao().loadAll();
    }

    @Override
    protected Combination handleGetCombination(int id) throws Exception {

        return getCombinationDao().load(id);
    }

    @Override
    protected List<Combination> handleGetCombinationsByStrategy(String strategyName) throws Exception {

        return getCombinationDao().findByStrategy(strategyName);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List<Combination> handleGetCombinationsByStrategyAndType(String strategyName, final Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        return getCombinationDao().findByStrategyAndType(strategyName, discriminator);
    }

    @Override
    protected List<Combination> handleGetCombinationsByMasterSecurity(int masterSecurityId) throws Exception {

        return getCombinationDao().findByMasterSecurity(masterSecurityId);
    }

    @Override
    protected Combination handleGetCombinationByStrategyAndMasterSecurity(String strategyName, int masterSecurityId) throws Exception {

        return getCombinationDao().findByStrategyAndMasterSecurity(strategyName, masterSecurityId);
    }

    @Override
    protected List<Combination> handleGetCombinationsByAnySecurity(String strategyName, int securityId) throws Exception {

        return getCombinationDao().findByAnySecurity(strategyName, securityId);
    }

    @Override
    protected List<Allocation> handleGetAllocationsByStrategy(String strategyName) throws Exception {

        return getAllocationDao().findByStrategy(strategyName);
    }

    @Override
    protected List<Allocation> handleGetAllocationsByStrategyAndSecurity(String strategyName, int securityId) throws Exception {

        return getAllocationDao().findByStrategyAndSecurity(strategyName, securityId);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected List<Allocation> handleGetAllocationsByStrategyAndType(String strategyName, Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        return getAllocationDao().findByStrategyAndType(strategyName, discriminator);
    }
}
