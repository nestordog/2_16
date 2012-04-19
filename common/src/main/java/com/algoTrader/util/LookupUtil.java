package com.algoTrader.util;

import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Combination;
import com.algoTrader.entity.security.Component;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.enumeration.Period;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.vo.PortfolioValueVO;

public class LookupUtil {

    public static StockOption[] getSubscribedStockOptions() {

        return ServiceLocator.instance().getLookupService().getSubscribedStockOptions().toArray(new StockOption[] {});
    }

    public static Future[] getSubscribedFutures() {

        return ServiceLocator.instance().getLookupService().getSubscribedFutures().toArray(new Future[] {});
    }

    public static Future getFutureByDuration(int futureFamilyId, Date targetDate, int duration) {

        return ServiceLocator.instance().getLookupService().getFutureByDuration(futureFamilyId, targetDate, duration);
    }

    public static Security[] getSubscribedSecurities() {

        return ServiceLocator.instance().getLookupService().getSubscribedSecuritiesInclFamily().toArray(new Security[] {});
    }

    public static Security[] getSubscribedSecuritiesByPeriodicity(String periodicityString) {

        Period periodicity = Period.valueOf(periodicityString);
        return ServiceLocator.instance().getLookupService().getSubscribedSecuritiesByPeriodicityInclFamily(periodicity).toArray(new Security[] {});
    }

    public static Subscription[] getNonPositionSubscriptions(String strategyName) throws Exception {

        return ServiceLocator.instance().getLookupService().getNonPositionSubscriptions(strategyName).toArray(new Subscription[] {});
    }

    public static Security getSecurity(int securityId) {

        return ServiceLocator.instance().getLookupService().getSecurity(securityId);
    }

    public static Security getSecurityInclFamilyAndUnderlying(int securityId) {

        return ServiceLocator.instance().getLookupService().getSecurityInclFamilyAndUnderlying(securityId);
    }

    public static Security getSecurityInclFamilyUnderlyingAndComponents(int securityId) {

        return ServiceLocator.instance().getLookupService().getSecurityInclFamilyUnderlyingAndComponents(securityId);
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

    @SuppressWarnings("rawtypes")
    public static Position[] getOpenPositionsByStrategyAndType(String strategyName, String className) throws ClassNotFoundException {

        Class cl = Class.forName(className);
        return ServiceLocator.instance().getLookupService().getOpenPositionsByStrategyAndType(strategyName, cl).toArray(new Position[] {});
    }

    public static Position getPositionBySecurityAndStrategy(int securityId, String strategyName) {

        return ServiceLocator.instance().getLookupService().getPositionBySecurityAndStrategy(securityId, strategyName);
    }

    public static Strategy[] getAllStrategies() {

        return ServiceLocator.instance().getLookupService().getAllStrategies().toArray(new Strategy[] {});
    }

    public static boolean hasOpenPositions() {

        return (ServiceLocator.instance().getLookupService().getOpenPositions().size() != 0);
    }

    public static PortfolioValueVO getPortfolioValue() {

        return ServiceLocator.instance().getLookupService().getPortfolioValue();
    }

    public static boolean hasLastTicks() {

        return (EsperManager.getLastEvent(StrategyUtil.getStartedStrategyName(), "GET_LAST_TICK") != null);
    }

    public static Tick getTickByDateAndSecurity(Date date, int securityId) {

        return ServiceLocator.instance().getLookupService().getTickByDateAndSecurity(date, securityId);
    }

    public static Subscription getSubscription(String strategyName, int securityId) {

        return ServiceLocator.instance().getLookupService().getSubscription(strategyName, securityId);
    }

    public static boolean isSubscribed(String strategyName, int securityId) {

        return ServiceLocator.instance().getLookupService().getSubscription(strategyName, securityId) != null;
    }

    public static Combination getCombination(int combinationId) {

        return (Combination) ServiceLocator.instance().getLookupService().getSecurity(combinationId);
    }

    @SuppressWarnings("rawtypes")
    public static Combination[] getCombinationsByStrategyAndComponentClass(String strategyName, String className) throws ClassNotFoundException {

        Class cl = Class.forName(className);
        return ServiceLocator.instance().getLookupService().getSubscribedSecuritiesByStrategyAndComponentClass(strategyName, cl).toArray(new Combination[] {});
    }

    public static Component[] getAllSubscribedComponents() {

        return ServiceLocator.instance().getLookupService().getAllSubscribedComponents().toArray(new Component[] {});
    }

    public static Component[] getComponentsByStrategy(String strategyName) {

        return ServiceLocator.instance().getLookupService().getSubscribedComponentsByStrategy(strategyName).toArray(new Component[] {});
    }

    public static Component[] getComponentsBySecurity(int securityId) {

        return ServiceLocator.instance().getLookupService().getSubscribedComponentsBySecurity(securityId).toArray(new Component[] {});
    }

    @SuppressWarnings("rawtypes")
    public static Component[] getComponentsByStrategyAndClass(String strategyName, String className) throws ClassNotFoundException {

        Class cl = Class.forName(className);
        return ServiceLocator.instance().getLookupService().getSubscribedComponentsByStrategyAndClass(strategyName, cl).toArray(new Component[] {});
    }

    public static Component getNewComponentInstance() {

        return Component.Factory.newInstance();
    }

    public static long getComponentCount(int securityId) {

        return ServiceLocator.instance().getLookupService().getComponentCount(securityId);
    }
}
