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
            margin += position.getMaintenanceMarginDouble();
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

    public BigDecimal getAvailableFunds() {

        return RoundUtil.getBigDecimal(getAvailableFundsDouble());
    }

    public double getAvailableFundsDouble() {

        return getNetLiqValueDouble() - getInitialMarginDouble();
    }

    public BigDecimal getSecuritiesCurrentValue() {

        return RoundUtil.getBigDecimal(getSecuritiesCurrentValueDouble());
    }

    @SuppressWarnings("unchecked")
    public double getSecuritiesCurrentValueDouble() {

        double securitiesValue = 0.0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            securitiesValue += position.getCurrentValueDouble();
        }
        return securitiesValue;
    }

    public BigDecimal getNetLiqValue() {

        return RoundUtil.getBigDecimal(getNetLiqValueDouble());
    }

    public double getNetLiqValueDouble() {

        return getCashBalanceDouble() + getSecuritiesCurrentValueDouble();
    }

    @SuppressWarnings("unchecked")
    public double getRedemptionValue() {

        double redemptionValue = 0.0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            redemptionValue += position.getRedemptionValue();
        }
        return redemptionValue;
    }

    public double getAtRiskRatio() {

        return getRedemptionValue() / getCashBalanceDouble();
    }

    @SuppressWarnings("unchecked")
    public double getLeverage() {

        double deltaRisk = 0.0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            if (position.isOpen()) {
                deltaRisk += position.getDeltaRisk();
            }
        }

        return deltaRisk / getNetLiqValueDouble();
    }
}
