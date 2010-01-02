package com.algoTrader.entity;

import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.algoTrader.util.CustomToStringStyle;
import com.algoTrader.util.EsperService;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;

public class SecurityImpl extends com.algoTrader.entity.Security {

    private static final long serialVersionUID = -6631052475125813394L;

    public Tick getLastTick() {
        EPStatement statement = EsperService.getEPServiceInstance().getEPAdministrator().getStatement("lastTick");

        Iterator it = statement.iterator();
        while (it.hasNext()) {
            EventBean bean = (EventBean) it.next();
            Integer securityId = (Integer) bean.get("securityId");
            if (securityId.equals(getId())) {
                return (Tick)bean.get("tick");
            }
        }
        return null;
    }

    public java.lang.String toString() {
        return ToStringBuilder.reflectionToString(this, CustomToStringStyle.getInstance());

    }
}
