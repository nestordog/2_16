package com.algoTrader.service;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;


public class TransactionServiceImpl extends com.algoTrader.service.TransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());

    protected Transaction handleExecuteTransaction(int quantity, Security security, BigDecimal current, BigDecimal commission, TransactionType transactionType)
            throws Exception {

        if (quantity == 0) return null;

        //TODO Execute SQ Transaction

        BigDecimal price = current.abs();

        // Account
        Account account = getAccountDao().findByCurrency(security.getCurrency());

        // Transaction
        Transaction transaction = new TransactionImpl();

        quantity = TransactionType.SELL.equals(transactionType) ? -Math.abs(quantity) : Math.abs(quantity);

        transaction.setNumber(0);
        transaction.setDateTime(DateUtil.getCurrentEPTime());
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setCommission(commission);
        transaction.setType(transactionType);
        transaction.setSecurity(security);

        transaction.setAccount(account);
        account.getTransactions().add(transaction);


        // Position
        Position position = security.getPosition();
        if (position == null) {

            position = new PositionImpl();
            position.setQuantity(quantity);

            position.setExitValue(new BigDecimal(0));
            position.setMargin(new BigDecimal(0));

            position.setSecurity(security);
            security.setPosition(position);

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            position.setAccount(account);
            account.getPositions().add(position);

            getPositionDao().create(position);

        } else {

            // attach the object
            position.setQuantity(position.getQuantity() + quantity);

            position.setExitValue(new BigDecimal(0));
            position.setMargin(new BigDecimal(0));

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            getPositionDao().update(position);
        }

        getTransactionDao().create(transaction);
        getAccountDao().update(account);
        getSecurityDao().update(security);

        logger.info("executed transaction type: " + transactionType + " quantity: " + transaction.getQuantity() + " of " + security.getSymbol() + " price: " + transaction.getPrice() + " commission: " + transaction.getCommission() + " balance: " + account.getBalance());

        EsperService.getEPServiceInstance().getEPRuntime().sendEvent(transaction);

        return transaction;
    }
}
