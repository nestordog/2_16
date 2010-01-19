package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.StockOptionUtil;


public class TransactionServiceImpl extends com.algoTrader.service.TransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());

    protected int handleExecuteTransaction(int quantity, Security security, BigDecimal current)
            throws Exception {

        //TODO Execute SQ Transaction

        BigDecimal price = current;

        // Account
        Account account = getAccountDao().findByCurrency(security.getCurrency());

        // Transaction
        Transaction transaction = new TransactionImpl();

        transaction.setNumber(0);
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
                position.setExitValue(null); //will be set to a correct value by a separate rule (very 30min)
                position.setMargin(null); //will be set to a correct value by a separate rule
            }

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            getPositionDao().update(position);
        }

        getTransactionDao().create(transaction);
        getAccountDao().update(account);
        getSecurityDao().update(security);

        logger.info("executed transaction quantity: " + transaction.getQuantity() + " of " + security.getSymbol() + " price: " + transaction.getPrice() + " commission: " + transaction.getCommission() + " new balance: " + account.getBalance());

        EsperService.getEPServiceInstance().getEPRuntime().sendEvent(transaction);

        return quantity;
    }

    protected void handleClosePosition(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        Security security = position.getSecurity();
        BigDecimal current = security.getLastTick().getCurrentValue();

        int quantity = - position.getQuantity();

        executeTransaction(quantity, position.getSecurity(), current);
    }

    protected void handleOpenPosition(int securityId, BigDecimal settlement, BigDecimal currentValue, BigDecimal underlaying) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(securityId);

        Account account = getAccountDao().findByCurrency(stockOption.getCurrency());

        double balance = account.getBalance().doubleValue();
        double margin = StockOptionUtil.getMargin(stockOption, settlement, underlaying).doubleValue();
        double current = currentValue.doubleValue();

        int quantity = (int)(balance / (margin - current));

        executeTransaction(-quantity, stockOption, currentValue);
    }
}
