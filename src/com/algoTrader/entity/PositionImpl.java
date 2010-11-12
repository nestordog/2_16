package com.algoTrader.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

        long totalQuantity = 0;
        double totalPrice = 0.0;
        long maxQuantity = getQuantity();
        List<Transaction> transactions = new ArrayList(getTransactions());
        Collections.reverse(transactions);

        // by FIFO principle
        // we go through all transactions (in reverse order) until we have considered to total quantity of the position
        for (Transaction transaction : transactions) {

            // price per Contract of this transaction
            // we need this because we might not consider to whole quantity of the transaction
            // (part might already have been sold again)
            double pricePerContract = (transaction.getPrice().doubleValue() * transaction.getSecurity().getContractSize() + transaction.getCommission().doubleValue() / transaction.getQuantity());

            if ((maxQuantity < 0) && TransactionType.SELL.equals(transaction.getType()) && (totalQuantity != maxQuantity)) {

                // for short positions look at sells
                long quantity = Math.max(transaction.getQuantity(), maxQuantity - totalQuantity);

                totalQuantity += quantity;
                totalPrice += quantity * pricePerContract;

            } else if ((maxQuantity > 0) && TransactionType.BUY.equals(transaction.getType()) && (totalQuantity != maxQuantity)) {

                // for long positions look at buys
                long quantity = Math.min(transaction.getQuantity(), maxQuantity - totalQuantity);

                totalQuantity += quantity;
                totalPrice += quantity * pricePerContract;
            }
        }
        return totalPrice / totalQuantity / getSecurity().getContractSize();
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
