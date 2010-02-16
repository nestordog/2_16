package com.algoTrader.entity;

import java.util.Iterator;

import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.EsperService;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;

public class SecurityImpl extends com.algoTrader.entity.Security {

    private static final long serialVersionUID = -6631052475125813394L;

    public Tick getLastTick() {
        EPStatement statement = EsperService.getEPServiceInstance().getEPAdministrator().getStatement(RuleName.GET_LAST_TICK.getValue());

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
}
