package com.algoTrader.entity.marketData;

public abstract class MarketDataEventImpl extends MarketDataEvent {

    private static final long serialVersionUID = 8758212212560594623L;

    public String toString() {

        return toLongString();
    }
}
