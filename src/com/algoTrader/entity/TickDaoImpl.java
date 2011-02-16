package com.algoTrader.entity;

import org.hibernate.Hibernate;

import com.algoTrader.vo.RawTickVO;
import com.algoTrader.vo.TickVO;

public class TickDaoImpl extends TickDaoBase {

    public void toTickVO(Tick tick, TickVO tickVO) {

        super.toTickVO(tick, tickVO);

        completeTickVO(tick, tickVO);
    }

    public TickVO toTickVO(final Tick tick) {

        TickVO tickVO = super.toTickVO(tick);

        completeTickVO(tick, tickVO);

        return tickVO;
    }

    public void toRawTickVO(Tick tick, RawTickVO rawTickVO) {

        super.toRawTickVO(tick, rawTickVO);

        completeRawTickVO(tick, rawTickVO);
    }

    public RawTickVO toRawTickVO(final Tick tick) {

        RawTickVO rawTickVO = super.toRawTickVO(tick);

        completeRawTickVO(tick, rawTickVO);

        return rawTickVO;
    }

    private void completeTickVO(Tick tick, TickVO tickVO) {

        tickVO.setSymbol(tick.getSecurity().getSymbol());
        tickVO.setMidpoint(tick.getCurrentValue());
    }

    private void completeRawTickVO(Tick tick, RawTickVO rawTickVO) {

        rawTickVO.setIsin(tick.getSecurity().getIsin());
    }

    public Tick tickVOToEntity(TickVO tickVO) {

        throw new UnsupportedOperationException("tickVOToEntity not yet implemented.");
    }

    public Tick rawTickVOToEntity(RawTickVO rawTickVO) {

        Tick tick = new TickImpl();
        super.rawTickVOToEntity(rawTickVO, tick, true);

        Security security = getSecurityDao().findByIsinFetched(rawTickVO.getIsin());

        // initialize the proxys
        Hibernate.initialize(security.getUnderlaying());
        Hibernate.initialize(security.getVolatility());
        Hibernate.initialize(security.getSecurityFamily());

        tick.setSecurity(security);

        return tick;
    }
}
