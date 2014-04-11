/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.esper.subscriber.Subscriber;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.PositionUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.OpenPositionVO;
import ch.algotrader.vo.PositionMutationVO;
import ch.algotrader.vo.TradePerformanceVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class TransactionServiceImpl extends TransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());
    private static Logger mailLogger = MyLogger.getLogger(TransactionServiceImpl.class.getName() + ".MAIL");
    private static Logger simulationLogger = MyLogger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");

    private @Value("${simulation}") boolean simulation;
    private @Value("${simulation.logTransactions}") boolean logTransactions;
    private @Value("${misc.portfolioDigits}") int portfolioDigits;

    @Override
    protected void handleCreateTransaction(Fill fill) {

        Order order = fill.getOrder();
        Broker broker = order.getAccount().getBroker();

        // reload the strategy and security to get potential changes
        Strategy strategy = getStrategyDao().load(order.getStrategy().getId());
        Security security = getSecurityDao().load(order.getSecurity().getId());

        // and update the strategy and security of the order
        order.setStrategy(strategy);
        order.setSecurity(security);

        SecurityFamily securityFamily = security.getSecurityFamily();
        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();
        BigDecimal executionCommission = RoundUtil.getBigDecimal(Math.abs(quantity * securityFamily.getExecutionCommission(broker).doubleValue()));
        BigDecimal clearingCommission = securityFamily.getClearingCommission(broker) != null ? RoundUtil.getBigDecimal(Math.abs(quantity * securityFamily.getClearingCommission(broker).doubleValue())) : null;

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setDateTime(fill.getExtDateTime());
        transaction.setExtId(fill.getExtId());
        transaction.setIntOrderId(fill.getOrder().getIntId());
        transaction.setExtOrderId(fill.getOrder().getExtId());
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
            BigDecimal clearingCommission, BigDecimal fee, Currency currency, TransactionType transactionType, String accountName, String description) {

        // validations
        Strategy strategy = getStrategyDao().findByName(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("strategy " + strategyName + " was not found");
        }

        int scale = this.portfolioDigits;
        Security security = getSecurityDao().findById(securityId);
        if (TransactionType.BUY.equals(transactionType) ||
                TransactionType.SELL.equals(transactionType) ||
                TransactionType.EXPIRATION.equals(transactionType) ||
                TransactionType.TRANSFER.equals(transactionType)) {

            if (security == null) {
                throw new IllegalArgumentException("security " + securityId + " was not found");
            }

            currency = security.getSecurityFamily().getCurrency();
            scale = security.getSecurityFamily().getScale();

            if (TransactionType.BUY.equals(transactionType)) {
                quantity = Math.abs(quantity);
            } else if (TransactionType.SELL.equals(transactionType)) {
                quantity = -Math.abs(quantity);
            }

            // for EXPIRATION or TRANSFER the actual quantity istaken

        } else if (TransactionType.CREDIT.equals(transactionType) ||
                TransactionType.INTREST_RECEIVED.equals(transactionType) ||
                TransactionType.DIVIDEND.equals(transactionType) ||
                TransactionType.REFUND.equals(transactionType)) {

            if (currency == null) {
                throw new IllegalArgumentException("need to define a currency for " + transactionType);
            }

            quantity = 1;

        } else if (TransactionType.DEBIT.equals(transactionType) ||
                TransactionType.INTREST_PAID.equals(transactionType) ||
                TransactionType.FEES.equals(transactionType)) {

            if (currency == null) {
                throw new IllegalArgumentException("need to define a currency for " + transactionType);
            }

            quantity = -1;

        } else if (TransactionType.REBALANCE.equals(transactionType)) {

            throw new IllegalArgumentException("transaction type REBALANCE not allowed");
        }

        Account account = getAccountDao().findByName(accountName);

        // create the transaction
        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setDateTime(dateTime);
        transaction.setExtId(extId);
        transaction.setQuantity(quantity);
        transaction.setPrice(price.setScale(scale));
        transaction.setType(transactionType);
        transaction.setSecurity(security);
        transaction.setStrategy(strategy);
        transaction.setCurrency(currency);
        transaction.setExecutionCommission(executionCommission);
        transaction.setClearingCommission(clearingCommission);
        transaction.setFee(fee);
        transaction.setAccount(account);
        transaction.setDescription(description);

        processTransaction(transaction);
    }

    private void processTransaction(final Transaction transaction) {

        // need to access transactionService through serviceLocator to get a new transaction
        TransactionService transactionService = ServiceLocator.instance().getService("transactionService", TransactionService.class);

        PositionMutationVO positionMutationEvent = transactionService.persistTransaction(transaction);

        // check if esper is initialized
        if (EngineLocator.instance().hasBaseEngine()) {

            // propagate the positionMutationEvent to the corresponding strategy
            if (positionMutationEvent != null) {
                EngineLocator.instance().sendEvent(positionMutationEvent.getStrategy(), positionMutationEvent);
            }

            // propagate the transaction to the corresponding strategy and Base
            if (!transaction.getStrategy().isBase()) {
                EngineLocator.instance().sendEvent(transaction.getStrategy().getName(), transaction);
            }
            EngineLocator.instance().sendEvent(StrategyImpl.BASE, transaction);
        }
    }

    @Override
    protected PositionMutationVO handlePersistTransaction(Transaction transaction) {

        OpenPositionVO openPositionVO = null;
        ClosePositionVO closePositionVO = null;
        TradePerformanceVO tradePerformance = null;

        // position handling (incl ClosePositionVO and TradePerformanceVO)
        if (transaction.getSecurity() != null) {

            // create a new position if necessary
            boolean existingOpenPosition = false;
            Position position = getPositionDao().findBySecurityAndStrategyIdLocked(transaction.getSecurity().getId(), transaction.getStrategy().getId());
            if (position == null) {

                position = PositionUtil.processFirstTransaction(transaction);

                // associate strategy
                position.setStrategy(transaction.getStrategy());

                getPositionDao().create(position);

                // associate reverse-relations (after position has received an id)
                transaction.setPosition(position);
                transaction.getSecurity().addPositions(position);

            } else {

                existingOpenPosition = position.isOpen();

                // get the closePositionVO (must be done before closing the position)
                closePositionVO = getPositionDao().toClosePositionVO(position);

                // process the transaction (adjust quantity, cost and realizedPL)
                tradePerformance = PositionUtil.processTransaction(position, transaction);

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
                transaction.setPosition(position);
            }

            // if no position was open before initialize the openPosition event
            if (!existingOpenPosition) {
                openPositionVO = getPositionDao().toOpenPositionVO(position);
            }
        }

        // add the amount to the corresponding cashBalance
        getCashBalanceService().processTransaction(transaction);

        // save a portfolioValue (if necessary)
        getPortfolioPersistenceService().savePortfolioValue(transaction);

        // create the transaction
        getTransactionDao().create(transaction);

        // prepare log message and propagate tradePerformance
        String logMessage = "executed transaction: " + transaction;
        if (tradePerformance != null && tradePerformance.getProfit() != 0.0) {

            logMessage += ",profit=" + RoundUtil.getBigDecimal(tradePerformance.getProfit()) + ",profitPct=" + RoundUtil.getBigDecimal(tradePerformance.getProfitPct());

            // propagate the TradePerformance event
            if (this.simulation && EngineLocator.instance().hasBaseEngine()) {
                EngineLocator.instance().getBaseEngine().sendEvent(tradePerformance);
            }
        }

        if (this.simulation && this.logTransactions) {
            simulationLogger.info(logMessage);
        } else {
            logger.info(logMessage);
        }

        // return PositionMutation event if existent
        return openPositionVO != null ? openPositionVO : closePositionVO != null ? closePositionVO : null;
    }

    @Override
    protected void handlePropagateFill(Fill fill) {

        // send the fill to the strategy that placed the corresponding order
        if (!fill.getOrder().getStrategy().isBase()) {
            EngineLocator.instance().sendEvent(fill.getOrder().getStrategy().getName(), fill);
        }

        if (!this.simulation) {
            logger.info("received fill: " + fill + " for order: " + fill.getOrder());
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
            Order order = fill.getOrder();
            SecurityFamily securityFamily = order.getSecurity().getSecurityFamily();

            //@formatter:off
            mailLogger.info("executed transaction: " +
                    fill.getSide() +
                    "," + totalQuantity +
                    "," + order.getSecurity() +
                    ",avgPrice=" + RoundUtil.getBigDecimal(totalPrice / totalQuantity, securityFamily.getScale()) +
                    "," + securityFamily.getCurrency() +
                    ",strategy=" + order.getStrategy());
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
