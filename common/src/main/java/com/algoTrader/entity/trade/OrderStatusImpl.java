package com.algoTrader.entity.trade;

public class OrderStatusImpl extends OrderStatus {

    private static final long serialVersionUID = -423135654204518265L;

    @Override
    public String toString() {

        //@formatter:off
        return getStatus()
            + (getParentOrder() != null ? " " + getParentOrder() : "")
            + " filledQuantity: " + getFilledQuantity()
            + " remainingQuantity: " + getRemainingQuantity();
        //@formatter:on
    }
}
