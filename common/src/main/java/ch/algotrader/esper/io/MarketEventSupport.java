/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.esper.io;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ch.algotrader.ServiceLocator;
import ch.algotrader.cache.CacheManager;
import ch.algotrader.entity.marketData.BarVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityImpl;
import ch.algotrader.service.ServerLookupService;

/**
 * Provides convenience factory methods for market event entities.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
final class MarketEventSupport {

    private static final ConcurrentMap<String, Long> SECURITY_ID_BY_SECURITY_STRING = new ConcurrentHashMap<>();

    public static TickVO rawTickToEvent(RawTickVO raw) {

        Security security = getSecurity(raw.getSecurity());
        return new TickVO(0L, raw.getDateTime(), "SIM", security.getId(), raw.getLast(), raw.getLastDateTime(),
                raw.getBid(), raw.getAsk(), raw.getVol(), raw.getVolAsk(), raw.getVolBid());
    }

    public static BarVO rawBarToEvent(RawBarVO raw) {

        Security security = getSecurity(raw.getSecurity());
        return new BarVO(0L, raw.getDateTime(), "SIM", security.getId(), raw.getBarSize(), raw.getOpen(), raw.getHigh(), raw.getLow(), raw.getClose(), raw.getVol());
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
