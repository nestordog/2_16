package com.algoTrader.util;

import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.WatchListItem;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.enumeration.Periodicity;
import com.algoTrader.vo.PortfolioValueVO;

public class LookupUtil {

    public static Security[] getSecuritiesInPortfolio() {

        return ServiceLocator.commonInstance().getLookupService().getAllSecuritiesInPortfolio();
    }

    public static StockOption[] getStockOptionsOnWatchlist() {

        return ServiceLocator.commonInstance().getLookupService().getStockOptionsOnWatchlist();
    }

    public static Future[] getFuturesOnWatchlist() {

        return ServiceLocator.commonInstance().getLookupService().getFuturesOnWatchlist();
    }

    public static Future getFutureByDuration(int futureFamilyId, Date targetDate, int duration) {

        return ServiceLocator.commonInstance().getLookupService().getFutureByDuration(futureFamilyId, targetDate, duration);
    }

    public static Security[] getSecuritiesOnWatchlist() {

        return ServiceLocator.commonInstance().getLookupService().getSecuritiesOnWatchlist();
    }

    public static Security[] getSecuritiesOnWatchlistByPeriodicity(String periodicityString) {

        Periodicity periodicity = Periodicity.valueOf(periodicityString);
        return ServiceLocator.commonInstance().getLookupService().getSecuritiesOnWatchlistByPeriodicity(periodicity);
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

        return ServiceLocator.commonInstance().getLookupService().getOpenPositions();
    }

    public static Position[] getOpenPositionsByStrategy(String strategyName) {

        return ServiceLocator.commonInstance().getLookupService().getOpenPositionsByStrategy(strategyName);
    }

    public static Position[] getOpenPositionsBySecurityId(int securityId) {

        Position[] positions = ServiceLocator.commonInstance().getLookupService().getOpenPositionsBySecurityId(securityId);
        return positions;
    }

    public static Position[] getBullishPositionsByStrategy(String strategyName) {

        return ServiceLocator.commonInstance().getLookupService().getBullishPositionsByStrategy(strategyName);
    }

    public static Position[] getBearishPositionsByStrategy(String strategyName) {

        return ServiceLocator.commonInstance().getLookupService().getBearishPositionsByStrategy(strategyName);
    }

    public static Strategy[] getAllStrategies() {

        return ServiceLocator.commonInstance().getLookupService().getAllStrategies();
    }

    public static boolean hasOpenPositions() {

        return (ServiceLocator.commonInstance().getLookupService().getOpenPositions().length != 0);
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
}
