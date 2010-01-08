package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.MyLogger;


public class TransactionServiceImpl extends com.algoTrader.service.TransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());

    protected int handleExecuteTransaction(int quantity, Security security, BigDecimal current)
            throws Exception {

        //TODO Execute SQ Transaction
        BigDecimal price = current;
        int number = 1234;

        // Account
        Account account = getAccountDao().findByCurrency(security.getCurrency());

        // Transaction
        Transaction transaction = new TransactionImpl();

        transaction.setNumber(number);
        transaction.setDateTime(new Date());
        transaction.setQuantity(quantity);
        transaction.setPrice(price.negate());
        transaction.setCommission(new BigDecimal(0)); //TODO Set Commission
        transaction.setType(quantity > 0 ? TransactionType.BUY : TransactionType.SELL);
        transaction.setSecurity(security);

        transaction.setAccount(account);
        account.getTransactions().add(transaction);


        // Position
        Position position = security.getPosition();
        if (position == null) {

            position = new PositionImpl();
            position.setQuantity(quantity);
            position.setExitValue(current.multiply(new BigDecimal(2))); //TODO Set ExitValue
            position.setMargin(new BigDecimal(0)); //TODO Set Margin

            position.setSecurity(security);
            security.setPosition(position);

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            position.setAccount(account);
            account.getPositions().add(position);

            getPositionDao().create(position);

        } else {

            // attach the object
            position = getPositionDao().load(position.getId());
            position.setQuantity(position.getQuantity() + quantity);

            if (position.getQuantity() == 0) {
                position.setExitValue(new BigDecimal(0));
                position.setMargin(new BigDecimal(0));
            } else {
                position.setExitValue(current.multiply(new BigDecimal(2))); //TODO Set ExitValue
                position.setMargin(new BigDecimal(0)); //TODO Set Margin
            }

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            getPositionDao().update(position);
        }

        getTransactionDao().create(transaction);
        getAccountDao().update(account);
        getSecurityDao().update(security);

        logger.info("executed transaction " + transaction + " on " + security.getSymbol());

        return quantity;
    }

    protected void handleClosePosition(Position position) throws Exception {

        Security security = position.getSecurity();
        BigDecimal current = security.getCurrentValue();

        if (current == null) return; // we dont have a current value yet

        int quantity = - position.getQuantity();

        executeTransaction(quantity, position.getSecurity(), current);
    }
}
