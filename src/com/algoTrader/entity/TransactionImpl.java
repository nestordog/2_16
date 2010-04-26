package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.RoundUtil;

public class TransactionImpl extends Transaction {

    private static final long serialVersionUID = -1528408715199422753L;

    public BigDecimal getValue() {

        return RoundUtil.getBigDecimal(getValueDouble());
    }

    public double getValueDouble() {

        if (getType().equals(TransactionType.BUY) ||
            getType().equals(TransactionType.SELL) ||
            getType().equals(TransactionType.EXPIRATION)) {

            return -getPrice().doubleValue() * (double)getQuantity() - getCommission().doubleValue();

        } else if (getType().equals(TransactionType.CREDIT) ||
            getType().equals(TransactionType.DIVIDEND) ||
            getType().equals(TransactionType.INTREST)) {

            return getPrice().doubleValue();

        } else if (getType().equals(TransactionType.DEBIT) ||
            getType().equals(TransactionType.FEES)) {

            return -getPrice().doubleValue();
        } else {
            throw new IllegalArgumentException("unsupported transactionType: " + getType());
        }
    }
}
