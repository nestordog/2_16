package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Iterator;

import com.algoTrader.enumeration.TransactionType;

public class AccountImpl extends com.algoTrader.entity.Account {

    private static final long serialVersionUID = -2271735085273721632L;

    public BigDecimal getBalance() {

        BigDecimal balance = new BigDecimal(0);
        for (Iterator it = getTransactions().iterator(); it.hasNext(); ) {
            Transaction transaction = (Transaction)it.next();
            if (transaction.getType().equals(TransactionType.BUY) ||
                transaction.getType().equals(TransactionType.SELL) ||
                transaction.getType().equals(TransactionType.EXPIRATION)) {

                balance = balance.subtract(transaction.getPrice().multiply(new BigDecimal(transaction.getQuantity())));
                balance = balance.subtract(transaction.getCommission());

            } else if (transaction.getType().equals(TransactionType.CREDIT) ||
                    transaction.getType().equals(TransactionType.DIVIDEND) ||
                    transaction.getType().equals(TransactionType.INTREST)) {

                balance = balance.add(transaction.getPrice());

            } else if (transaction.getType().equals(TransactionType.DEBIT) ||
                    transaction.getType().equals(TransactionType.FEES)) {

                balance = balance.subtract(transaction.getPrice());
            }
        }
        return balance;
    }

    public BigDecimal getMargin() {

        BigDecimal margin = new BigDecimal(0);
        for (Iterator it = getPositions().iterator(); it.hasNext(); ) {
            Position position = (Position)it.next();
            if (position.getQuantity() != 0) {
                if (position.getMargin() == null) {
                    break;
                }
                margin = margin.add(position.getMargin());
            }
        }
        return margin;
    }

    public BigDecimal getAvailableAmount() {

        return getBalance().subtract(getMargin());
    }

    public int getPositionCount() {

        int count = 0;
        for (Iterator it = getPositions().iterator(); it.hasNext(); ) {
            Position position = (Position)it.next();
            if (position.getQuantity() != 0) {
                count++;
            }
        }
        return count;
    }

}
