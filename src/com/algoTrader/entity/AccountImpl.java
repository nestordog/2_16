package com.algoTrader.entity;

import java.math.BigDecimal;
import java.util.Collection;

import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.RoundUtil;

public class AccountImpl extends com.algoTrader.entity.Account {

    private static final long serialVersionUID = -2271735085273721632L;

    @SuppressWarnings("unchecked")
    public BigDecimal getBalance() {

        double balance = 0.0;
        Collection<Transaction> transactions = getTransactions();
        for (Transaction transaction : transactions) {
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

    @SuppressWarnings("unchecked")
    public BigDecimal getMargin() {

        double margin = 0.0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
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

    @SuppressWarnings("unchecked")
    public int getPositionCount() {

        int count = 0;
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            if (position.getQuantity() != 0) {
                count++;
            }
        }
        return count;
    }

    @SuppressWarnings("unchecked")
    public BigDecimal getPortfolioValue() {

        double portfolioValue = getBalance().doubleValue();
        Collection<Position> positions = getPositions();
        for (Position position : positions) {
            Security security = position.getSecurity();
            Tick tick = security.getLastTick();
            if (position.getQuantity() != 0 && tick != null) {
                portfolioValue += position.getQuantity() * security.getCurrentValuePerContract().doubleValue();
            }
        }
        return RoundUtil.getBigDecimal(portfolioValue);
    }

    public double getPortfolioValueDouble() {

        return getPortfolioValue().doubleValue();
    }
}
