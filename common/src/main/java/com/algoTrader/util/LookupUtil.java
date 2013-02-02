package com.algoTrader.util;

import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Combination;
import com.algoTrader.entity.security.Component;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.esper.EsperManager;
import com.espertech.esper.collection.Pair;

public class LookupUtil {

    public static StockOption[] getSubscribedStockOptions() {

        return ServiceLocator.instance().getLookupService().getSubscribedStockOptions().toArray(new StockOption[] {});
    }

    public static Future[] getSubscribedFutures() {

        return ServiceLocator.instance().getLookupService().getSubscribedFutures().toArray(new Future[] {});
    }

    public static Subscription[] getNonPositionSubscriptions(String strategyName) throws Exception {

        return ServiceLocator.instance().getLookupService().getNonPositionSubscriptions(strategyName).toArray(new Subscription[] {});
    }

    public static Security getSecurity(int securityId) {

        return ServiceLocator.instance().getLookupService().getSecurity(securityId);
    }

    public static int getSecurityFamilyIdBySecurity(int securityId) {

        Security security = getSecurity(securityId);
        return security != null ? security.getSecurityFamily().getId() : 0;
    }

    public static Security getSecurityInitialized(int securityId) throws java.lang.Exception {

        return ServiceLocator.instance().getLookupService().getSecurityInitialized(securityId);
    }

    public static Security getSecurityInclComponentsInitialized(int securityId) throws java.lang.Exception {

        return ServiceLocator.instance().getLookupService().getSecurityInclComponentsInitialized(securityId);
    }

    public static Security getSecurityByIsin(String isin) {

        return ServiceLocator.instance().getLookupService().getSecurityByIsin(isin);
    }

    public static Position[] getPositions(Security security) {

        return security.getPositions().toArray(new Position[0]);
    }

    public static Position[] getOpenPositions() {

        return ServiceLocator.instance().getLookupService().getOpenPositions().toArray(new Position[] {});
    }

    public static Position[] getOpenPositionsByStrategy(String strategyName) {

        return ServiceLocator.instance().getLookupService().getOpenPositionsByStrategy(strategyName).toArray(new Position[] {});
    }

    public static Position[] getOpenPositionsBySecurityId(int securityId) {

        return ServiceLocator.instance().getLookupService().getOpenPositionsBySecurityId(securityId).toArray(new Position[] {});
    }

    public static Position[] getOpenPositionsByStrategyAndSecurityFamily(String strategyName, int securityFamily) {

        return ServiceLocator.instance().getLookupService().getOpenPositionsByStrategyAndSecurityFamily(strategyName, securityFamily).toArray(new Position[] {});
    }

    public static boolean hasOpenPositions(String strategyName) {

        return ServiceLocator.instance().getLookupService().getOpenPositionsByStrategy(strategyName).size() > 0;
    }

    public static Position getPositionBySecurityAndStrategy(int securityId, String strategyName) {

        return ServiceLocator.instance().getLookupService().getPositionBySecurityAndStrategy(securityId, strategyName);
    }

    public static PortfolioValue getPortfolioValue() {

        return ServiceLocator.instance().getPortfolioService().getPortfolioValue();
    }

    public static boolean hasCurrentMarketDataEvents() {

        return (EsperManager.getLastEvent(StrategyUtil.getStartedStrategyName(), "CURRENT_MARKET_DATA_EVENT") != null);
    }

    public static Tick getTickByDateAndSecurityInclSecurityInitialized(Date date, int securityId) {

        return ServiceLocator.instance().getLookupService().getTickByDateAndSecurityInclSecurityInitialized(date, securityId);
    }

    public static Subscription getSubscription(String strategyName, int securityId) {

        return ServiceLocator.instance().getLookupService().getSubscription(strategyName, securityId);
    }

    public static Combination getCombination(int combinationId) {

        return (Combination) ServiceLocator.instance().getLookupService().getSecurity(combinationId);
    }

    public static Component[] getComponentsByStrategy(String strategyName) {

        return ServiceLocator.instance().getLookupService().getSubscribedComponentsByStrategy(strategyName).toArray(new Component[] {});
    }

    public static Component[] getComponentsBySecurity(int securityId) {

        return ServiceLocator.instance().getLookupService().getSubscribedComponentsBySecurity(securityId).toArray(new Component[] {});
    }

    public static Tick completeTick(Pair<Tick, Object> pair, Date date) {

        Tick tick = pair.getFirst();
        tick.setDateTime(date);
        tick.setSecurity(ServiceLocator.instance().getLookupService().getSecurityInitialized(tick.getSecurity().getId()));
        return tick;
    }
}
