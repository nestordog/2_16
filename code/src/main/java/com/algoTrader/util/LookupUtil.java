package com.algoTrader.util;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
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

    public static Security[] getSecuritiesOnWatchlistByPeriodicity(String periodicityString) {

        Periodicity periodicity = Periodicity.valueOf(periodicityString);
        return ServiceLocator.commonInstance().getLookupService().getSecuritiesOnWatchlistByPeriodicity(periodicity);
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
}
