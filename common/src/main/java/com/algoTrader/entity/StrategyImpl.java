package com.algoTrader.entity;


public class StrategyImpl extends Strategy {

    public static final String BASE = "BASE";

    private static final long serialVersionUID = -2271735085273721632L;

    @Override
    public boolean isBase() {
        return (BASE.equals(getName()));
    }

    @Override
    public String toString() {

        return getName();
    }
}
