package com.algoTrader.entity;

import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.StrategyUtil;
import com.espertech.esper.event.bean.BeanEventBean;

public class SecurityImpl extends Security {

    private static final long serialVersionUID = -6631052475125813394L;

    public boolean isStrategyUnderlaying() {

        return Hibernate.isInitialized(getStrategies()) && (getStrategies().size() != 0);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Tick getLastTick() {

        List<Map> events = ServiceLocator.commonInstance().getRuleService().getAllEvents(StrategyUtil.getStartedStrategyName(), "GET_LAST_TICK");

        // try to see if the rule GET_LAST_TICK has the tick
        for (Map event : events) {
            Integer securityId = (Integer) event.get("securityId");
            if (securityId.equals(getId())) {
                return (Tick) ((BeanEventBean) event.get("tick")).getUnderlying();
            }
        }

        // if we did not get the tick up to now go to the db an get the last tick
        Tick tick = ServiceLocator.commonInstance().getLookupService().getLastTick(getId());
        return tick;
    }

    /**
     * spread depends on the pricePerContract (i.e. spread should be the same
     * for 12.- à contractSize 10 as for 1.20 à contractSize 100)
     *
     * @return price per option
     */
    public double getDummyBid(double price) {

        double pricePerContract = price * getSecurityFamily().getContractSize();
        double spread = pricePerContract * getSecurityFamily().getSpreadSlope() + getSecurityFamily().getSpreadConstant();
        return (pricePerContract - (spread / 2.0)) / getSecurityFamily().getContractSize();
    }

    /**
     * spread depends on the pricePerContract (i.e. spread should be the same
     * for 12.- à contractSize 10 as for 1.20 à contractSize 100)
     *
     * @return price per option
     */
    public double getDummyAsk(double price) {

        double pricePerContract = price * getSecurityFamily().getContractSize();
        double spread = pricePerContract * getSecurityFamily().getSpreadSlope() + getSecurityFamily().getSpreadConstant();
        return (pricePerContract + (spread / 2.0)) / getSecurityFamily().getContractSize();
    }

    public boolean isOnWatchlist() {

        return Hibernate.isInitialized(getWatchers()) && (getWatchers().size() != 0);
    }

    public void validateTick(Tick tick) {

        // do nothing, this method will be overwritten
    }
}
