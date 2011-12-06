package com.algoTrader.util;

import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.WatchListItem;
import com.algoTrader.entity.combination.Allocation;
import com.algoTrader.entity.combination.Combination;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.enumeration.Periodicity;
import com.algoTrader.vo.PortfolioValueVO;

public class LookupUtil {

    public static Security[] getSecuritiesInPortfolio() {

        return ServiceLocator.commonInstance().getLookupService().getAllSecuritiesInPortfolio().toArray(new Security[] {});
    }

    public static StockOption[] getStockOptionsOnWatchlist() {

        return ServiceLocator.commonInstance().getLookupService().getStockOptionsOnWatchlist().toArray(new StockOption[] {});
    }

    public static Future[] getFuturesOnWatchlist() {

        return ServiceLocator.commonInstance().getLookupService().getFuturesOnWatchlist().toArray(new Future[] {});
    }

    public static Future getFutureByDuration(int futureFamilyId, Date targetDate, int duration) {

        return ServiceLocator.commonInstance().getLookupService().getFutureByDuration(futureFamilyId, targetDate, duration);
    }

    public static Security[] getSecuritiesOnWatchlist() {

        return ServiceLocator.commonInstance().getLookupService().getSecuritiesOnWatchlist().toArray(new Security[] {});
    }

    public static Security[] getSecuritiesOnWatchlistByPeriodicity(String periodicityString) {

        Periodicity periodicity = Periodicity.valueOf(periodicityString);
        return ServiceLocator.commonInstance().getLookupService().getSecuritiesOnWatchlistByPeriodicity(periodicity).toArray(new Security[] {});
    }

    public static WatchListItem[] getNonPositionWatchListItem(String strategyName) throws Exception {

        return ServiceLocator.commonInstance().getLookupService().getNonPositionWatchListItem(strategyName).toArray(new WatchListItem[] {});
    }

    public static Security getSecurity(int securityId) {

        return ServiceLocator.commonInstance().getLookupService().getSecurity(securityId);
    }

    public static Security getSecurityFetched(int securityId) {

        return ServiceLocator.commonInstance().getLookupService().getSecurityFetched(securityId);
    }

    public static Security getSecurityByIsin(String isin) {

        return ServiceLocator.commonInstance().getLookupService().getSecurityByIsin(isin);
    }

    public static Position[] getPositions(Security security) {

        return security.getPositions().toArray(new Position[0]);
    }

    public static Position[] getOpenPositions() {

        return ServiceLocator.commonInstance().getLookupService().getOpenPositions().toArray(new Position[] {});
    }

    public static Position[] getOpenPositionsByStrategy(String strategyName) {

        return ServiceLocator.commonInstance().getLookupService().getOpenPositionsByStrategy(strategyName).toArray(new Position[] {});
    }

    public static Position[] getOpenPositionsBySecurityId(int securityId) {

        return ServiceLocator.commonInstance().getLookupService().getOpenPositionsBySecurityId(securityId).toArray(new Position[] {});
    }

    @SuppressWarnings("rawtypes")
    public static Position[] getOpenPositionsByStrategyAndType(String strategyName, String className) throws ClassNotFoundException {

        Class cl = Class.forName(className);
        return ServiceLocator.commonInstance().getLookupService().getOpenPositionsByStrategyAndType(strategyName, cl).toArray(new Position[] {});
    }

    public static Position getPositionBySecurityAndStrategy(int securityId, String strategyName) {

        return ServiceLocator.commonInstance().getLookupService().getPositionBySecurityAndStrategy(securityId, strategyName);
    }

    public static Strategy[] getAllStrategies() {

        return ServiceLocator.commonInstance().getLookupService().getAllStrategies().toArray(new Strategy[] {});
    }

    public static boolean hasOpenPositions() {

        return (ServiceLocator.commonInstance().getLookupService().getOpenPositions().size() != 0);
    }

    public static PortfolioValueVO getPortfolioValue() {

        return ServiceLocator.commonInstance().getLookupService().getPortfolioValue();
    }

    public static boolean hasLastTicks() {

        return (ServiceLocator.commonInstance().getRuleService().getLastEvent(StrategyUtil.getStartedStrategyName(), "GET_LAST_TICK") != null);
    }

    public static Tick getTickByDateAndSecurity(Date date, int securityId) {

        return ServiceLocator.commonInstance().getLookupService().getTickByDateAndSecurity(date, securityId);
    }

    public static WatchListItem getWatchListItem(String strategyName, int securityId) {

        return ServiceLocator.commonInstance().getLookupService().getWatchListItem(strategyName, securityId);
    }

    public static boolean isOnWatchlist(String strategyName, int securityId) {

        return ServiceLocator.commonInstance().getLookupService().getWatchListItem(strategyName, securityId) != null;
    }

    public static Combination getCombination(int combinationId) {

        return ServiceLocator.commonInstance().getLookupService().getCombination(combinationId);
    }

    public static Combination getCombinationByStrategyAndMasterSecurity(String strategyName, int masterSecurityId) {

        return ServiceLocator.commonInstance().getLookupService().getCombinationByStrategyAndMasterSecurity(strategyName, masterSecurityId);
    }

    public static Combination[] getCombinationsByMasterSecurity(int masterSecurityId) {

        return ServiceLocator.commonInstance().getLookupService().getCombinationsByMasterSecurity(masterSecurityId).toArray(new Combination[] {});
    }

    @SuppressWarnings("rawtypes")
    public static Combination[] getCombinationsByStrategyAndType(String strategyName, String className) throws ClassNotFoundException {

        Class cl = Class.forName(className);
        return ServiceLocator.commonInstance().getLookupService().getCombinationsByStrategyAndType(strategyName, cl).toArray(new Combination[] {});
    }

    public static Allocation[] getAllocationsByStrategy(String strategyName) {

        return ServiceLocator.commonInstance().getLookupService().getAllocationsByStrategy(strategyName).toArray(new Allocation[] {});
    }

    @SuppressWarnings("rawtypes")
    public static Allocation[] getAllocationsByStrategyAndType(String strategyName, String className) throws ClassNotFoundException {

        Class cl = Class.forName(className);
        return ServiceLocator.commonInstance().getLookupService().getAllocationsByStrategyAndType(strategyName, cl).toArray(new Allocation[] {});
    }

    public static Allocation getAllocation() {

        return Allocation.Factory.newInstance();
    }
}
