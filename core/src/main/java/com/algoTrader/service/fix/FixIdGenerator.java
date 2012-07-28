package com.algoTrader.service.fix;

import java.util.Date;

import com.algoTrader.util.Constants;

public final class FixIdGenerator {

    private static FixIdGenerator instance;
    private int orderId = 1;

    public FixIdGenerator() {

        this.orderId = (int) ((new Date()).getTime() % Constants.MILLIS_PER_DAY);
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
