package com.algoTrader.service;

import com.algoTrader.util.Constants;

public final class AlgoIdGenerator {

    private static AlgoIdGenerator instance;

    private int orderId = Constants.SECONDS_PER_DAY;

    public static synchronized AlgoIdGenerator getInstance() {

        if (instance == null) {
            instance = new AlgoIdGenerator();
        }
        return instance;
    }

    public int getNextOrderId() {

        return this.orderId++;
    }
}
