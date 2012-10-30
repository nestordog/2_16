package com.algoTrader.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.TransactionType;

public class PositionUtil {

    /**
     * positive for SELL
     */
    public static double getRealizedPL(Collection<Transaction> transactions, boolean net) {

        double pL = 0.0;

        List<Pair<Long, Transaction>> closedPositionTransactions = getClosedPositionTransactions(transactions);
        for (Pair<Long, Transaction> pair : closedPositionTransactions) {

            Transaction transaction = pair.getSecond();
            long quantity = pair.getFirst();
            double value = net ? transaction.getNetValueDouble() : transaction.getGrossValueDouble();

            pL += value / transaction.getQuantity() * quantity;
        }

        return pL;
    }

    /**
     * positive for BUY
     */
    public static double getCost(Collection<Transaction> transactions, boolean net) {

        double cost = 0.0;

        List<Pair<Long, Transaction>> openPositionTransactions = getOpenPositionTransactions(transactions);
        for (Pair<Long, Transaction> pair : openPositionTransactions) {

            Transaction transaction = pair.getSecond();
            long quantity = pair.getFirst();
            double value = net ? transaction.getNetValueDouble() : transaction.getGrossValueDouble();

            cost -= value / transaction.getQuantity() * quantity;
        }

        return cost;
    }

    public static double getAveragePrice(Security security, Collection<Transaction> transactions, boolean net) {

        long totalQuantity = 0;
        double totalPrice = 0.0;

        List<Pair<Long, Transaction>> openPositionTransactions = getOpenPositionTransactions(transactions);
        for (Pair<Long, Transaction> pair : openPositionTransactions) {

            Transaction transaction = pair.getSecond();
            long quantity = pair.getFirst();
            double value = net ? transaction.getNetValueDouble() : transaction.getGrossValueDouble();

            totalQuantity += quantity;
            totalPrice -= value / transaction.getQuantity() * quantity;

        }
        return totalPrice / totalQuantity / security.getSecurityFamily().getContractSize();

    }

    public static double getAverageAge(Collection<Transaction> transactions) {

        long totalQuantity = 0;
        long totalAge = 0;

        List<Pair<Long, Transaction>> openPositionTransactions = getOpenPositionTransactions(transactions);
        for (Pair<Long, Transaction> pair : openPositionTransactions) {

            Transaction transaction = pair.getSecond();
            long quantity = pair.getFirst();
            long age = DateUtil.getCurrentEPTime().getTime() - transaction.getDateTime().getTime();

            totalQuantity += quantity;
            totalAge += quantity * age;

        }
        if (totalQuantity != 0) {
            return totalAge / totalQuantity / 86400000.0;
        } else {
            return Double.NaN;
        }
    }

