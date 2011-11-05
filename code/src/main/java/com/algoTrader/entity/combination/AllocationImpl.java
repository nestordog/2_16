package com.algoTrader.entity.combination;

public class AllocationImpl extends Allocation {

    private static final long serialVersionUID = 8647813109138743881L;

    @Override
    public String toString() {

        return getSecurity().getSymbol() + " (qty " + getQuantity() + ")";
    }
}
