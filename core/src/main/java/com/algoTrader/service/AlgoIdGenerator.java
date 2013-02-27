package com.algoTrader.service;


public final class AlgoIdGenerator {

    private static AlgoIdGenerator instance;

    private int orderId = 0;

    public static synchronized AlgoIdGenerator getInstance() {

        if (instance == null) {
            instance = new AlgoIdGenerator();
        }
        return instance;
    }

    public String getNextOrderId() {
        return "a" + String.valueOf(this.orderId++);
    }
}
