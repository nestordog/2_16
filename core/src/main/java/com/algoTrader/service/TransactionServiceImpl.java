package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.entity.strategy.Account;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.esper.subscriber.Subscriber;
import com.algoTrader.util.CollectionUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.vo.ClosePositionVO;
import com.algoTrader.vo.OpenPositionVO;
import com.algoTrader.vo.PositionMutationVO;
import com.algoTrader.vo.TradePerformanceVO;

public abstract class TransactionServiceImpl extends TransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());
    private static Logger mailLogger = MyLogger.getLogger(TransactionServiceImpl.class.getName() + ".MAIL");
    private static Logger simulationLogger = MyLogger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");

    private @Value("${simulation}") boolean simulation;
    private @Value("${simulation.logTransactions}") boolean logTransactions;

    @Override
    protected void handleCreateTransaction(Fill fill) {

        Order order = fill.getOrd();

        // reload the strategy and security to get potential changes
        Strategy strategy = getStrategyDao().load(order.getStrategy().getId());
        Security security = getSecurityDao().load(order.getSecurity().getId());

        // and update the strategy and security of the order
        order.setStrategy(strategy);
        order.setSecurity(security);

        SecurityFamily securityFamily = security.getSecurityFamily();
        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();
        BigDecimal executionCommission = RoundUtil.getBigDecimal(Math.abs(quantity * securityFamily.getExecutionCommission().doubleValue()));
        BigDecimal clearingCommission = securityFamily.getClearingCommission() != null ? RoundUtil.getBigDecimal(Math.abs(quantity * securityFamily.getClearingCommission().doubleValue())) : null;

        Transaction transaction = new TransactionImpl();
        transaction.setDateTime(fill.getExtDateTime());
        transaction.setExtId(fill.getExtId());
        transaction.setQuantity(quantity);
        transaction.setPrice(fill.getPrice());
        transaction.setType(transactionType);
        transaction.setSecurity(security);
        transaction.setStrategy(strategy);
        transaction.setCurrency(securityFamily.getCurrency());
        transaction.setExecutionCommission(executionCommission);
        transaction.setClearingCommission(clearingCommission);
        transaction.setAccount(order.getAccount());

        processTransaction(transaction);
    }

    @Override
    protected void handleCreateTransaction(int securityId, String strategyName, String extId, Date dateTime, long quantity, BigDecimal price, BigDecimal executionCommission,
            BigDecimal clearingCommission, Currency currency, TransactionType transactionType, String accountName) {

        // validations
        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("strategy " + strategyName + " was not found");
        }

        Security security = getSecurityDao().findById(securityId);
        if (TransactionType.BUY.equals(transactionType) ||
                TransactionType.SELL.equals(transactionType) ||
                TransactionType.EXPIRATION.equals(transactionType) ||
                TransactionType.TRANSFER.equals(transactionType)) {

            if (security == null) {
                throw new IllegalArgumentException("security " + securityId + " was not found");
            }

            currency = security.getSecurityFamily().getCurrency();

            if (TransactionType.BUY.equals(transactionType)) {
                quantity = Math.abs(quantity);
            } else if (TransactionType.SELL.equals(transactionType)) {
                quantity = -Math.abs(quantity);
            }

            // for EXPIRATION or TRANSFER the actual quantity istaken

        } else if (TransactionType.CREDIT.equals(transactionType) ||
                TransactionType.INTREST_RECEIVED.equals(transactionType) ||
                TransactionType.REFUND.equals(transactionType)) {

            if (currency == null) {
                throw new IllegalArgumentException("need to define a currency for " + transactionType);
            }

            quantity = 1;

        } else if (TransactionType.DEBIT.equals(transactionType) || TransactionType.INTREST_PAID.equals(transactionType) || TransactionType.FEES.equals(transactionType)) {

            if (currency == null) {
                throw new IllegalArgumentException("need to define a currency for " + transactionType);
            }

            quantity = -1;

        } else if (TransactionType.REBALANCE.equals(transactionType)) {

            throw new IllegalArgumentException("transaction type REBALANCE not allowed");
        }

        Account account = getAccountDao().findByName(accountName);

        // create the transaction
        Transaction transaction = new TransactionImpl();
        transaction.setDateTime(dateTime);
        transaction.setExtId(extId);
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setType(transactionType);
        transaction.setSecurity(security);
        transaction.setStrategy(strategy);
        transaction.setCurrency(currency);
        transaction.setExecutionCommission(executionCommission);
        transaction.setClearingCommission(clearingCommission);
        transaction.setAccount(account);

        processTransaction(transaction);
    }

    @Override
    protected void handleProcessTransaction(Transaction transaction) {

        // need to access transactionService through serviceLocator to get a new transaction
        TransactionService transactionService = ServiceLocator.instance().getService("transactionService", TransactionService.class);

        PositionMutationVO positionMutationEvent = transactionService.persistTransaction(transaction);

        // check if esper is initialized
        if (EsperManager.isInitialized(StrategyImpl.BASE)) {

            // propagate the positionMutationEvent to the corresponding strategy
            if (positionMutationEvent != null) {
                EsperManager.sendEvent(positionMutationEvent.getStrategy(), positionMutationEvent);
            }

            // propagate the transaction to the corresponding strategy
            if (!StrategyImpl.BASE.equals(transaction.getStrategy().getName())) {
                EsperManager.sendEvent(transaction.getStrategy().getName(), transaction);
            }
        }
    }

    @Override
    protected PositionMutationVO handlePersistTransaction(Transaction transaction) {

        double profit = 0.0;
        double profitPct = 0.0;
        double avgAge = 0;

        OpenPositionVO openPositionVO = null;
        ClosePositionVO closePositionVO = null;

        // position handling (incl ClosePositionVO and TradePerformanceVO)
        if (transaction.getSecurity() != null) {

            // create a new position if necessary
            boolean existingOpenPosition = false;
            Position position = getPositionDao().findBySecurityAndStrategyIdLocked(transaction.getSecurity().getId(), transaction.getStrategy().getId());
            if (position == null) {

                position = new PositionImpl();
                position.setQuantity(transaction.getQuantity());

                position.setExitValue(null);
                position.setMaintenanceMargin(null);

                // associate the security
                transaction.getSecurity().addPositions(position);

                // associate the transaction
                position.addTransactions(transaction);

                // associate the strategy
                position.setStrategy(transaction.getStrategy());

                getPositionDao().create(position);

            } else {

                existingOpenPosition = position.isOpen();

                // get the closePositionVO (must be done before closing the position)
                closePositionVO = getPositionDao().toClosePositionVO(position);

                // evaluate the profit in closing transactions (must be done before attaching the new transaction)
                if (Long.signum(position.getQuantity()) * Long.signum(transaction.getQuantity()) == -1) {

                    double cost, value;
                    if (Math.abs(transaction.getQuantity()) <= Math.abs(position.getQuantity())) {
                        cost = position.getCostDouble() * Math.abs((double) transaction.getQuantity() / (double) position.getQuantity());
                        value = transaction.getNetValueDouble();
                    } else {
                        cost = position.getCostDouble();
                        value = transaction.getNetValueDouble() * Math.abs((double) position.getQuantity() / (double) transaction.getQuantity());
                    }

                    profit = value - cost;
                    profitPct = Direction.LONG.equals(position.getDirection()) ? ((value - cost) / cost) : ((cost - value) / cost);
                    avgAge = position.getAverageAge();
                }

                position.setQuantity(position.getQuantity() + transaction.getQuantity());

                // in case a position was closed reset exitValue and margin
                if (!position.isOpen()) {

                    // set all values to null
                    position.setExitValue(null);
                    position.setMaintenanceMargin(null);
                } else {

                    // reset the closePosition event
                    closePositionVO = null;
                }

                // associate the position
                position.addTransactions(transaction);
            }

            // if no position was open before initialize the openPosition event
            if (!existingOpenPosition) {

                openPositionVO = getPositionDao().toOpenPositionVO(position);
            }
        }

        // add the amount to the corresponding cashBalance
        getCashBalanceService().processTransaction(transaction);

        // save a portfolioValue (if necessary)
        getPortfolioPersistenceService().savePortfolioValue(transaction.getStrategy(), transaction);

        // update all entities
        getTransactionDao().create(transaction);

        // propagate the TradePerformance event
        if (profit != 0.0 && this.simulation) {
            TradePerformanceVO tradePerformance = new TradePerformanceVO();
            tradePerformance.setProfit(profit);
            tradePerformance.setProfitPct(profitPct);
            tradePerformance.setAvgAge(avgAge);
            tradePerformance.setWinning(profit > 0);

            if (EsperManager.isInitialized(StrategyImpl.BASE)) {
                EsperManager.sendEvent(StrategyImpl.BASE, tradePerformance);
            }
        }

        //@formatter:off
        String logMessage = "executed transaction: " + transaction +
        ((profit != 0.0) ? (
            " profit: " + RoundUtil.getBigDecimal(profit) +
            " profitPct: " + RoundUtil.getBigDecimal(profitPct) +
            " avgAge: " + RoundUtil.getBigDecimal(avgAge, 4))
            : "");
        //@formatter:on

        if (this.simulation && this.logTransactions) {
            simulationLogger.info(logMessage);
        } else {
            logger.info(logMessage);
        }

        // return PositionMutation event if existent
        if (openPositionVO != null) {
            return openPositionVO;
        } else if (closePositionVO != null)
            return closePositionVO;
         else {
            return null;
        }
    }

    @Override
    protected void handlePropagateFill(Fill fill) {

        // send the fill to the strategy that placed the corresponding order
        if (!StrategyImpl.BASE.equals(fill.getOrd().getStrategy().getName())) {
            EsperManager.sendEvent(fill.getOrd().getStrategy().getName(), fill);
        }

        if (!this.simulation) {
            logger.info("received fill: " + fill + " for order: " + fill.getOrd());
        }
    }

    @Override
    protected void handleLogFillSummary(List<Fill> fills) {

        if (fills.size() > 0 && !this.simulation) {

            long totalQuantity = 0;
            double totalPrice = 0.0;

            for (Fill fill : fills) {

                totalQuantity += fill.getQuantity();
                totalPrice += fill.getPrice().doubleValue() * fill.getQuantity();
            }

            Fill fill = CollectionUtil.getFirstElement(fills);
            Order order = fill.getOrd();
            SecurityFamily securityFamily = order.getSecurity().getSecurityFamily();

            //@formatter:off
            mailLogger.info("executed transaction: " +
                    fill.getSide() +
                    " " + totalQuantity +
                    " " + order.getSecurity() +
                    " avgPrice: " + RoundUtil.getBigDecimal(totalPrice / totalQuantity, securityFamily.getScale()) +
                    " " + securityFamily.getCurrency() +
                    " strategy: " + order.getStrategy());
            //@formatter:on
        }
    }

    public static class LogTransactionSummarySubscriber {

        public void update(Map<?, ?>[] insertStream, Map<?, ?>[] removeStream) {

            List<Fill> fills = new ArrayList<Fill>();
            for (Map<?, ?> element : insertStream) {
                Fill fill = (Fill) element.get("fill");
                fills.add(fill);
            }

            ServiceLocator.instance().getService("transactionService", TransactionService.class).logFillSummary(fills);
        }
    }

    public static class CreateTransactionSubscriber extends Subscriber {

        /*
         * synchronized to make sure that only on transaction is created at a time
         */
        public void update(Fill fill) {

            long startTime = System.nanoTime();
            logger.debug("createTransaction start");
            ServiceLocator.instance().getService("transactionService", TransactionService.class).createTransaction(fill);
            logger.debug("createTransaction end");
            MetricsUtil.accountEnd("CreateTransactionSubscriber", startTime);
        }
    }
}
