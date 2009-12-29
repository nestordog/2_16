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

    protected void handleExpireTransactions() throws Exception {

        List list = getPositionDao().findExpiredPositions();

        for (Iterator it = list.iterator(); it.hasNext(); ) {

            Position position = (Position)it.next();

            // StockOption
            StockOption option = (StockOptionImpl)position.getSecurity();

            // Account
            Account account = getAccountDao().load(position.getAccount().getId());

            // Transaction
            Transaction transaction = new TransactionImpl();

            transaction.setNumber(0); // we dont habe a number
            transaction.setDateTime(option.getExpiration());
            transaction.setQuantity(- position.getQuantity());
            transaction.setPrice(new BigDecimal(0));
            transaction.setCommission(new BigDecimal(0));
            transaction.setType(TransactionType.EXPIRATION);
            transaction.setSecurity(option);

            transaction.setAccount(account);
            account.getTransactions().add(transaction);

            // attach the object
            position.setQuantity(0);
            position.setExitValue(new BigDecimal(0));
            position.setMargin(new BigDecimal(0));

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            getPositionDao().update(position);
            getTransactionDao().create(transaction);
            getAccountDao().update(account);
        }
    }

    protected void handleSetMargin(Position position, BigDecimal settlement, BigDecimal underlaying) throws ConvergenceException, FunctionEvaluationException {

        StockOption option = (StockOption)position.getSecurity();

        BigDecimal margin = StockOptionUtil.getMargin(option, settlement, underlaying);

        int quantity = Math.abs(position.getQuantity());
        position.setMargin(margin.multiply(new BigDecimal(quantity)));

        getPositionDao().update(position);
    }
}
