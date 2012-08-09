package com.algoTrader.entity.strategy;

public class DefaultOrderPreferenceImpl extends DefaultOrderPreference {

    private static final long serialVersionUID = -5231151073076967781L;

    @Override
    public String toString() {

        return getStrategy() + " " + getSecurityFamily() + " " + getOrderPreference();
    }
}
