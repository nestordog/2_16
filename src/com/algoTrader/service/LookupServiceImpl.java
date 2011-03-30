package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.algoTrader.entity.Future;
import com.algoTrader.entity.FutureFamily;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.SecurityFamily;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionFamily;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Periodicity;
import com.algoTrader.util.DateUtil;
import com.algoTrader.vo.PortfolioValueVO;

@SuppressWarnings("unchecked")
public class LookupServiceImpl extends LookupServiceBase {

    protected Security handleGetSecurity(int id) throws java.lang.Exception {

        return getSecurityDao().load(id);
    }

    protected Security handleGetSecurityByIsin(String isin) throws Exception {

        return getSecurityDao().findByIsin(isin);
    }

    protected Security handleGetSecurityFetched(int securityId) throws Exception {

        return getSecurityDao().findByIdFetched(securityId);
    }

    protected Security[] handleGetSecuritiesOnWatchlistByPeriodicity(Periodicity periodicity) throws Exception {

        return (Security[]) getSecurityDao().findSecuritiesOnWatchlistByPeriodicity(periodicity).toArray(new Security[0]);
    }

    protected StockOption handleGetNearestStockOption(int underlayingId, Date expirationDate, BigDecimal underlayingSpot, String optionTypeString) throws Exception {

        return getStockOptionDao().findNearestStockOption(underlayingId, expirationDate, underlayingSpot, optionTypeString);
    }

    protected Future handleGetNearestFuture(int underlayingId, Date expirationDate) throws Exception {

        return getFutureDao().findNearestFuture(underlayingId, expirationDate);
    }

    protected Strategy handleGetStrategy(int id) throws java.lang.Exception {

        return getStrategyDao().load(id);
    }

    protected Strategy handleGetStrategyByName(String name) throws Exception {

        return getStrategyDao().findByName(name);
    }

    protected Strategy handleGetStrategyByNameFetched(String name) throws Exception {

        return getStrategyDao().findByNameFetched(name);
    }

    protected SecurityFamily handleGetSecurityFamily(int id) throws Exception {

        return getSecurityFamilyDao().load(id);
    }

    protected StockOptionFamily handleGetStockOptionFamilyByUnderlaying(int id) throws Exception {

        return getStockOptionFamilyDao().findByUnderlaying(id);
    }

    protected FutureFamily handleGetFutureFamilyByUnderlaying(int id) throws Exception {

        return getFutureFamilyDao().findByUnderlaying(id);
    }

    protected Position handleGetPosition(int id) throws java.lang.Exception {

        return getPositionDao().load(id);
    }

    protected Position handleGetPositionFetched(int id) throws Exception {

        return getPositionDao().findByIdFetched(id);
    }

    protected Position handleGetPositionBySecurityAndStrategy(int securityId, String strategyName) throws Exception {

        return getPositionDao().findBySecurityAndStrategy(securityId, strategyName);
    }

    protected Transaction handleGetTransaction(int id) throws java.lang.Exception {

        return getTransactionDao().load(id);
    }

    protected Security[] handleGetAllSecurities() throws Exception {

        return (Security[])getSecurityDao().loadAll().toArray(new Security[0]);
    }

    protected Strategy[] handleGetAllStrategies() throws Exception {

        return (Strategy[]) getStrategyDao().loadAll().toArray(new Strategy[0]);
    }

    protected Position[] handleGetAllPositions() throws Exception {

        return (Position[])getPositionDao().loadAll().toArray(new Position[0]);
    }

    protected Transaction[] handleGetAllTransactions() throws Exception {

        return (Transaction[])getTransactionDao().loadAll().toArray(new Transaction[0]);
    }

    protected Transaction[] handleGetAllTrades() throws Exception {

        return (Transaction[])getTransactionDao().findAllTrades().toArray(new Transaction[0]);
    }

    protected Transaction[] handleGetAllCashFlows() throws Exception {

        return (Transaction[]) getTransactionDao().findAllCashflows().toArray(new Transaction[0]);
    }

    protected Security[] handleGetAllSecuritiesInPortfolio() throws Exception {

        return (Security[])getSecurityDao().findSecuritiesInPortfolio().toArray(new Security[0]);
    }

    protected StockOption[] handleGetStockOptionsOnWatchlist() throws Exception {

        return (StockOption[]) getStockOptionDao().findStockOptionsOnWatchlist().toArray(new StockOption[0]);
    }

    protected Position[] handleGetOpenPositions() throws Exception {

        return (Position[])getPositionDao().findOpenPositions().toArray(new Position[0]);
    }

    protected Position[] handleGetOpenPositionsByStrategy(String strategyName) throws Exception {

        return (Position[]) getPositionDao().findOpenPositionsByStrategy(strategyName).toArray(new Position[0]);
    }

    protected Position[] handleGetBullishPositionsByStrategy(String strategyName) throws Exception {

        return (Position[]) getPositionDao().findBullishPositionsByStrategy(strategyName).toArray(new Position[0]);
    }

    protected Position[] handleGetBearishPositionsByStrategy(String strategyName) throws Exception {

        return (Position[]) getPositionDao().findBearishPositionsByStrategy(strategyName).toArray(new Position[0]);
    }

    protected PortfolioValueVO handleGetPortfolioValue() throws Exception {

        PortfolioValueVO portfolioValueVO = new PortfolioValueVO();
        portfolioValueVO.setSecuritiesCurrentValue(getStrategyDao().getPortfolioSecuritiesCurrentValueDouble());
        portfolioValueVO.setCashBalance(getStrategyDao().getPortfolioCashBalanceDouble());
        portfolioValueVO.setMaintenanceMargin(getStrategyDao().getPortfolioMaintenanceMarginDouble());
        portfolioValueVO.setNetLiqValue(getStrategyDao().getPortfolioNetLiqValueDouble());
        portfolioValueVO.setLeverage(getStrategyDao().getPortfolioLeverageDouble());

        return portfolioValueVO;
    }

    protected Tick handleGetLastTick(int securityId) throws Exception {

        return getTickDao().findLastTickForSecurityAndMaxDate(securityId, DateUtil.getCurrentEPTime());
    }

    protected List<Tick> handleGetPreFeedTicks(int securityId, int numberOfTicks) {


        List<Integer> recentIds = getTickDao().findLastNTickIdsForSecurity(securityId, numberOfTicks);
        if (recentIds.size() > 0) {
            List<Integer> ids = getTickDao().findEndOfDayTickIds(securityId, recentIds.get(0));
            ids.addAll(recentIds);
            return getTickDao().findByIdsFetched(ids);
        } else {
            return new ArrayList<Tick>();
        }
    }

    protected List<Strategy> handleGetAutoActivateStrategies() throws Exception {

        return getStrategyDao().findAutoActivateStrategies();
    }

    protected double handleGetForexRateDouble(Currency baseCurrency, Currency transactionCurrency) throws Exception {

        return getForexDao().getRateDouble(baseCurrency, transactionCurrency);
    }

    protected List<Currency> handleGetHeldCurrencies(String strategyName) throws Exception {

        return getStrategyDao().findHeldCurrencies(strategyName);
    }

    protected List<Currency> handleGetHeldCurrencies() throws Exception {

        return getStrategyDao().findPortfolioHeldCurrencies();
    }
}
