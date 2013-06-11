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
package ch.algotrader.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.map.SingletonMap;

import ch.algotrader.ServiceLocator;
import ch.algotrader.cache.CacheManager;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.BarImpl;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickImpl;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.security.StockOption;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.esper.EsperManager;
import ch.algotrader.service.LookupService;
import ch.algotrader.service.PortfolioService;
import ch.algotrader.vo.RawBarVO;
import ch.algotrader.vo.RawTickVO;

import com.espertech.esper.collection.Pair;

/**
 * Provides static Lookup methods based mainly on the {@link ch.algotrader.service.LookupService}
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LookupUtil {

    private static final LookupService lookupService = ServiceLocator.instance().getLookupService();
    private static final PortfolioService portfolioService = ServiceLocator.instance().getPortfolioService();
    private static final CacheManager cacheManager = ServiceLocator.instance().getService("cacheManager", CacheManager.class);
    private static final Map<String, Integer> securityIds = new HashMap<String, Integer>();

    /**
     * Gets a Security by its {@code id} and initializes {@link Subscription Subscriptions}, {@link
     * Position Positions}, Underlying {@link Security} and {@link SecurityFamily} to make sure that
     * they are available when the Hibernate Session is closed and this Security is in a detached
     * state.
     */
    public static Security getSecurityInitialized(int securityId) {

        if (cacheManager != null) {
            return cacheManager.get(SecurityImpl.class, securityId);
        } else {
            return lookupService.getSecurityInitialized(securityId);
        }
    }

    /**
     * Gets a Security by its {@code isin}.
     */
    public static Security getSecurityByIsin(String isin) {

        if (cacheManager != null) {

            String queryString = "from SecurityImpl as s where s.isin = :isin";

            Map<String, Object> namedParameters = new SingletonMap<String, Object>("isin", isin);

            return (Security) cacheManager.query(queryString, namedParameters).iterator().next();
        } else {
            return lookupService.getSecurityByIsin(isin);
        }
    }

    /**
     * Gets a {@link SecurityFamily} id by the {@code securityId} of one of its Securities
     */
    public static int getSecurityFamilyIdBySecurity(int securityId) {

        Security security = getSecurityInitialized(securityId);
        return security != null ? security.getSecurityFamily().getId() : 0;
    }

    /**
     * Gets a Subscriptions by the defined {@code strategyName} and {@code securityId}.
     */
    public static Subscription getSubscription(String strategyName, int securityId) {

        if (cacheManager != null) {

            String queryString = "from SubscriptionImpl where strategy.name = :strategyName and security.id = :securityId";

            Map<String, Object> namedParameters = new HashMap<String, Object>();
            namedParameters.put("strategyName", strategyName);
            namedParameters.put("securityId", securityId);

            return (Subscription) cacheManager.query(queryString, namedParameters).iterator().next();
        } else {
            return lookupService.getSubscriptionByStrategyAndSecurity(strategyName, securityId);
        }
    }

    /**
     * Gets all StockOptions that are subscribed by at least one Strategy.
     */
    public static StockOption[] getSubscribedStockOptions() {

        if (cacheManager != null) {

            String queryString = "select distinct s from StockOptionImpl as s join s.subscriptions as s2 where s2 != null order by s.id";

            return cacheManager.query(queryString).toArray(new StockOption[] {});
        } else {
            return lookupService.getSubscribedStockOptions().toArray(new StockOption[] {});
        }
    }

    /**
     * Gets all Futures that are subscribed by at least one Strategy.
     */
    public static Future[] getSubscribedFutures() {

        if (cacheManager != null) {

            String queryString = "select distinct f from FutureImpl as f join f.subscriptions as s where s != null order by f.id";

            return cacheManager.query(queryString).toArray(new Future[] {});
        } else {
            return lookupService.getSubscribedFutures().toArray(new Future[] {});
        }
    }

    /**
     * Gets a Position by Security and Strategy.
     */
    public static Position getPositionBySecurityAndStrategy(int securityId, String strategyName) {

        if (cacheManager != null) {

            String queryString = "select p from PositionImpl as p join p.strategy as s where p.security.id = :securityId and s.name = :strategyName";

            Map<String, Object> namedParameters = new HashMap<String, Object>();
            namedParameters.put("strategyName", strategyName);
            namedParameters.put("securityId", securityId);

            return (Position) cacheManager.query(queryString, namedParameters).iterator().next();
        } else {
            return lookupService.getPositionBySecurityAndStrategy(securityId, strategyName);
        }
    }

    /**
     * Gets all open Position (with a quantity != 0).
     */
    public static Position[] getOpenPositions() {

        if (cacheManager != null) {

            String queryString = "from PositionImpl as p where p.quantity != 0 order by p.security.id";

            return cacheManager.query(queryString).toArray(new Position[] {});
        } else {
            return lookupService.getOpenPositions().toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Strategy.
     */
    public static Position[] getOpenPositionsByStrategy(String strategyName) {

        if (cacheManager != null) {

            String queryString = "from PositionImpl as p where p.strategy.name = :strategyName and p.quantity != 0 order by p.security.id";

            Map<String, Object> namedParameters = new SingletonMap<String, Object>("strategyName", strategyName);

            return cacheManager.query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return lookupService.getOpenPositionsByStrategy(strategyName).toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Security
     */
    public static Position[] getOpenPositionsBySecurity(int securityId) {

        if (cacheManager != null) {

            String queryString = "from PositionImpl as p where p.security.id = :securityId and p.quantity != 0 order by p.id";

            Map<String, Object> namedParameters = new SingletonMap<String, Object>("securityId", securityId);

            return cacheManager.query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return lookupService.getOpenPositionsBySecurity(securityId).toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Strategy and SecurityFamily.
     */
    public static Position[] getOpenPositionsByStrategyAndSecurityFamily(String strategyName, int securityFamily) {

        if (cacheManager != null) {

            String queryString = "from PositionImpl as p where p.strategy.name = :strategyName and p.quantity != 0 and p.security.securityFamily.id = :securityFamilyId order by p.security.id";

            Map<String, Object> namedParameters = new HashMap<String, Object>();
            namedParameters.put("strategyName", strategyName);
            namedParameters.put("securityFamily", securityFamily);

            return cacheManager.query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return lookupService.getOpenPositionsByStrategyAndSecurityFamily(strategyName, securityFamily).toArray(new Position[] {});
        }
    }

    /**
     * Gets the current {@link PortfolioValue} of the system
     */
    public static PortfolioValue getPortfolioValue() {

        return portfolioService.getPortfolioValue();
    }

    /**
     * Returns true if the statement {@code CURRENT_MARKET_DATA_EVENT} contains any {@link MarketDataEvent MarketDataEvents}
     */
    public static boolean hasCurrentMarketDataEvents() {

        String startedStrategyName = ServiceLocator.instance().getConfiguration().getStartedStrategyName();
        return (EsperManager.getLastEvent(startedStrategyName, "CURRENT_MARKET_DATA_EVENT") != null);
    }

    /**
     * Gets the first Tick of the defined Security that is before the maxDate (but not earlier than
     * one minute before that the maxDate).
     */
    public static Tick getTickByDateAndSecurity(int securityId, Date date) {

        return lookupService.getTickBySecurityAndMaxDate(securityId, date);
    }

    /**
     * attaches the fully initialized Security as well as the specified Date to the Tick contained in the {@link Pair}
     */
    public static Tick completeTick(Pair<Tick, Object> pair, Date date) {

        Tick tick = pair.getFirst();

        int securityId = tick.getSecurity().getId();

        Security security = cacheManager.get(SecurityImpl.class, securityId);
        tick.setSecurity(security);
        tick.setDateTime(date);

        return tick;
    }

    /**
     * Same functionality as {@code TickDao#rawTickVOToEntity} which however is only availabe inside a Hibernate Session
     */
    public static Tick rawTickVOToEntity(RawTickVO rawTickVO) {

        Tick tick = new TickImpl();

        // copy all properties
        tick.setLast(rawTickVO.getLast());
        tick.setLastDateTime(rawTickVO.getLastDateTime());
        tick.setBid(rawTickVO.getBid());
        tick.setAsk(rawTickVO.getAsk());
        tick.setVolBid(rawTickVO.getVolBid());
        tick.setVolAsk(rawTickVO.getVolAsk());
        tick.setVol(rawTickVO.getVol());
        tick.setVol(rawTickVO.getVol());
        tick.setOpenIntrest(rawTickVO.getOpenIntrest());
        tick.setSettlement(rawTickVO.getSettlement());

        // cache security id, as queries byIsin get evicted from cache whenever any change to security table happens
        String isin = rawTickVO.getIsin();
        Integer securityId = securityIds.get(isin);
        if (securityId == null) {
            securityId = lookupService.getSecurityByIsin(isin).getId();
            securityIds.put(isin, securityId);
        }

        // get the fully initialized security
        Security security = cacheManager.get(SecurityImpl.class, securityId);
        tick.setSecurity(security);

        return tick;
    }

    /**
     * Same functionality as {@code TickDao#rawBarVOToEntity} which however is only availabe inside a Hibernate Session
     */
    public static Bar rawBarVOToEntity(RawBarVO rawBarVO) {

        Bar bar = new BarImpl();

        // copy all properties
        bar.setBarSize(rawBarVO.getBarSize());
        bar.setOpen(rawBarVO.getOpen());
        bar.setHigh(rawBarVO.getHigh());
        bar.setLow(rawBarVO.getLow());
        bar.setClose(rawBarVO.getClose());
        bar.setDateTime(rawBarVO.getDateTime());
        bar.setVol(rawBarVO.getVol());
        bar.setOpenIntrest(rawBarVO.getOpenIntrest());
        bar.setSettlement(rawBarVO.getSettlement());

        // cache security id, as queries byIsin get evicted from cache whenever any change to security table happens
        String isin = rawBarVO.getIsin();
        Integer securityId = securityIds.get(isin);
        if (securityId == null) {
            securityId = lookupService.getSecurityByIsin(isin).getId();
            securityIds.put(isin, securityId);
        }

        // get the fully initialized security
        Security security = cacheManager.get(SecurityImpl.class, securityId);
        bar.setSecurity(security);

        return bar;
    }
}
