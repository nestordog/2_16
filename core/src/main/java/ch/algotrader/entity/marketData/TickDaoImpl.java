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
package ch.algotrader.entity.marketData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.espertech.esper.collection.Pair;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.security.Security;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.vo.RawTickVO;
import ch.algotrader.vo.TickVO;

@SuppressWarnings("unchecked")
/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
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

    /**
     * set the FileName to the first non-null value of isin, symbol, bbgid, ric and conid or id
     */
    private void completeRawTickVO(Tick tick, RawTickVO rawTickVO) {

        Security security = tick.getSecurity();
        if (security.getIsin() != null) {
            rawTickVO.setSecurity(security.getIsin());
        } else if (security.getSymbol() != null) {
            rawTickVO.setSecurity(security.getSymbol());
        } else if (security.getBbgid() != null) {
            rawTickVO.setSecurity(security.getBbgid());
        } else if (security.getRic() != null) {
            rawTickVO.setSecurity(security.getRic());
        } else if (security.getConid() != null) {
            rawTickVO.setSecurity(security.getConid());
        } else {
            rawTickVO.setSecurity(String.valueOf(security.getId()));
        }
    }

    @Override
    public Tick tickVOToEntity(TickVO tickVO) {

        throw new UnsupportedOperationException("tickVOToEntity not yet implemented.");
    }

    @Override
    public Tick rawTickVOToEntity(RawTickVO rawTickVO) {

        throw new UnsupportedOperationException("not implemented (LookupUtil.rawTickVOToEntity(RawTickVO)");
    }

    @Override
    protected String handleFindTickerIdBySecurity(int securityId) throws Exception {

        // sometimes Esper returns a Map instead of scalar
        String query = "select tickerId from TickWindow where security.id = " + securityId;
        Object obj = EngineLocator.instance().getServerEngine().executeSingelObjectQuery(query);
        if (obj instanceof Map) {
            return ((Map<String, String>) obj).get("tickerId");
        } else {
            return (String) obj;
        }
    }

    @Override
    protected Collection<Tick> handleFindCurrentTicksByStrategy(String strategyName) throws Exception {

        List<Subscription> subscriptions = getSubscriptionDao().findByStrategy(strategyName);

        Collection<Tick> ticks = new ArrayList<Tick>();
        for (Subscription subscription : subscriptions) {
            String query = "select * from TickWindow where security.id = " + subscription.getSecurity().getId();
            Pair<Tick, Object> pair = (Pair<Tick, Object>) EngineLocator.instance().getServerEngine().executeSingelObjectQuery(query);
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
