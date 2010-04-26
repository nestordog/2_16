package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;

import com.algoTrader.util.RoundUtil;

public class AccountImpl extends Account {

    private static final long serialVersionUID = -2271735085273721632L;

    public BigDecimal getCashBalance() {
        return RoundUtil.getBigDecimal(getCashBalanceDouble());
    }

    @SuppressWarnings("unchecked")
    public double getCashBalanceDouble() {

        double balance = 0.0;
        Collection<Transaction> transactions = getTransactions();
        for (Transaction transaction : transactions) {
            balance += transaction.getValueDouble();
        }
        return balance;
    }

    public BigDecimal getMargin() {
        return RoundUtil.getBigDecimal(getMarginDouble());
    }

    @SuppressWarnings("unchecked")
    public double getMarginDouble() {

        double margin = 0.0;
        Collection<Position> positions = getOpenPositions();
        for (Position position : positions) {
            margin += position.getMarginDouble();
        }
        return margin;
    }

    public BigDecimal getAvailableAmount() {

        return RoundUtil.getBigDecimal(getAvailableAmountDouble());
    }

    public double getAvailableAmountDouble() {

        return getCashBalanceDouble() - getMarginDouble();
    }

    public BigDecimal getSecuritiesValue() {

        return RoundUtil.getBigDecimal(getSecuritiesValueDouble());
    }

    @SuppressWarnings("unchecked")
    public double getSecuritiesValueDouble() {

        double securitiesValue = 0.0;
        Collection<Position> positions = getOpenPositions();
        for (Position position : positions) {
            securitiesValue += position.getValueDouble();
        }
        return securitiesValue;
    }

    public BigDecimal getTotalValue() {

        return RoundUtil.getBigDecimal(getTotalValueDouble());
    }

    public double getTotalValueDouble() {

        return getCashBalanceDouble() + getSecuritiesValueDouble();
    }

    @SuppressWarnings("unchecked")
    public double getRedemptionValue() {

        double redemptionValue = 0.0;
        System.currentTimeMillis();
        Collection<Position> positions = getOpenPositions();
        for (Position position : positions) {
            redemptionValue += position.getRedemptionValue();
        }
        return redemptionValue;
    }

    public double getAtRiskRatio() {

        return getRedemptionValue() / getCashBalanceDouble();
    }
}
