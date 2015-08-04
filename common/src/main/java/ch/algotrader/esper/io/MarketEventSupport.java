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
package ch.algotrader.esper.io;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.algotrader.ServiceLocator;
import ch.algotrader.cache.CacheManager;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.service.ServerLookupService;
import ch.algotrader.vo.RawBarVO;
import ch.algotrader.vo.RawTickVO;

/**
 * Provides convenience factory methods for market event entities.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
final class MarketEventSupport {

    private static final ConcurrentMap<String, Long> SECURITY_ID_BY_SECURITY_STRING = new ConcurrentHashMap<>();

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
        final ServiceLocator serviceLocator = ServiceLocator.instance();

        //first, lookup the security ID (does normally not change)
        Long securityId = SECURITY_ID_BY_SECURITY_STRING.get(securityString);
        if (securityId == null) {
            // lookup the securityId
            ServerLookupService serverLookupService = ServiceLocator.instance().getService("serverLookupService", ServerLookupService.class);
            securityId = serverLookupService.getSecurityIdBySecurityString(securityString);
            SECURITY_ID_BY_SECURITY_STRING.put(securityString, securityId);//due to racing we may replace an existing entry but that's fine
        }

        // now get the fully initialized security (may change hence do a lookup with cache manager who knows about changes)
        final CacheManager cacheManager = serviceLocator.getService("cacheManager", CacheManager.class);
        return cacheManager.get(SecurityImpl.class, securityId);
    }
}
