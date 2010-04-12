package com.algoTrader.entity;

import java.util.Collection;

public class OrderImpl extends com.algoTrader.entity.Order {

    private static final long serialVersionUID = -4322081437722784564L;

    @SuppressWarnings("unchecked")
    public long getExecutedQuantity() {

        Collection<Transaction> list = getTransactions();
        long executedQuantity = 0;
        for (Transaction transaction : list) {
            executedQuantity += transaction.getQuantity();
        }

        return executedQuantity;
    }
}
