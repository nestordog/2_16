package com.algoTrader.entity.trade;

import java.math.BigDecimal;

public abstract class IncrementalLimitOrderImpl extends IncrementalLimitOrder {

    private static final long serialVersionUID = -3834520928717471963L;

    @Override
    public abstract void setDefaultLimits(BigDecimal bid, BigDecimal ask);

    @Override
    public abstract IncrementalLimitOrder adjustLimit();

    @Override
    public abstract boolean checkLimit();
}
