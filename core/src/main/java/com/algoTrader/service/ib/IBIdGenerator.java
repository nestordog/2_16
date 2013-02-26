package com.algoTrader.service.ib;


public final class IBIdGenerator {

    private static IBIdGenerator instance;
    private int requestId = 1;
    private int orderId = 1;

    public static synchronized IBIdGenerator getInstance() {

        if (instance == null) {
            instance = new IBIdGenerator();
        }
        return instance;
    }

    public String getNextOrderId() {
        return String.valueOf(this.orderId++);
    }

    public int getNextRequestId() {
        return this.requestId++;
    }

    public void initializeOrderId(int orderId) {
        this.orderId = orderId;
    }

    public boolean isOrderIdInitialized() {

        return this.orderId != -1;
    }
}
