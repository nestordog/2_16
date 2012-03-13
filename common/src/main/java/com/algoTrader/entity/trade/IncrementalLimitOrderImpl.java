package com.algoTrader.entity.trade;

public abstract class IncrementalLimitOrderImpl extends IncrementalLimitOrder {

    private static final long serialVersionUID = -3834520928717471963L;

    @Override
    public abstract IncrementalLimitOrder adjustLimit();

    @Override
    public abstract boolean checkLimit();
}
