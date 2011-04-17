package com.algoTrader.entity;

import com.algoTrader.enumeration.OrderStatus;

public class PartialOrderImpl extends PartialOrder {

    private static final long serialVersionUID = -7341726577868580753L;

    public void setStatus(OrderStatus status) {

        if (OrderStatus.PARTIALLY_EXECUTED.equals(status) || OrderStatus.EXECUTED.equals(status)) {
            getParentOrder().setStatus(status);
        }
        if (OrderStatus.SUBMITTED.equals(status) && OrderStatus.OPEN.equals(getParentOrder().getStatus())) {
            getParentOrder().setStatus(status);
        }
        super.setStatus(status);
    }

    public void addTransaction(Transaction transaction) {

        getTransactions().add(transaction);
        getParentOrder().getTransactions().add(transaction);
    }
}
