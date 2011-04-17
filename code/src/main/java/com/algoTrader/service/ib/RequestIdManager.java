package com.algoTrader.service.ib;

public class RequestIdManager {

    private static RequestIdManager instance = null;
    private int requestId = 0;
    private int orderId = -1;

    private RequestIdManager() {
    }

    public static RequestIdManager getInstance() {
        if (instance == null) {
            instance = new RequestIdManager();
        }
        return instance;
    }

    public int getNextOrderId() {
        this.orderId++;
        return this.orderId;
    }

    public int getNextRequestId() {
        this.requestId++;
        return this.requestId;
    }

    public void initializeOrderId(int orderId) {
        this.orderId = orderId;
    }

    public boolean isOrderIdInitialized() {
        if (this.orderId == -1) {
            return false;
        } else {
            return true;
        }
    }
}
