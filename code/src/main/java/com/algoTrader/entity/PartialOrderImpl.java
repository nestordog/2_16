package com.algoTrader.entity;

import com.algoTrader.enumeration.Status;

public class PartialOrderImpl extends PartialOrder {

    private static final long serialVersionUID = -7341726577868580753L;

    @Override
    public void setStatus(Status status) {

        if (Status.PARTIALLY_EXECUTED.equals(status) || Status.EXECUTED.equals(status)) {
            getParentOrder().setStatus(status);
        }
        if (Status.SUBMITTED.equals(status) && Status.OPEN.equals(getParentOrder().getStatus())) {
            getParentOrder().setStatus(status);
        }
        super.setStatus(status);
    }

    @Override
    public void addTransaction(Transaction transaction) {

        getTransactions().add(transaction);
        getParentOrder().getTransactions().add(transaction);
    }

    @Override
    public String toString() {

        return String.valueOf(getId());
    }
}
