/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.map.SingletonMap;

import com.espertech.esper.collection.Pair;

import ch.algotrader.ServiceLocator;
import ch.algotrader.cache.CacheManager;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.vo.RawBarVO;
import ch.algotrader.vo.RawTickVO;

/**
 * Provides static Lookup methods based mainly on the {@link ch.algotrader.service.LookupService}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LookupUtil {

    private static CacheManager getCacheManager() {
        return ServiceLocator.instance().containsService("cacheManager") ? ServiceLocator.instance().getService("cacheManager", CacheManager.class) : null;
    }

    /**
     * Gets a Security by its {@code id} and initializes {@link Subscription Subscriptions}, {@link
     * Position Positions}, Underlying {@link Security} and {@link ch.algotrader.entity.security.SecurityFamily} to make sure that
     * they are available when the Hibernate Session is closed and this Security is in a detached
     * state.
     */
    public static Security getSecurityInitialized(int securityId) {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {
            return cacheManager.get(SecurityImpl.class, securityId);
        } else {
            return ServiceLocator.instance().getLookupService().getSecurityInitialized(securityId);
        }
    }

    /**
     * Gets a Security by its {@code isin}.
     */
    public static Security getSecurityByIsin(String isin) {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "from SecurityImpl as s where s.isin = :isin";

            Map<String, Object> namedParameters = new SingletonMap<String, Object>("isin", isin);

            return (Security) CollectionUtil.getSingleElementOrNull(cacheManager.query(queryString, namedParameters));
        } else {
            return ServiceLocator.instance().getLookupService().getSecurityByIsin(isin);
        }
    }

    /**
     * Gets a {@link ch.algotrader.entity.security.SecurityFamily} id by the {@code securityId} of one of its Securities
     */
    public static int getSecurityFamilyIdBySecurity(int securityId) {

        Security security = getSecurityInitialized(securityId);
        return security != null ? security.getSecurityFamily().getId() : 0;
    }

    /**
     * Gets a Subscriptions by the defined {@code strategyName} and {@code securityId}.
     */
    public static Subscription getSubscription(String strategyName, int securityId) {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "from SubscriptionImpl where strategy.name = :strategyName and security.id = :securityId";

            Map<String, Object> namedParameters = new HashMap<String, Object>();
            namedParameters.put("strategyName", strategyName);
            namedParameters.put("securityId", securityId);

            return (Subscription) CollectionUtil.getSingleElementOrNull(cacheManager.query(queryString, namedParameters));
        } else {
            return ServiceLocator.instance().getLookupService().getSubscriptionByStrategyAndSecurity(strategyName, securityId);
        }
    }

    /**
     * Gets all Options that are subscribed by at least one Strategy.
     */
    public static Option[] getSubscribedOptions() {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "select distinct s from OptionImpl as s join s.subscriptions as s2 where s2 != null order by s.id";

            return cacheManager.query(queryString).toArray(new Option[] {});
        } else {
            return ServiceLocator.instance().getLookupService().getSubscribedOptions().toArray(new Option[] {});
        }
    }

    /**
     * Gets all Futures that are subscribed by at least one Strategy.
     */
    public static Future[] getSubscribedFutures() {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "select distinct f from FutureImpl as f join f.subscriptions as s where s != null order by f.id";

            return cacheManager.query(queryString).toArray(new Future[] {});
        } else {
            return ServiceLocator.instance().getLookupService().getSubscribedFutures().toArray(new Future[] {});
        }
    }

    /**
     * Gets a Position by Security and Strategy.
     */
    public static Position getPositionBySecurityAndStrategy(int securityId, String strategyName) {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "select p from PositionImpl as p join p.strategy as s where p.security.id = :securityId and s.name = :strategyName";

            Map<String, Object> namedParameters = new HashMap<String, Object>();
            namedParameters.put("strategyName", strategyName);
            namedParameters.put("securityId", securityId);

            return (Position) CollectionUtil.getSingleElementOrNull(cacheManager.query(queryString, namedParameters));
        } else {
            return ServiceLocator.instance().getLookupService().getPositionBySecurityAndStrategy(securityId, strategyName);
        }
    }

    /**
     * Gets all open Position (with a quantity != 0).
     */
    public static Position[] getOpenPositions() {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "from PositionImpl as p where p.quantity != 0 order by p.security.id";

            return cacheManager.query(queryString).toArray(new Position[] {});
        } else {
            return ServiceLocator.instance().getLookupService().getOpenPositions().toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Strategy.
     */
    public static Position[] getOpenPositionsByStrategy(String strategyName) {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "from PositionImpl as p where p.strategy.name = :strategyName and p.quantity != 0 order by p.security.id";

            Map<String, Object> namedParameters = new SingletonMap<String, Object>("strategyName", strategyName);

            return cacheManager.query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return ServiceLocator.instance().getLookupService().getOpenPositionsByStrategy(strategyName).toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Security
     */
    public static Position[] getOpenPositionsBySecurity(int securityId) {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "from PositionImpl as p where p.security.id = :securityId and p.quantity != 0 order by p.id";

            Map<String, Object> namedParameters = new SingletonMap<String, Object>("securityId", securityId);

            return cacheManager.query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return ServiceLocator.instance().getLookupService().getOpenPositionsBySecurity(securityId).toArray(new Position[] {});
        }
    }

    /**
     * Gets open Positions for the specified Strategy and SecurityFamily.
     */
    public static Position[] getOpenPositionsByStrategyAndSecurityFamily(String strategyName, int securityFamilyId) {

        CacheManager cacheManager = getCacheManager();
        if (cacheManager != null) {

            String queryString = "from PositionImpl as p where p.strategy.name = :strategyName and p.quantity != 0 and p.security.securityFamily.id = :securityFamilyId order by p.security.id";

            Map<String, Object> namedParameters = new HashMap<String, Object>();
            namedParameters.put("strategyName", strategyName);
            namedParameters.put("securityFamilyId", securityFamilyId);

            return cacheManager.query(queryString, namedParameters).toArray(new Position[] {});
        } else {
            return ServiceLocator.instance().getLookupService().getOpenPositionsByStrategyAndSecurityFamily(strategyName, securityFamilyId).toArray(new Position[] {});
        }
    }

    /**
     * Gets the current {@link PortfolioValue} of the system
     */
    public static PortfolioValue getPortfolioValue() {

        return ServiceLocator.instance().getPortfolioService().getPortfolioValue();
    }

    /**
     * Returns true if the MarketDataWindow contains any {@link ch.algotrader.entity.marketData.MarketDataEvent MarketDataEvents}
     */
    @SuppressWarnings("unchecked")
    public static boolean hasCurrentMarketDataEvents() {

        String startedStrategyName = ConfigLocator.instance().getCommonConfig().getStartedStrategyName();
        Map<String, Long> map = (Map<String, Long>) EngineLocator.instance().getEngine(startedStrategyName).executeSingelObjectQuery("select count(*) as cnt from MarketDataWindow");
        return (map.get("cnt") > 0);
    }

    /**
     * Gets the first Tick of the defined Security that is before the maxDate (but not earlier than
     * one minute before that the maxDate).
     */
    public static Tick getTickByDateAndSecurity(int securityId, Date date) {

        return ServiceLocator.instance().getLookupService().getTickBySecurityAndMaxDate(securityId, date);
    }

    /**
     * attaches the fully initialized Security as well as the specified Date to the Tick contained in the {@link Pair}
     */
    public static Tick completeTick(Pair<Tick, Object> pair) {

        Tick tick = pair.getFirst();

        int securityId = tick.getSecurity().getId();

        CacheManager cacheManager = getCacheManager();
        // TODO: decide what to do about cache manager being null
        Security security = cacheManager.get(SecurityImpl.class, securityId);
        tick.setSecurity(security);

        return tick;
    }

    /**
     * Same functionality as {@code TickDao#rawTickVOToEntity} which however is only availabe inside a Hibernate Session
     */
    public static Tick rawTickVOToEntity(RawTickVO rawTickVO) {

        Tick tick = Tick.Factory.newInstance();

        // copy all properties
        tick.setDateTime(rawTickVO.getDateTime());
        tick.setVol(rawTickVO.getVol());
        tick.setLast(rawTickVO.getLast());
        tick.setLastDateTime(rawTickVO.getLastDateTime());
        tick.setBid(rawTickVO.getBid());
        tick.setAsk(rawTickVO.getAsk());
        tick.setVolBid(rawTickVO.getVolBid());
        tick.setVolAsk(rawTickVO.getVolAsk());

        // cache securities, as queries by isin, symbol etc. get evicted from cache whenever any change to security table happens
        String securityString = rawTickVO.getSecurity();
        Security security = getSecurity(securityString);
        tick.setSecurity(security);

        return tick;
    }

    /**
     * Same functionality as {@code TickDao#rawBarVOToEntity} which however is only availabe inside a Hibernate Session
     */
    public static Bar rawBarVOToEntity(RawBarVO rawBarVO) {

        Bar bar = Bar.Factory.newInstance();

        // copy all properties
        bar.setDateTime(rawBarVO.getDateTime());
        bar.setVol(rawBarVO.getVol());
        bar.setBarSize(rawBarVO.getBarSize());
        bar.setOpen(rawBarVO.getOpen());
        bar.setHigh(rawBarVO.getHigh());
        bar.setLow(rawBarVO.getLow());
        bar.setClose(rawBarVO.getClose());

        // cache securities, as queries by isin, symbol etc. get evicted from cache whenever any change to security table happens
        String securityString = rawBarVO.getSecurity();
        Security security = getSecurity(securityString);
        bar.setSecurity(security);

        return bar;
    }

    private static Security getSecurity(String securityString) {

        // lookup the securityId
        int securityId = ServiceLocator.instance().getLookupService().getSecurityIdBySecurityString(securityString);

        CacheManager cacheManager = getCacheManager();
        // TODO: decide what to do about cache manager being null
        // get the fully initialized security
        return cacheManager.get(SecurityImpl.class, securityId);
    }
}
