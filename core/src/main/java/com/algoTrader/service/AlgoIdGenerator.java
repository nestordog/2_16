package com.algoTrader.service;


public final class AlgoIdGenerator {

    private static AlgoIdGenerator instance;
    private int orderId = 1;

    public AlgoIdGenerator() {

        this.orderId = Integer.MAX_VALUE;
    }

    public static synchronized AlgoIdGenerator getInstance() {

        if (instance == null) {
            instance = new AlgoIdGenerator();
        }
        return instance;
    }

    public int getNextOrderId() {

        return this.orderId--;
    }
}