    /**
     * transactions need to be of one security
     */
    public static List<Pair<Long, Transaction>> getOpenPositionTransactions(Collection<Transaction> transactions) {

        ArrayList<Transaction> sortedTransactions = new ArrayList<Transaction>(transactions);

        // sort by date ascending
        Collections.sort(sortedTransactions, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                return (t1.getDateTime().compareTo(t2.getDateTime()));
            }
        });

        List<Pair<Long, Transaction>> quantityTransactionPairs = new ArrayList<Pair<Long, Transaction>>();
        long totalQuantity = 0;
        for (Transaction transaction : sortedTransactions) {

            // if queue is empty or transaction increases existing position -> add transaction to queue
            if (quantityTransactionPairs.size() == 0 || Long.signum(totalQuantity) == Long.signum(transaction.getQuantity())) {
                quantityTransactionPairs.add(new Pair<Long, Transaction>(transaction.getQuantity(), transaction));

                // if transaction is reducing quantity -> go through the queue and remove as many items as necessary
            } else {
                long runningQuantity = transaction.getQuantity();
                for (Iterator<Pair<Long, Transaction>> it = quantityTransactionPairs.iterator(); it.hasNext();) {

                    Pair<Long, Transaction> pair = it.next();

                    // transaction will be completely removed
                    if (Math.abs(pair.getFirst()) <= Math.abs(runningQuantity)) {
                        runningQuantity += pair.getFirst();
                        it.remove();

                        // transaction will be partly removed
                    } else {
                        pair.setFirst(pair.getFirst() + runningQuantity);
                        runningQuantity = 0;
                        break;
                    }
                }

                // if not the entire runningQuantity could be eliminated,
                // create a new Quantity Transaction with the reminder
                if (runningQuantity != 0) {
                    quantityTransactionPairs.add(new Pair<Long, Transaction>(runningQuantity, transaction));
                }
            }
            totalQuantity += transaction.getQuantity();
        }

        return quantityTransactionPairs;
    }


    public static List<Pair<Long, Transaction>> getOpenPositionTransactionsMultiSecurity(Collection<Transaction> transactions) {

        // group BUY and SELL transactions by security
        Map<Security, Collection<Transaction>> transactionsPerSecurity = new HashMap<Security, Collection<Transaction>>();
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals(TransactionType.BUY) || transaction.getType().equals(TransactionType.SELL)) {
                if (!transactionsPerSecurity.containsKey(transaction.getSecurity())) {
                    transactionsPerSecurity.put(transaction.getSecurity(), new HashSet<Transaction>());
                }
                transactionsPerSecurity.get(transaction.getSecurity()).add(transaction);
            }
        }

        // for every security the the corresponding open positions
        List<Pair<Long, Transaction>> openPositionTransactions = new ArrayList<Pair<Long, Transaction>>();
        for (Map.Entry<Security, Collection<Transaction>> entry : transactionsPerSecurity.entrySet()) {

            openPositionTransactions.addAll(getOpenPositionTransactions(entry.getValue()));
        }
        return openPositionTransactions;
    }

    /**
     * transactions need to be of one security
     */
    public static List<Pair<Long, Transaction>> getClosedPositionTransactions(Collection<Transaction> transactions) {

        // compile a list of transactions with their quantities
        Map<Transaction, Long> transactionQuantityMap = new HashMap<Transaction, Long>();
        for (Transaction transaction : transactions) {
            transactionQuantityMap.put(transaction, transaction.getQuantity());
        }

        // go through the list of openPositionTransactions
        List<Pair<Long, Transaction>> openPositionTransactions = getOpenPositionTransactions(transactions);
        for (Pair<Long, Transaction> openPositionTransaction : openPositionTransactions) {

            // reduce the position by the corresponding amount
            long quantity = transactionQuantityMap.get(openPositionTransaction.getSecond()) - openPositionTransaction.getFirst();
            if (quantity == 0) {
                transactionQuantityMap.remove(openPositionTransaction.getSecond());
            } else {
                transactionQuantityMap.put(openPositionTransaction.getSecond(), quantity);
            }
        }

        // compile the closedPositionTransaction List
        List<Pair<Long, Transaction>> closedPositionTransactions = new ArrayList<Pair<Long, Transaction>>();
        for (Map.Entry<Transaction, Long> entry : transactionQuantityMap.entrySet()) {
            closedPositionTransactions.add(new Pair<Long, Transaction>(entry.getValue(), entry.getKey()));
        }

        return closedPositionTransactions;
    }

    public static List<Pair<Long, Transaction>> getClosedPositionTransactionsMultiSecurity(Collection<Transaction> transactions) {

        // group BUY and SELL transactions by security
        Map<Security, Collection<Transaction>> transactionsPerSecurity = new HashMap<Security, Collection<Transaction>>();
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals(TransactionType.BUY) || transaction.getType().equals(TransactionType.SELL)) {
                if (!transactionsPerSecurity.containsKey(transaction.getSecurity())) {
                    transactionsPerSecurity.put(transaction.getSecurity(), new HashSet<Transaction>());
                }
                transactionsPerSecurity.get(transaction.getSecurity()).add(transaction);
            }
        }

        // for every security the the corresponding open positions
        List<Pair<Long, Transaction>> closedPositionTransactions = new ArrayList<Pair<Long, Transaction>>();
        for (Map.Entry<Security, Collection<Transaction>> entry : transactionsPerSecurity.entrySet()) {

            closedPositionTransactions.addAll(getClosedPositionTransactions(entry.getValue()));
        }
        return closedPositionTransactions;
    }
}
