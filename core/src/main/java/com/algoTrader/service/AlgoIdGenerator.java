package com.algoTrader.service;

import com.algoTrader.enumeration.Duration;

public final class AlgoIdGenerator {

    private static AlgoIdGenerator instance;

    private int orderId = (int) (Duration.ONE_DAY.getValue() / 1000);

    public static synchronized AlgoIdGenerator getInstance() {

        if (instance == null) {
            instance = new AlgoIdGenerator();
        }
        return instance;
    }

    public String getNextOrderId() {

        return String.valueOf(this.orderId++);
    }
}
