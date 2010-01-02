package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.StockOptionImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.StockOptionUtil;


public class TransactionServiceImpl extends com.algoTrader.service.TransactionServiceBase {


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

        return quantity;
    }

}
