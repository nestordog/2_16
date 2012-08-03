package com.algoTrader.entity.trade;

public abstract class SimpleOrderImpl extends SimpleOrder {

    private static final long serialVersionUID = -7687451249870091145L;

    @Override
    public boolean isAlgoOrder() {
        return false;
    }
}
