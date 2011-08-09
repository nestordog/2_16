package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;

import com.algoTrader.entity.CashBalance;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyDao;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.WatchListItem;
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
import com.algoTrader.vo.PortfolioValueVO;

@SuppressWarnings("unchecked")
public class LookupServiceImpl extends LookupServiceBase {

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
    protected Security[] handleGetSecuritiesOnWatchlistByPeriodicity(Periodicity periodicity) throws Exception {

        return getSecurityDao().findSecuritiesOnWatchlistByPeriodicity(periodicity).toArray(new Security[0]);
    }

    @Override
    protected WatchListItem handleGetWatchListItem(String strategyName, int securityId) throws Exception {

        return getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId);
    }

    @Override
    protected StockOption handleGetNearestStockOption(int underlayingId, Date expirationDate, BigDecimal underlayingSpot, OptionType optionType)
            throws Exception {

        return getStockOptionDao().findNearestStockOption(underlayingId, expirationDate, underlayingSpot, optionType.getValue());
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
    protected Future handleGetNearestFuture(int underlayingId, Date expirationDate) throws Exception {

        Future future = getFutureDao().findNearestFuture(underlayingId, expirationDate);

        // if no future was found, we need to create the missing part of the future-chain
        if (future == null) {

            FutureFamily futureFamily = getFutureFamilyDao().findByUnderlaying(underlayingId);
            getFutureService().createFutures(futureFamily.getId());

            future = getFutureDao().findNearestFuture(underlayingId, expirationDate);

            if (future == null) {
                throw new LookupServiceException("the requested targetExpirationDate " + expirationDate + " is too far our");
            }
        }

        return future;
    }

    @Override
    protected Future handleGetFutureByDuration(int underlayingId, Date targetExpirationDate, int duration) throws Exception {

        FutureFamily futureFamily = getFutureFamilyDao().findByUnderlaying(underlayingId);
        if (futureFamily == null) {
            throw new LookupServiceException("futureFamily for underlaying id: " + underlayingId + " does not exist");
        } else {
            Date expirationDate = DateUtil.getExpirationDateNMonths(futureFamily.getExpirationType(), targetExpirationDate, duration);
            Future future = getFutureDao().findFutureByExpiration(underlayingId, expirationDate);

            // if no future was found, we need to create the missing part of the future-chain
            if (future == null) {

                getFutureService().createFutures(futureFamily.getId());

                future = getFutureDao().findNearestFuture(underlayingId, expirationDate);

                if (future == null) {
                    throw new LookupServiceException("the requested targetExpirationDate " + targetExpirationDate + " is too far our");
                }
            }
            return future;
        }
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
    protected Position handleGetPositionBySecurityAndStrategy(int securityId, String strategyName) throws Exception {

        return getPositionDao().findBySecurityAndStrategy(securityId, strategyName);
    }

    @Override
    protected Transaction handleGetTransaction(int id) throws java.lang.Exception {

        return getTransactionDao().load(id);
    }

    @Override
    protected Security[] handleGetAllSecurities() throws Exception {

        return getSecurityDao().loadAll().toArray(new Security[0]);
    }

    @Override
    protected Strategy[] handleGetAllStrategies() throws Exception {

        return getStrategyDao().loadAll().toArray(new Strategy[0]);
    }

    @Override
    protected Position[] handleGetAllPositions() throws Exception {

        return getPositionDao().loadAll().toArray(new Position[0]);
    }

    @Override
    protected Transaction[] handleGetAllTransactions() throws Exception {

        return getTransactionDao().loadAll().toArray(new Transaction[0]);
    }

    @Override
    protected Transaction[] handleGetAllTrades() throws Exception {

        return getTransactionDao().findAllTrades().toArray(new Transaction[0]);
    }

    @Override
    protected Transaction[] handleGetAllCashFlows() throws Exception {

        return getTransactionDao().findAllCashflows().toArray(new Transaction[0]);
    }

    @Override
    protected Security[] handleGetAllSecuritiesInPortfolio() throws Exception {

        return getSecurityDao().findSecuritiesInPortfolio().toArray(new Security[0]);
    }

    @Override
    protected StockOption[] handleGetStockOptionsOnWatchlist() throws Exception {

        return getStockOptionDao().findStockOptionsOnWatchlist().toArray(new StockOption[0]);
    }

    @Override
    protected Security[] handleGetSecuritiesOnWatchlist() throws Exception {

        return getSecurityDao().findSecuritiesOnWatchlist().toArray(new Security[0]);
    }

    @Override
    protected Future[] handleGetFuturesOnWatchlist() throws Exception {

        return getFutureDao().findFuturesOnWatchlist().toArray(new Future[0]);
    }

    @Override
    protected Position[] handleGetOpenPositions() throws Exception {

        return getPositionDao().findOpenPositions().toArray(new Position[0]);
    }

    @Override
    protected Position[] handleGetOpenFXPositions() throws Exception {

        return getPositionDao().findOpenFXPositions().toArray(new Position[0]);
    }

    @Override
    protected Position[] handleGetOpenPositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findOpenPositionsByStrategy(strategyName).toArray(new Position[0]);
    }

    @Override
    protected Position[] handleGetOpenFXPositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findOpenFXPositionsByStrategy(strategyName).toArray(new Position[0]);
    }

    @Override
    protected Position[] handleGetBullishPositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findBullishPositionsByStrategy(strategyName).toArray(new Position[0]);
    }

    @Override
    protected Position[] handleGetBearishPositionsByStrategy(String strategyName) throws Exception {

        return getPositionDao().findBearishPositionsByStrategy(strategyName).toArray(new Position[0]);
    }

    @Override
    protected PortfolioValueVO handleGetPortfolioValue() throws Exception {

        double cashBalance = getStrategyDao().getPortfolioCashBalanceDouble();
        double securitiesCurrentValue = getStrategyDao().getPortfolioSecuritiesCurrentValueDouble();
        double maintenanceMargin = getStrategyDao().getPortfolioMaintenanceMarginDouble();
        double leverage = getStrategyDao().getPortfolioLeverageDouble();

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
    protected List<Tick> handleGetEndOfDayTicks(int securityId, Date maxDate, Date maxTime) {

        List<Integer> ids = (List<Integer>) getTickDao().findEndOfDayTickIds(TickDao.TRANSFORM_NONE, securityId, maxDate, maxTime);
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
    protected List<CashBalance> handleGetCashBalancesBase() throws Exception {

        return getCashBalanceDao().findCashBalancesBase();
    }
}
