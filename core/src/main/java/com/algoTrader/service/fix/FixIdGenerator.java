package com.algoTrader.service.fix;

import com.algoTrader.util.Constants;

public final class FixIdGenerator {

    private static FixIdGenerator instance;
    private int orderId = 1;

    public FixIdGenerator() {

        // start at seconds since midnight
        this.orderId = (int) (System.currentTimeMillis() % Constants.ONE_DAY / 1000);
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
