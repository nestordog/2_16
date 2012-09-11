package com.algoTrader.entity;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.RoundUtil;

public class TransactionImpl extends Transaction {

    private static final long serialVersionUID = -1528408715199422753L;

    private static @Value("${misc.portfolioDigits}") int portfolioDigits;

    private Double value = null; // cache getValueDouble because getValue get's called very often

    @Override
    public BigDecimal getGrossValue() {

        if (getSecurity() != null) {
            int scale = getSecurity().getSecurityFamily().getScale();
            return RoundUtil.getBigDecimal(getGrossValueDouble(), scale);
        } else {
            return RoundUtil.getBigDecimal(getGrossValueDouble(), portfolioDigits);
        }

    }

    @Override
    public double getGrossValueDouble() {

        //@formatter:off
        if (this.value == null) {
            if (getType().equals(TransactionType.BUY) ||
                    getType().equals(TransactionType.SELL) ||
                    getType().equals(TransactionType.EXPIRATION)) {
                this.value = -getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getPrice().doubleValue();
            } else if (getType().equals(TransactionType.CREDIT) ||
                    getType().equals(TransactionType.INTREST_RECEIVED) ||
                    getType().equals(TransactionType.REFUND)) {
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
        //@formatter:off
        return this.value;
    }

    @Override
    public BigDecimal getNetValue() {

        if (getSecurity() != null) {
            int scale = getSecurity().getSecurityFamily().getScale();
            return RoundUtil.getBigDecimal(getNetValueDouble(), scale);
        } else {
            return RoundUtil.getBigDecimal(getNetValueDouble(), portfolioDigits);
        }
    }

    @Override
    public double getNetValueDouble() {

        return getGrossValueDouble() - getTotalCommissionDouble();
    }

    @Override
    public double getTotalCommissionDouble() {

        return getExecutionCommission().doubleValue() + getClearingCommission().doubleValue();
    }

    @Override
    public BigDecimal getTotalCommission() {

        return RoundUtil.getBigDecimal(getTotalCommissionDouble(), portfolioDigits);
    }

    @Override
    public String toString() {

        if (TransactionType.BUY.equals(getType()) || TransactionType.SELL.equals(getType()) || TransactionType.EXPIRATION.equals(getType()) ) {

            //@formatter:off
            return getType()
                + " " + getQuantity()
                + " " + getSecurity()
                + " price: " + getPrice() + " " + getCurrency()
                + " commission: " + getTotalCommission()
                + " strategy: " + getStrategy();
            //@formatter:on

        } else {

            //@formatter:off
            return getType()
                + " amount: " + (getQuantity() < 0 ? "-" : "") + getPrice()+ " " + getCurrency()
                + " strategy: " + getStrategy()
                + (getDescription() != null ? " " + getDescription() : "");
            //@formatter:on
        }
    }
}
