package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Iterator;

import com.algoTrader.enumeration.RuleName;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.EsperService;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;

public class SecurityImpl extends com.algoTrader.entity.Security {

    private static final long serialVersionUID = -6631052475125813394L;

    public Tick getLastTick() {

        EPStatement statement = EsperService.getStatement(RuleName.GET_LAST_TICK);

        if (statement != null && statement.isStarted()) {
            for (Iterator<EventBean> it = statement.iterator(); it.hasNext(); ) {
                EventBean bean = it.next();
                Integer securityId = (Integer) bean.get("securityId");
                if (securityId.equals(getId())) {
                    return (Tick)bean.get("tick");
                }
            }
        }
        return null;
    }

    public boolean hasOpenPositions() {
        return getPosition().isOpen();
    }

    public BigDecimal getCommission(long quantity, TransactionType transactionType) {

        return new BigDecimal(0);
    }

    public BigDecimal getCurrentValuePerContract() {

        return getLastTick().getCurrentValue();
    }

    public double getCurrentValuePerContractDouble() {

        return getLastTick().getCurrentValueDouble();
    }

    public int getContractSize() {

        return 1;
    }
}
