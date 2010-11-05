package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.RuleName;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.EsperService;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.SafeIterator;

public class SecurityImpl extends com.algoTrader.entity.Security {

    private static final long serialVersionUID = -6631052475125813394L;

    public Tick getLastTick() {

        // try to see if the rule GET_LAST_TICK has the tick
        EPStatement statement = EsperService.getStatement(RuleName.GET_LAST_TICK);
        if (statement != null && statement.isStarted()) {

            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    Integer securityId = (Integer) bean.get("securityId");
                    if (securityId.equals(getId())) {
                        return (Tick)bean.get("tick");
                    }
                }
            } finally {
                it.close();
            }
        }

        // if we did not get the tick up to now go to the db an get the last tick
        Tick tick = ServiceLocator.instance().getLookupService().getLastTick(getId());
        return tick;
    }

    public boolean hasOpenPositions() {
        return getPosition().isOpen();
    }

    public BigDecimal getCommission(long quantity, TransactionType transactionType) {

        return new BigDecimal(0);
    }

    public int getContractSize() {

        return 1;
    }

    public void validateTick(Tick tick) {

        // do nothing, this method will be overwritten
    }
}
