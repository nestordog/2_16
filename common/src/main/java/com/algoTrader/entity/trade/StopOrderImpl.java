package com.algoTrader.entity.trade;

import java.math.BigDecimal;

public class StopOrderImpl extends StopOrder {

    private static final long serialVersionUID = -9213820219309533525L;

    @Override
    public String getDescription() {
        return "stop: " + getStop();
    }

    @Override
    public void validate() throws OrderValidationException {

        if (getStop() == null) {
            throw new OrderValidationException("no stop defined for " + this);
        }
    }

    @Override
    public Order modifyStop(BigDecimal stop) {

        StopOrder order = (StopOrder) cloneOrder();
        order.setStop(stop);
        return order;
    }
}
