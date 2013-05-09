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
import com.algoTrader.vo.SABRSurfaceVO;
import com.espertech.esper.collection.Pair;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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

    public static Combination getCombinationInclComponentsInitialized(int securityId) throws java.lang.Exception {

        return ServiceLocator.instance().getLookupService().getCombinationInclComponentsInitialized(securityId);
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

    public static Position[] getOpenPositionsBySecurity(int securityId) {

        return ServiceLocator.instance().getLookupService().getOpenPositionsBySecurity(securityId).toArray(new Position[] {});
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

    public static Tick getTickByDateAndSecurity(int securityId, Date date) {

        return ServiceLocator.instance().getLookupService().getTickBySecurityAndMaxDate(securityId, date);
    }

    public static Subscription getSubscription(String strategyName, int securityId) {

        return ServiceLocator.instance().getLookupService().getSubscription(strategyName, securityId);
    }

    public static Combination getCombination(int combinationId) {

        return (Combination) ServiceLocator.instance().getLookupService().getSecurity(combinationId);
    }

    public static Component[] getComponentsByStrategy(String strategyName) {

        return ServiceLocator.instance().getLookupService().getSubscribedComponentsByStrategyInclSecurity(strategyName).toArray(new Component[] {});
    }

    public static Component[] getComponentsBySecurity(int securityId) {

        return ServiceLocator.instance().getLookupService().getSubscribedComponentsBySecurityInclSecurity(securityId).toArray(new Component[] {});
    }

    public static Tick completeTick(Pair<Tick, Object> pair, Date date) {

        Tick tick = pair.getFirst();
        tick.setDateTime(date);
        tick.setSecurity(ServiceLocator.instance().getLookupService().getSecurityInitialized(tick.getSecurity().getId()));
        return tick;
    }

    public static SABRSurfaceVO getSABRSurface(int underlyingId, Date date) {

        return ServiceLocator.instance().getStockOptionService().calibrateSABRSurfaceByIVol(underlyingId, date);
    }
}
