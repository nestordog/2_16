package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.algoTrader.util.CustomToStringStyle;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.PropertiesUtil;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;

public class SecurityImpl extends com.algoTrader.entity.Security {

    private static final long serialVersionUID = -6631052475125813394L;

    private static int lastTransactionAge = Integer.parseInt(PropertiesUtil.getProperty("lastTransactionAge"));
    private static int minVol = Integer.parseInt(PropertiesUtil.getProperty("minVol"));


    public Tick getLastTick() {
        EPStatement statement = EsperService.getEPServiceInstance().getEPAdministrator().getStatement("lastTick");

        if (statement != null) {
            Iterator it = statement.iterator();
            while (it.hasNext()) {
                EventBean bean = (EventBean) it.next();
                Integer securityId = (Integer) bean.get("securityId");
                if (securityId.equals(getId())) {
                    return (Tick)bean.get("tick");
                }
            }
        }
        return null;
    }

    public boolean hasOpenPositions() {
        return getPosition().getQuantity() > 0;
    }

    public BigDecimal getCurrentValue() {

        long currenttime = EsperService.getEPServiceInstance().getEPRuntime().getCurrentTime();
        Tick tick = getLastTick();

        if (tick != null) {
            if (currenttime - tick.getLastDateTime().getTime() > lastTransactionAge) {

                if (tick.getVolAsk() > minVol && tick.getVolBid() > minVol) {
                    return (tick.getAsk().add(tick.getBid()).divide(new BigDecimal(2)));
                } else {
                    return tick.getLast();
                }
            } else {
                return tick.getLast();
            }
        } else {
            return null;
        }
    }

    public java.lang.String toString() {
        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());
    }
}
