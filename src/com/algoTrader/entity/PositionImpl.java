package com.algoTrader.entity;

import java.util.Collection;

import com.algoTrader.enumeration.TransactionType;


public class PositionImpl extends com.algoTrader.entity.Position {

    private static final long serialVersionUID = -2679980079043322328L;

    public boolean isOpen() {

        return (getQuantity() != 0);
    }

    public double getMarketPriceDouble() {

        if (isOpen()) {

            Tick tick = getSecurity().getLastTick();
            if (tick != null) {
                if (getQuantity() < 0) {

                    // short position
                    return tick.getAsk().doubleValue();
                } else {

                    // short position
                    return tick.getBid().doubleValue();
                }
            } else {
                return Double.NaN;
            }
        } else {
            return 0.0;
        }
    }

    public double getMarketValueDouble() {

        if (isOpen()) {

            return getQuantity() * getSecurity().getContractSize() * getMarketPriceDouble();
        } else {
            return 0.0;
        }
    }

    @SuppressWarnings("unchecked")
    public double getAveragePriceDouble() {

        long quantity = 0;
        double totalPrice = 0.0;
        Collection<Transaction> transactions = getTransactions();
        for (Transaction transaction : transactions) {

            if (getQuantity() < 0 && TransactionType.SELL.equals(transaction.getType())) {

                // for short positions look at sells
                quantity += transaction.getQuantity();
                totalPrice += transaction.getQuantity() * transaction.getPrice().doubleValue() +
                    transaction.getCommission().doubleValue() / transaction.getSecurity().getContractSize();

            } else if (getQuantity() > 0 && TransactionType.BUY.equals(transaction.getType())) {

                // for short positions look at sells
                quantity += transaction.getQuantity();
                totalPrice += transaction.getQuantity() * transaction.getPrice().doubleValue() +
                    transaction.getCommission().doubleValue() / transaction.getSecurity().getContractSize();
            }
        }
        return totalPrice / quantity;
    }

    public double getCostDouble() {

        if (isOpen()) {

            return getQuantity() * getSecurity().getContractSize() * getAveragePriceDouble();
        } else {
            return 0.0;
        }
    }

    public double getUnrealizedPLDouble() {
        if (isOpen()) {

            return getMarketValueDouble() - getCostDouble();
        } else {
            return 0.0;
        }
    }

    public double getMaintenanceMarginDouble() {

        if (isOpen() && getMaintenanceMargin() != null) {
                return getMaintenanceMargin().doubleValue();
        } else {
            return 0.0;
        }
    }

    public double getRedemptionValue() {

        if (isOpen() && getExitValue() != null) {

            return -getQuantity() * getSecurity().getContractSize() * getExitValue().doubleValue();
        } else {
            return 0.0;
        }
    }

    public double getDeltaRisk() {

        if (getSecurity() instanceof StockOption) {

            StockOption stockOption = (StockOption) getSecurity();

            return getMarketValueDouble() * stockOption.getLeverage();

        } else {
            return 0.0;
        }
    }
}
