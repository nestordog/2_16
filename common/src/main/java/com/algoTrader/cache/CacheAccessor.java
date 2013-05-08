/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading. The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation, disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading Badenerstrasse 16 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.cache;

import java.util.HashMap;
import java.util.Map;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.Bar;
import com.algoTrader.entity.marketData.BarImpl;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.vo.RawBarVO;
import com.algoTrader.vo.RawTickVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CacheAccessor {

    private CacheManager cacheManager;

    private Map<String, Integer> securityIds = new HashMap<String, Integer>();

    public CacheManager getCacheManager() {
        return this.cacheManager;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Tick rawTickVOToEntity(RawTickVO rawTickVO) {

        long beforeRawToEntity = System.nanoTime();
        Tick tick = new TickImpl();
        rawTickVOToEntity(rawTickVO, tick);
        long afterRawToEntity = System.nanoTime();

        // cache security id, as queries byIsin get evicted from cache whenever any change to security table happens
        long beforeGetSecurityId = System.nanoTime();
        String isin = rawTickVO.getIsin();
        Integer securityId = this.securityIds.get(isin);
        if (securityId == null) {
            securityId = ServiceLocator.instance().getLookupService().getSecurityByIsin(isin).getId();
            this.securityIds.put(isin, securityId);
        }
        long afterGetSecurityId = System.nanoTime();

        // get the fully initialized security
        long beforeSecurityLookup = System.nanoTime();
        Security security = this.cacheManager.get(Security.class, securityId);
        tick.setSecurity(security);
        long afterSecurityLookup = System.nanoTime();

        MetricsUtil.account("MarketDataEventDao.rawToEntity", (afterRawToEntity - beforeRawToEntity));
        MetricsUtil.account("MarketDataEventDao.getSecurityId", (afterGetSecurityId - beforeGetSecurityId));
        MetricsUtil.account("MarketDataEventDao.securityLookup", (afterSecurityLookup - beforeSecurityLookup));

        return tick;
    }

    public Bar rawBarVOToEntity(RawBarVO barVO) {

        long beforeRawToEntity = System.nanoTime();
        Bar bar = new BarImpl();
        rawBarVOToEntity(barVO, bar);
        long afterRawToEntity = System.nanoTime();

        // cache security id, as queries byIsin get evicted from cache whenever any change to security table happens
        long beforeGetSecurityId = System.nanoTime();
        String isin = barVO.getIsin();
        Integer securityId = this.securityIds.get(isin);
        if (securityId == null) {
            securityId = ServiceLocator.instance().getLookupService().getSecurityByIsin(isin).getId();
            this.securityIds.put(isin, securityId);
        }
        long afterGetSecurityId = System.nanoTime();

        // get the fully initialized security
        long beforeSecurityLookup = System.nanoTime();
        Security security = this.cacheManager.get(Security.class, securityId);
        bar.setSecurity(security);
        long afterSecurityLookup = System.nanoTime();

        MetricsUtil.account("MarketDataEventDao.rawToEntity", (afterRawToEntity - beforeRawToEntity));
        MetricsUtil.account("MarketDataEventDao.getSecurityId", (afterGetSecurityId - beforeGetSecurityId));
        MetricsUtil.account("MarketDataEventDao.securityLookup", (afterSecurityLookup - beforeSecurityLookup));

        return bar;
    }

    public void rawTickVOToEntity(RawTickVO source, Tick target) {

        target.setLast(source.getLast());
        target.setLastDateTime(source.getLastDateTime());
        target.setBid(source.getBid());
        target.setAsk(source.getAsk());
        target.setVolBid(source.getVolBid());
        target.setVolAsk(source.getVolAsk());
        target.setVol(source.getVol());
        target.setVol(source.getVol());
        target.setOpenIntrest(source.getOpenIntrest());
        target.setSettlement(source.getSettlement());
    }

    public void rawBarVOToEntity(RawBarVO source, Bar target) {

        target.setBarSize(source.getBarSize());
        target.setOpen(source.getOpen());
        target.setHigh(source.getHigh());
        target.setLow(source.getLow());
        target.setClose(source.getClose());
        target.setDateTime(source.getDateTime());
        target.setVol(source.getVol());
        target.setOpenIntrest(source.getOpenIntrest());
        target.setSettlement(source.getSettlement());
    }
}
