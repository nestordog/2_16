package com.algoTrader.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.DateUtil;


public class PositionImpl extends Position {

    private static final long serialVersionUID = -2679980079043322328L;

    public boolean isOpen() {

        return getQuantity() != 0;
    }

    public boolean isLong() {

        return getQuantity() > 0;
    }

    public boolean isShort() {

        return getQuantity() < 0;
    }

    public boolean isFlat() {

        return getQuantity() == 0;
    }

    /**
     * empty positions and sideways positons return null
     */
    public Boolean isBullish() {

        if (!isOpen())
            return null;

        if (getSecurity() instanceof StockOption) {
            if (((StockOption) getSecurity()).getType().equals(OptionType.PUT)) {
                return getQuantity() < 0;
            } else {
                return getQuantity() > 0;
            }
        } else if (getSecurity() instanceof Future) {

            return getQuantity() > 0;
        } else {
            // we have nothing else yet
            return null;
        }
    }

    public boolean isBearish() {

        return !isBullish();
    }

    /**
     * always positive
     */
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

    public double getMarketPriceBaseDouble() {

        return getMarketPriceDouble() * getSecurity().getFXRateBase();
    }

    /**
     * short positions: negative long positions: positive
     */
    public double getMarketValueDouble() {

        if (isOpen()) {

            return getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getMarketPriceDouble();
        } else {
            return 0.0;
        }
    }

    public double getMarketValueBaseDouble() {

        return getMarketValueDouble() * getSecurity().getFXRateBase();
    }

    /**
     * always positive
     */
    public double getAveragePriceDouble() {

        long totalQuantity = 0;
        double totalPrice = 0.0;

        List<QuantityTransaction> quantityTransactions = getFIFIQueue();
        for (QuantityTransaction queueTransaction : quantityTransactions) {

            Transaction transaction = queueTransaction.getTransaction();
            long quantity = queueTransaction.getQuantity();
            double pricePerContract = Math.abs(transaction.getValueDouble() / transaction.getQuantity());

            totalQuantity += quantity;
            totalPrice += quantity * pricePerContract;


        }
        return totalPrice / totalQuantity / getSecurity().getSecurityFamily().getContractSize();
    }

    public double getAveragePriceBaseDouble() {

        return getAveragePriceDouble() * getSecurity().getFXRateBase();
    }

    /**
     * in days
     */
    public double getAverageAge() {

        long totalQuantity = 0;
        long totalAge = 0;

        List<QuantityTransaction> quantityTransactions = getFIFIQueue();
        for (QuantityTransaction queueTransaction : quantityTransactions) {

            Transaction transaction = queueTransaction.getTransaction();
            long quantity = queueTransaction.getQuantity();
            long age = DateUtil.getCurrentEPTime().getTime() - transaction.getDateTime().getTime();

            totalQuantity += quantity;
            totalAge += quantity * age;

        }
        return totalAge / totalQuantity / 86400000.0;
    }

    /**
     * short positions: negative long positions: positive
     */
    public double getCostDouble() {

        if (isOpen()) {

            return getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getAveragePriceDouble();
        } else {
            return 0.0;
        }
    }

    public double getCostBaseDouble() {

        return getCostDouble() * getSecurity().getFXRateBase();
    }

    public double getUnrealizedPLDouble() {
        if (isOpen()) {

            return getMarketValueDouble() - getCostDouble();
        } else {
            return 0.0;
        }
    }

    public double getUnrealizedPLBaseDouble() {

        return getUnrealizedPLDouble() * getSecurity().getFXRateBase();
    }

    public double getExitValueDouble() {

        if (getExitValue() != null) {
            return getExitValue().doubleValue();
        } else {
            return 0.0;
        }
    }

    public double getExitValueDoubleBase() {

        return getExitValueDouble() * getSecurity().getFXRateBase();
    }

    public double getMaintenanceMarginDouble() {

        if (isOpen() && getMaintenanceMargin() != null) {
                return getMaintenanceMargin().doubleValue();
        } else {
            return 0.0;
        }
    }

    public double getMaintenanceMarginBaseDouble() {

        return getMaintenanceMarginDouble() * getSecurity().getFXRateBase();
    }

    public double getRedemptionValueDouble() {

        if (isOpen() && getExitValue() != null) {

            return -getQuantity() * getSecurity().getSecurityFamily().getContractSize() * getExitValueDouble();
        } else {
            return 0.0;
        }
    }

    public double getRedemptionValueBaseDouble() {

        return getRedemptionValueDouble() * getSecurity().getFXRateBase();
    }

    public double getMaxLossDouble() {

        if (isOpen() && getExitValue() != null) {

            double maxLossPerItem;
            if (isLong()) {
                maxLossPerItem = getMarketPriceDouble() - getExitValueDouble();
            } else {
                maxLossPerItem = getExitValueDouble() - getMarketPriceDouble();
            }
            return -getQuantity() * getSecurity().getSecurityFamily().getContractSize() * maxLossPerItem;
        } else {
            return 0.0;
        }
    }

    public double getMaxLossBaseDouble() {

        return getMaxLossDouble() * getSecurity().getFXRateBase();
    }
    public double getExposure() {

        return getMarketValueDouble() * getSecurity().getLeverage();
    }

    public String toString() {

        return getQuantity() + " " + getSecurity();
    }

    private List<QuantityTransaction> getFIFIQueue() {

        List<Transaction> transactions = new ArrayList<Transaction>(getTransactions());

        // sort by date ascending
        Collections.sort(transactions, new Comparator<Transaction>() {
            public int compare(Transaction t1, Transaction t2) {
                return (t1.getDateTime().compareTo(t2.getDateTime()));
            }
        });

        List<QuantityTransaction> queue = new ArrayList<QuantityTransaction>();
        long totalQuantity = 0;
        for (Transaction transaction : transactions) {

            // if queue is empty or transaction increases existing position -> add transaction to queue
            if (queue.size() == 0 || Long.signum(totalQuantity) == Long.signum(transaction.getQuantity())) {
                queue.add(new QuantityTransaction(transaction.getQuantity(), transaction));

                // if transaction is reducing quantity -> go through the queue and remove as many items as necessary
            } else {
                long runningQuantity = transaction.getQuantity();
                for (Iterator<QuantityTransaction> it = queue.iterator(); it.hasNext();) {

                    QuantityTransaction queueTransaction = it.next();

                    // transaction will be completely removed
                    if (Math.abs(queueTransaction.getQuantity()) <= Math.abs(runningQuantity)) {
                        runningQuantity += queueTransaction.getQuantity();
                        it.remove();

                    // transaction will be partly removed
                    } else {
                        queueTransaction.setQuantity(queueTransaction.getQuantity() + runningQuantity);
                        runningQuantity = 0;
                        break;
                    }
                }

                // if not the entire runningQuantity could be eliminated,
                // create a new Quantity Transaction with the reminder
                if (runningQuantity != 0) {
                    queue.add(new QuantityTransaction(runningQuantity, transaction));
                }
            }
            totalQuantity += transaction.getQuantity();
        }

        return queue;
    }

    private static class QuantityTransaction {

        private long quantity;
        private Transaction transaction;

        public QuantityTransaction(long quantity, Transaction transaction) {
            super();
            this.quantity = quantity;
            this.transaction = transaction;
        }

        public long getQuantity() {
            return this.quantity;
        }

        public void setQuantity(long quantity) {
            this.quantity = quantity;
        }

        public Transaction getTransaction() {
            return this.transaction;
        }
    }
}
