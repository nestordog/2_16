package com.algoTrader.service.fix;

import com.algoTrader.enumeration.Duration;

public final class FixIdGenerator {

    private static FixIdGenerator instance;
    private int orderId = 1;

    public FixIdGenerator() {

        // start at seconds since midnight
        this.orderId = (int) (System.currentTimeMillis() % Duration.ONE_DAY.getValue() / 1000);
    }

    public static synchronized FixIdGenerator getInstance() {

        if (instance == null) {
            instance = new FixIdGenerator();
        }
        return instance;
    }

    public int getNextOrderId() {

        return this.orderId++;
    }
}
