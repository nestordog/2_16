/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;

import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.security.Security;

/**
 * Provides different Position related utility methods.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PositionUtil {

    /**
     * Returns the realized Profit-and-Loss of a Position based on the specified {@code transactions}
     * @return positive for SELL
     */
    public static double getRealizedPL(Collection<Transaction> transactions, boolean net) {

        double pL = 0.0;

        Collection<Transaction> closedPositionTransactions = getClosedPositionTransactionsSingleSecurity(transactions);
        for (Transaction transaction : closedPositionTransactions) {

            pL += net ? transaction.getNetValueDouble() : transaction.getGrossValueDouble();
        }

        return pL;
    }

    /**
     * Returns the total Cost of a Position based on the specified {@code transactions}
     * @return positive for BUY
     */
    public static double getCost(Collection<Transaction> transactions, boolean net) {

        double cost = 0.0;

        List<Transaction> openPositionTransactions = getOpenPositionTransactionsSingleSecurity(transactions);
        for (Transaction transaction : openPositionTransactions) {

            cost -= net ? transaction.getNetValueDouble() : transaction.getGrossValueDouble();
        }

        return cost;
    }

    /**
     * Returns the Average Price of a Position based on the specified {@code transactions}
     *
     * @param net if set to true the net price will be returned
     * @return positive for BUY
     */
    public static double getAveragePrice(Security security, Collection<Transaction> transactions, boolean net) {

        long totalQuantity = 0;
        double totalPrice = 0.0;

        List<Transaction> openPositionTransactions = getOpenPositionTransactionsSingleSecurity(transactions);
        for (Transaction transaction : openPositionTransactions) {

            totalQuantity += transaction.getQuantity();
            totalPrice -= net ? transaction.getNetValueDouble() : transaction.getGrossValueDouble();

        }
        return totalPrice / totalQuantity / security.getSecurityFamily().getContractSize();

    }

    /**
     * Returns the Average Age of a Position based on the specified {@code transactions}
     */
    public static double getAverageAge(Collection<Transaction> transactions) {

        long totalQuantity = 0;
        long totalAge = 0;

        List<Transaction> openPositionTransactions = getOpenPositionTransactionsSingleSecurity(transactions);
        for (Transaction transaction : openPositionTransactions) {

            long age = DateUtil.getCurrentEPTime().getTime() - transaction.getDateTime().getTime();

            totalQuantity += transaction.getQuantity();
            totalAge += transaction.getQuantity() * age;

        }
        if (totalQuantity != 0) {
            return totalAge / totalQuantity / 86400000.0;
        } else {
            return Double.NaN;
        }
    }

    private static List<Transaction> getOpenPositionTransactionsSingleSecurity(Collection<Transaction> transactions) {

        ArrayList<Transaction> sortedTransactions = new ArrayList<Transaction>(transactions);

        // sort by date ascending
        Collections.sort(sortedTransactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                return (t1.getDateTime().compareTo(t2.getDateTime()));
            }
        });

        List<Transaction> openPositionTransactions = new ArrayList<Transaction>();
        long totalQuantity = 0;
        for (Transaction transaction : sortedTransactions) {

            // if queue is empty or transaction increases existing position -> add transaction to queue
            if (openPositionTransactions.size() == 0 || Long.signum(totalQuantity) == Long.signum(transaction.getQuantity())) {

                Transaction openPositionTransaction = cloneTransaction(transaction, transaction.getQuantity());
                openPositionTransactions.add(openPositionTransaction);

                // if transaction is reducing quantity -> go through the queue and remove as many items as necessary
            } else {
                long runningQuantity = transaction.getQuantity();
                for (Iterator<Transaction> it = openPositionTransactions.iterator(); it.hasNext();) {

                    Transaction openPositionTransaction = it.next();

                    // transaction will be completely removed
                    if (Math.abs(openPositionTransaction.getQuantity()) <= Math.abs(runningQuantity)) {
                        runningQuantity += openPositionTransaction.getQuantity();
                        it.remove();

                        // transaction will be partly removed
                    } else {
                        adjustTransaction(openPositionTransaction, openPositionTransaction.getQuantity() + runningQuantity);
                        runningQuantity = 0;
                        break;
                    }
                }

                // if not the entire runningQuantity could be eliminated,
                // create a new Quantity Transaction with the reminder
                if (runningQuantity != 0) {
                    openPositionTransactions.add(cloneTransaction(transaction, runningQuantity));
                }
            }
            totalQuantity += transaction.getQuantity();
        }

        return openPositionTransactions;
    }

    private static Collection<Transaction> getClosedPositionTransactionsSingleSecurity(Collection<Transaction> transactions) {

        // add the transactions to a map by their id
        Map<Integer, Transaction> transactionMap = new HashMap<Integer, Transaction>();
        for (Transaction transaction : transactions) {
            Transaction clone = cloneTransaction(transaction, transaction.getQuantity());
            transactionMap.put(transaction.getId(), clone);
        }

        // go through the list of openPositionTransactions
        List<Transaction> openPositionTransactions = getOpenPositionTransactionsSingleSecurity(transactions);
        for (Transaction openPositionTransaction : openPositionTransactions) {

            // reduce the quantity of the transaction by the corresponding amount
            Transaction transaction = transactionMap.get(openPositionTransaction.getId());
            long quantity = transaction.getQuantity() - openPositionTransaction.getQuantity();
            if (quantity == 0) {
                transactionMap.remove(openPositionTransaction.getId());
            } else {
                adjustTransaction(transaction, quantity);
            }
        }

        return transactionMap.values();
    }

    private static Transaction cloneTransaction(Transaction transaction, long quantity) {

        Transaction clone;
        try {
            clone = (Transaction) BeanUtils.cloneBean(transaction);

            // reduce commissions
            int scale = transaction.getSecurity().getSecurityFamily().getScale();
            if (clone.getExecutionCommission() != null) {
                double executionCommission = clone.getExecutionCommission().doubleValue();
                double newExecutionCommission = executionCommission / clone.getQuantity() * quantity;
                clone.setExecutionCommission(RoundUtil.getBigDecimal(newExecutionCommission, scale));
            }

            if (clone.getClearingCommission() != null) {
                double clearingCommission = clone.getClearingCommission().doubleValue();
                double newClearingCommission = clearingCommission / clone.getQuantity() * quantity;
                clone.setClearingCommission(RoundUtil.getBigDecimal(newClearingCommission, scale));
            }

            // set new quantity
            clone.setQuantity(quantity);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return clone;
    }

    private static void adjustTransaction(Transaction transaction, long quantity) {

        int scale = transaction.getSecurity().getSecurityFamily().getScale();
        if (transaction.getExecutionCommission() != null) {
            double executionCommission = transaction.getExecutionCommission().doubleValue();
            double newExecutionCommission = executionCommission / transaction.getQuantity() * quantity;
            transaction.setExecutionCommission(RoundUtil.getBigDecimal(newExecutionCommission, scale));
        }

        if (transaction.getClearingCommission() != null) {
            double clearingCommission = transaction.getClearingCommission().doubleValue();
            double newClearingCommission = clearingCommission / transaction.getQuantity() * quantity;
            transaction.setClearingCommission(RoundUtil.getBigDecimal(newClearingCommission, scale));
        }

        transaction.setQuantity(quantity);
    }
}
