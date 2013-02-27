package com.algoTrader.entity.trade;


public class StopLimitOrderImpl extends StopLimitOrder {

    private static final long serialVersionUID = -6796363895406178181L;

    @Override
    public String getDescription() {
        return "stop: " + getStop() + " limit: " + getLimit();
    }

    @Override
    public void validate() throws OrderValidationException {

        if (getLimit() == null) {
            throw new OrderValidationException("no limit defined for " + this);
        } else if (getStop() == null) {
            throw new OrderValidationException("no stop defined for " + this);
        }
    }
}
