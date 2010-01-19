package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Iterator;

public class AccountImpl extends com.algoTrader.entity.Account {

    private static final long serialVersionUID = -2271735085273721632L;

    public BigDecimal getBalance() {

        BigDecimal balance = new BigDecimal(0);
        for (Iterator it = getTransactions().iterator(); it.hasNext(); ) {
            Transaction transaction = (Transaction)it.next();
            balance = balance.add(transaction.getPrice().multiply(new BigDecimal(transaction.getQuantity())));
        }
        return balance;
    }

    public BigDecimal getMargin() {

        BigDecimal margin = new BigDecimal(0);
        for (Iterator it = getPositions().iterator(); it.hasNext(); ) {
            Position position = (Position)it.next();
            if (position.getQuantity() != 0) {
                margin = margin.add(position.getMargin());
            }
        }
        return margin;
    }

    public int getPositionCount() {

        int count = 0;
        for (Iterator it = getPositions().iterator(); it.hasNext(); ) {
            Position position = (Position)it.next();
            if (position.getQuantity() > 0) {
                count++;
            }
        }
        return count;
    }

}
