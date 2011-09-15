package com.algoTrader.entity.marketData;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.algoTrader.entity.security.Security;
import com.algoTrader.vo.RawTickVO;
import com.algoTrader.vo.TickVO;

public class TickDaoImpl extends TickDaoBase {

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
        tickVO.setSymbol(tick.getSecurity().getSymbol());
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

        Security security = getSecurityDao().findByIsinFetched(rawTickVO.getIsin());

        // for some reason security get's sometimes loaded as a javassist proxy
        // so we have to manualy get the implementation
        if (security instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) security;
            security = (Security) proxy.getHibernateLazyInitializer().getImplementation();
        }

        // initialize the proxys
        Hibernate.initialize(security.getUnderlaying());
        Hibernate.initialize(security.getVolatility());
        Hibernate.initialize(security.getSecurityFamily());
        Hibernate.initialize(security.getPositions());

        tick.setSecurity(security);

        return tick;
    }
}
