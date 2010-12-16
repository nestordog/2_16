package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.Rule;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.SecurityFamily;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionFamily;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
import com.algoTrader.vo.PortfolioValueVO;

@SuppressWarnings("unchecked")
public class LookupServiceImpl extends LookupServiceBase {

    protected Security handleGetSecurity(int id) throws java.lang.Exception {

        return getSecurityDao().load(id);
    }

    protected Security handleGetSecurityByStrategyName(String strategyName) throws Exception {

        return getSecurityDao().findByStrategyName(strategyName);
    }

    protected Security handleGetSecurityByIsin(String isin) throws Exception {

        return getSecurityDao().findByIsin(isin);
    }

    protected Security handleGetSecurityFetched(int stockOptionId) throws Exception {

        return getSecurityDao().findSecurityFetched(stockOptionId);
    }

    protected StockOption handleGetNearestStockOption(int underlayingId, Date expirationDate, BigDecimal underlayingSpot, String optionTypeString) throws Exception {

        return getStockOptionDao().findNearestStockOption(underlayingId, expirationDate, underlayingSpot, optionTypeString);
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

    protected Position handleGetPosition(int id) throws java.lang.Exception {

        return getPositionDao().load(id);
    }

    protected Position handleGetPositionFetched(int id) throws Exception {

        return getPositionDao().findByIdFetched(id);
    }

    protected Rule handleGetRule(int id) throws java.lang.Exception {

        return getRuleDao().load(id);
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

    protected Rule[] handleGetAllRules() throws Exception {

        return (Rule[])getRuleDao().loadAll().toArray(new Rule[0]);

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

        return (Security[])getSecurityDao().findSecuritesInPortfolio().toArray(new Security[0]);
    }

    protected StockOption[] handleGetStockOptionsOnWatchlist() throws Exception {

        return (StockOption[]) getStockOptionDao().findStockOptionsOnWatchlist().toArray(new StockOption[0]);
    }

    protected Position[] handleGetOpenPositions() throws Exception {

        return (Position[])getPositionDao().findOpenPositions().toArray(new Position[0]);
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

        List<Tick> list = getTickDao().findLastNTicksForSecurity(securityId, 1);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    protected List<Tick> handleGetPreFeedTicks(int securityId, int numberOfTicks) {

        getTickDao().findLastNTicksForSecurity(securityId, numberOfTicks);
        List<Integer> recentIds = getTickDao().findLastNTickIdsForSecurity(securityId, numberOfTicks);
        List<Integer> ids = getTickDao().findEndOfDayTickIds(securityId, recentIds.get(0));

        ids.addAll(recentIds);

        return getTickDao().findByIdsFetched(ids);
    }

    protected Rule handleGetRuleByName(String name) throws Exception {

        return getRuleDao().findByName(name);
    }

    protected List<Rule> handleGetInitRules(String strategyName) throws Exception {

        return getRuleDao().findInitRules(strategyName);
    }

    protected List<Rule> handleGetAutoActivateRules(String strategyName) throws Exception {

        return getRuleDao().findAutoActivateRules(strategyName);
    }

    protected List<Strategy> handleGetAutoActivateStrategies() throws Exception {

        return getStrategyDao().findAutoActivateStrategies();
    }

    protected Object handleTest(Object object) throws Exception {

        Security security = (Security) object;
        getSecurityDao().update(security);
        security.getIsin();

        return null;
    }
}
