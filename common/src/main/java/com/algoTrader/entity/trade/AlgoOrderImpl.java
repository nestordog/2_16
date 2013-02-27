package com.algoTrader.entity.trade;

public abstract class AlgoOrderImpl extends AlgoOrder {

    private static final long serialVersionUID = 5310975560518020161L;

    @Override
    public boolean isAlgoOrder() {
        return true;
    }

    @Override
    public void done() {
        // do nothing
    }
}
