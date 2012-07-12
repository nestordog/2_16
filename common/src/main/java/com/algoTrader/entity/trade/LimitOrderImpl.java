package com.algoTrader.entity.trade;

public class LimitOrderImpl extends LimitOrder {

    private static final long serialVersionUID = -3560878461518491161L;

    @Override
    public String toString() {

        return super.toString() + " limit " + getLimit();
    }
}
