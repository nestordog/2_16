/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
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
