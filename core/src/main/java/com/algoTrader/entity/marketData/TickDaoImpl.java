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
package com.algoTrader.entity.marketData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.strategy.StrategyImpl;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.vo.RawTickVO;
import com.algoTrader.vo.TickVO;
import com.espertech.esper.collection.Pair;

@SuppressWarnings("unchecked")
/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickDaoImpl extends TickDaoBase {

    private Map<String, Integer> securityIds = new HashMap<String, Integer>();

    @Override
    public void toTickVO(Tick tick, TickVO tickVO) {

        super.toTickVO(tick, tickVO);

        completeTickVO(tick, tickVO);
    }

    @Override
    public TickVO toTickVO(final Tick tick) {

        TickVO tickVO = super.toTickVO(tick);

        completeTickVO(tick, tickVO);

        return tickVO;
    }

    @Override
    public void toRawTickVO(Tick tick, RawTickVO rawTickVO) {

        super.toRawTickVO(tick, rawTickVO);

        completeRawTickVO(tick, rawTickVO);
    }

    @Override
    public RawTickVO toRawTickVO(final Tick tick) {

        RawTickVO rawTickVO = super.toRawTickVO(tick);

        completeRawTickVO(tick, rawTickVO);

        return rawTickVO;
    }

    private void completeTickVO(Tick tick, TickVO tickVO) {

        tickVO.setSecurityId(tick.getSecurity().getId());
        tickVO.setCurrentValue(tick.getCurrentValue());
    }

    private void completeRawTickVO(Tick tick, RawTickVO rawTickVO) {

        rawTickVO.setIsin(tick.getSecurity().getIsin());
    }

    @Override
    public Tick tickVOToEntity(TickVO tickVO) {

        throw new UnsupportedOperationException("tickVOToEntity not yet implemented.");
    }

    @Override
    public Tick rawTickVOToEntity(RawTickVO rawTickVO) {

        long beforeRawToEntity = System.nanoTime();
        Tick tick = new TickImpl();
        super.rawTickVOToEntity(rawTickVO, tick, true);
        long afterRawToEntity = System.nanoTime();

        // cache security id, as queries byIsin get evicted from cache whenever any change to security table happens
        long beforeGetSecurityId = System.nanoTime();
        String isin = rawTickVO.getIsin();
        Integer securityId = this.securityIds.get(isin);
        if (securityId == null) {
            securityId = getSecurityDao().findSecurityIdByIsin(isin);
            this.securityIds.put(isin, securityId);
        }
        long afterGetSecurityId = System.nanoTime();

        // get the fully initialized security
        long beforeSecurityLookup = System.nanoTime();
        Security security = getSecurityDao().get(securityId);
        long afterSecurityLookup = System.nanoTime();

        long beforeInitialization = System.nanoTime();
        security.initialize();
        tick.setSecurity(security);
        long afterInitialization = System.nanoTime();

        MetricsUtil.account("MarketDataEventDao.rawToEntity", (afterRawToEntity - beforeRawToEntity));
        MetricsUtil.account("MarketDataEventDao.getSecurityId", (afterGetSecurityId - beforeGetSecurityId));
        MetricsUtil.account("MarketDataEventDao.securityLookup", (afterSecurityLookup - beforeSecurityLookup));
        MetricsUtil.account("MarketDataEventDao.initialization", (afterInitialization - beforeInitialization));


        return tick;
    }

    @Override
    protected Integer handleFindTickerIdBySecurity(int securityId) throws Exception {

        // sometimes Esper returns a Map instead of scalar
        String query = "select tickerId from TickWindow where security.id = " + securityId;
        Object obj = EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, query);
        if (obj instanceof Map) {
            return ((Map<String, Integer>) obj).get("tickerId");
        } else {
            return (Integer) obj;
        }
    }

    @Override
    protected Collection<Tick> handleFindCurrentTicksByStrategy(String strategyName) throws Exception {

        List<Subscription> subscriptions = getSubscriptionDao().findByStrategy(strategyName);

        Collection<Tick> ticks = new ArrayList<Tick>();
        for (Subscription subscription : subscriptions) {
            String query = "select * from TickWindow where security.id = " + subscription.getSecurity().getId();
            Pair<Tick, Object> pair = (Pair<Tick, Object>)EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, query);
            if (pair != null) {

                Tick tick = pair.getFirst();
                tick.setDateTime(new Date());

                // refresh the security (associated entities might have been modified
                Security security = ServiceLocator.instance().getLookupService().getSecurityInitialized(tick.getSecurity().getId());
                tick.setSecurity(security);

                if (security.validateTick(tick)) {
                    ticks.add(tick);
                }
            }
        }

        return ticks;
    }
}
