package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Order;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;

public abstract class TransactionServiceImpl extends com.algoTrader.service.TransactionServiceBase {

    private static final int firstTradingHour = PropertiesUtil.getIntProperty("simulation.firstTradingHour");;

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());

    private static boolean externalTransactionsEnabled = PropertiesUtil.getBooleanProperty("externalTransactionsEnabled");
    private static boolean simulation = PropertiesUtil.getBooleanProperty("simulation");

    @SuppressWarnings("unchecked")
    protected void handleExecuteTransaction(Order order) throws Exception {

        Security security = order.getSecurity();
        TransactionType transactionType = order.getTransactionType();
        long requestedQuantity = order.getRequestedQuantity();

        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }

        if (!OrderStatus.PREARRANGED.equals(order.getStatus())) {
            if (!simulation && externalTransactionsEnabled &&
                    (TransactionType.BUY.equals(transactionType) || TransactionType.SELL.equals(transactionType))) {
                executeExternalTransaction(order);
            } else {
                executeInternalTransaction(order);
            }
        }

        Collection<Transaction> transactions = order.getTransactions();
        for (Transaction transaction : transactions) {

            transaction.setType(transactionType);
            transaction.setSecurity(security);

            // Account
            Account account = getAccountDao().findByCurrency(security.getCurrency());
            transaction.setAccount(account);
            account.getTransactions().add(transaction);

            // Position
            Position position = security.getPosition();
            if (position == null) {

                position = new PositionImpl();
                position.setQuantity(transaction.getQuantity());

                position.setExitValue(null);
                position.setMargin(null);

                position.setSecurity(security);
                security.setPosition(position);

                position.getTransactions().add(transaction);
                transaction.setPosition(position);

                position.setAccount(account);
                account.getPositions().add(position);

                getPositionDao().create(position);

            } else {

                // attach the object
                position.setQuantity(position.getQuantity() + transaction.getQuantity());

                if (!position.isOpen()) {
                    position.setExitValue(null);
                    position.setMargin(null);
                }

                position.getTransactions().add(transaction);
                transaction.setPosition(position);

                getPositionDao().update(position);
            }

            getTransactionDao().create(transaction);
            getAccountDao().update(account);
            getSecurityDao().update(security);

            logger.info("executed transaction type: " + transactionType + " quantity: " + transaction.getQuantity() + " of " + security.getSymbol() + " price: " + transaction.getPrice() + " commission: " + transaction.getCommission() + " portfolioValue: " + account.getTotalValue());

            EsperService.route(transaction);
        }
    }

    @SuppressWarnings("unchecked")
    private void executeInternalTransaction(Order order) {

        Transaction transaction = new TransactionImpl();
        transaction.setDateTime(DateUtil.getCurrentEPTime());

        StockOption stockOption = (StockOption)order.getSecurity();
        double currentValue = stockOption.getLastTick().getCurrentValueDouble();

        // in daily or (half)hourly simulation, if exitValue is reached during the day, take the exitValue
        // instead of the currentValue! because we will have passed the exitValue in the meantime
        long eventsPerDay = (Long)EsperService.getVariableValue("var_events_per_day");
        if (simulation && TransactionType.BUY.equals(order.getTransactionType()) && (eventsPerDay <= 33)) {

            double exitValue = stockOption.getPosition().getExitValue().doubleValue();
            long currentHour = DateUtils.getFragmentInHours(DateUtil.getCurrentEPTime(), Calendar.DAY_OF_YEAR);
            if (currentValue > exitValue && currentHour > firstTradingHour) {

                logger.info("adjusted currentValue (" + currentValue + ") to exitValue (" + exitValue+ ") in closePosition for order on " + order.getSecurity().getSymbol());
                currentValue = exitValue;
            }
        }

        int contractSize = stockOption.getContractSize();

        if (TransactionType.SELL.equals(order.getTransactionType())) {

            double dummyBid = StockOptionUtil.getDummyBid(currentValue);
            BigDecimal price = RoundUtil.getBigDecimal(dummyBid * contractSize);
            transaction.setPrice(price);
            transaction.setQuantity(-Math.abs(order.getRequestedQuantity()));

        } else if (TransactionType.BUY.equals(order.getTransactionType())) {

            double dummyAsk = StockOptionUtil.getDummyAsk(currentValue);
            BigDecimal price = RoundUtil.getBigDecimal(dummyAsk * contractSize);
            transaction.setPrice(price);
            transaction.setQuantity(Math.abs(order.getRequestedQuantity()));

        } else if (TransactionType.EXPIRATION.equals(order.getTransactionType())) {

            double underlayingSpot = stockOption.getUnderlaying().getLastTick().getCurrentValueDouble();
            double intrinsicValue = StockOptionUtil.getIntrinsicValue(stockOption, underlayingSpot);
            BigDecimal price = RoundUtil.getBigDecimal(intrinsicValue * contractSize);
            transaction.setPrice(price);
            transaction.setQuantity(Math.abs(order.getRequestedQuantity()));
        }

        transaction.setCommission(order.getSecurity().getCommission(order.getRequestedQuantity(), order.getTransactionType()));
        transaction.setNumber(null);

        order.setStatus(OrderStatus.AUTOMATIC);
        order.getTransactions().add(transaction);
    }
}
