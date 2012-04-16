package com.algoTrader.entity.marketData;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Hibernate;

import com.algoTrader.entity.security.Security;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.vo.RawTickVO;
import com.algoTrader.vo.TickVO;

@SuppressWarnings("unchecked")
public class TickDaoImpl extends TickDaoBase {

    Map<String, Integer> securityIds = new HashMap<String, Integer>();

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

        Tick tick = new TickImpl();
        super.rawTickVOToEntity(rawTickVO, tick, true);

        // cache security id, as queries byIsin get evicted from cache whenever any change to security table happens
        Integer securityId = this.securityIds.get(rawTickVO.getIsin());
        Security security;
        if (securityId != null) {
            security = getSecurityDao().get(securityId);
        } else {
            security = getSecurityDao().findByIsin(rawTickVO.getIsin());
            this.securityIds.put(rawTickVO.getIsin(), security.getId());
        }

        // if security is a proxy, replace it with the implementation
        security = (Security) HibernateUtil.getProxyImplementation(security);

        // initialize the associated proxyies of security
        Hibernate.initialize(security.getUnderlying());
        Hibernate.initialize(security.getSecurityFamily());
        Hibernate.initialize(security.getPositions());

        tick.setSecurity(security);

        return tick;
    }
}
