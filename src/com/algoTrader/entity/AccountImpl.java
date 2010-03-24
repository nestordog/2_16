package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Iterator;

import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.RoundUtil;

public class AccountImpl extends com.algoTrader.entity.Account {

    private static final long serialVersionUID = -2271735085273721632L;

    public BigDecimal getBalance() {

        double balance = 0.0;
        for (Iterator it = getTransactions().iterator(); it.hasNext(); ) {
            Transaction transaction = (Transaction)it.next();
            if (transaction.getType().equals(TransactionType.BUY) ||
                transaction.getType().equals(TransactionType.SELL) ||
                transaction.getType().equals(TransactionType.EXPIRATION)) {

                balance -= (transaction.getPrice().doubleValue() * (double)transaction.getQuantity());
                balance -= transaction.getCommission().doubleValue();

            } else if (transaction.getType().equals(TransactionType.CREDIT) ||
                    transaction.getType().equals(TransactionType.DIVIDEND) ||
                    transaction.getType().equals(TransactionType.INTREST)) {

                balance += transaction.getPrice().doubleValue();

            } else if (transaction.getType().equals(TransactionType.DEBIT) ||
                    transaction.getType().equals(TransactionType.FEES)) {

                balance -= transaction.getPrice().doubleValue();
            }
        }
        return RoundUtil.getBigDecimal(balance);
    }

    public BigDecimal getMargin() {

        double margin = 0.0;
        for (Iterator it = getPositions().iterator(); it.hasNext(); ) {
            Position position = (Position)it.next();
            if (position.getQuantity() != 0) {
                if (position.getMargin() == null) {
                    break;
                }
                margin += position.getMargin().doubleValue();
            }
        }
        return RoundUtil.getBigDecimal(margin);
    }

    public BigDecimal getAvailableAmount() {

        double availableAmount = getBalance().doubleValue() - getMargin().doubleValue();
        return RoundUtil.getBigDecimal(availableAmount);
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

    public BigDecimal getPortfolioValue() {

        double portfolioValue = getBalance().doubleValue();
        for (Iterator it = getPositions().iterator(); it.hasNext(); ) {
            Position position = (Position)it.next();
            Security security = position.getSecurity();
            Tick tick = security.getLastTick();
            if (position.getQuantity() != 0 && tick != null) {
                portfolioValue += position.getQuantity() * security.getCurrentValuePerContract().doubleValue();
            }
        }
        return RoundUtil.getBigDecimal(portfolioValue);
    }
}
