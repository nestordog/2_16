package com.algoTrader.entity;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.RoundUtil;

public class TransactionImpl extends Transaction {

    private static final long serialVersionUID = -1528408715199422753L;
    private static final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");

    private Double value = null; // cache getValueDouble because getValue get's called very often

    public BigDecimal getGrossValue() {

        if (getSecurity() != null) {
            int scale = getSecurity().getSecurityFamily().getScale();
            return RoundUtil.getBigDecimal(getGrossValueDouble(), scale);
        } else {
            return RoundUtil.getBigDecimal(getGrossValueDouble());
        }

    }

    /**
     * SELL / CREDIT / INTREST: positive cashflow
     * BUY / EXPIRATION / DEBIT / FEES: negative cashflow
     * REBALANCE: positive or negative cashflow (depending on quantity equals 1 or -1)
     */
    public double getGrossValueDouble() {

        if (this.value == null) {
            if (getType().equals(TransactionType.BUY) ||
                    getType().equals(TransactionType.SELL) ||
                    getType().equals(TransactionType.EXPIRATION)) {
                this.value = -getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getPrice().doubleValue();
            } else if (getType().equals(TransactionType.CREDIT) ||
                    getType().equals(TransactionType.INTREST_RECEIVED)) {
                this.value = getPrice().doubleValue();
            } else if (getType().equals(TransactionType.DEBIT) ||
                    getType().equals(TransactionType.FEES) ||
                    getType().equals(TransactionType.INTREST_PAID)) {
                this.value = -getPrice().doubleValue();
            } else if (getType().equals(TransactionType.REBALANCE)) {
                this.value = getQuantity() * getPrice().doubleValue();
            } else {
                throw new IllegalArgumentException("unsupported transactionType: " + getType());
            }
        }
        return this.value;
    }

    public BigDecimal getNetValue() {

        if (getSecurity() != null) {
            int scale = getSecurity().getSecurityFamily().getScale();
            return RoundUtil.getBigDecimal(getNetValueDouble(), scale);
        } else {
            return RoundUtil.getBigDecimal(getNetValueDouble());
        }
    }

    public double getNetValueDouble() {

        return getGrossValueDouble() - getCommission().doubleValue();
    }

    public String toString() {

        return format.format(getDateTime()) + " " + getType() + " " + getQuantity() + (getSecurity() != null ? (" " + getSecurity()) : "") + " " + getPrice() + " " + getCurrency();
    }
}
