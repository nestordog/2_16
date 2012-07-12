package com.algoTrader.entity.trade;

public class TrailingStopOrderImpl extends TrailingStopOrder {

    private static final long serialVersionUID = -7260306708056150268L;

    @Override
    public String toString() {

        return super.toString() + " trailingAmount " + getTrailingAmount();
    }
}
