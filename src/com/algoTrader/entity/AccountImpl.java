package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;

import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;

public class AccountImpl extends Account {

    private static final long serialVersionUID = -2271735085273721632L;

    private static double initialMarginMarkup = PropertiesUtil.getDoubleProperty("strategie.initialMarginMarkup");

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

    public BigDecimal getMaintenanceMargin() {
        return RoundUtil.getBigDecimal(getMaintenanceMarginDouble());
    }

    @SuppressWarnings("unchecked")
    public double getMaintenanceMarginDouble() {

        double margin = 0.0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            margin += position.getMarginDouble();
        }
        return margin;
    }

    @Override
    public BigDecimal getInitialMargin() {

        return RoundUtil.getBigDecimal(getInitialMarginDouble());
    }

    @Override
    public double getInitialMarginDouble() {

        return initialMarginMarkup * getMaintenanceMarginDouble();
    }

    public BigDecimal getAvailableAmount() {

        return RoundUtil.getBigDecimal(getAvailableAmountDouble());
    }

    public double getAvailableAmountDouble() {

        return getTotalValueDouble() - getInitialMarginDouble();
    }

    public BigDecimal getSecuritiesValue() {

        return RoundUtil.getBigDecimal(getSecuritiesValueDouble());
    }

    @SuppressWarnings("unchecked")
    public double getSecuritiesValueDouble() {

        double securitiesValue = 0.0;
        Collection<Position> positions = getPositions();
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
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            redemptionValue += position.getRedemptionValue();
        }
        return redemptionValue;
    }

    public double getAtRiskRatio() {

        return getRedemptionValue() / getCashBalanceDouble();
    }
}
