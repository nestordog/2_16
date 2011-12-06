package com.algoTrader.entity.marketData;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.WatchListItem;
import com.algoTrader.entity.security.Security;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.StrategyUtil;
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

        Security security = tick.getSecurity();
        Strategy strategy = StrategyUtil.getStartedStrategy();

        tickVO.setSecurityId(security.getId());
        tickVO.setSymbol(security.getSymbol());
        tickVO.setCurrentValue(tick.getCurrentValue());

        // we have to iterate because this code might be executed inside the client
        for (WatchListItem watchListItem : security.getWatchListItems()) {
            if (watchListItem.getStrategy().getId() == strategy.getId()
                    && (watchListItem.getUpperAlertValue() != null || watchListItem.getLowerAlertValue() != null)) {

                int scale = security.getSecurityFamily().getScale();
                tickVO.setUpperAlertValue(watchListItem.getUpperAlertValue() != null ? RoundUtil.getBigDecimal(watchListItem.getUpperAlertValue(), scale) : null);
                tickVO.setLowerAlertValue(watchListItem.getLowerAlertValue() != null ? RoundUtil.getBigDecimal(watchListItem.getLowerAlertValue(), scale) : null);
            }
        }
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
