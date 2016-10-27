/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.HibernateInitializer;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.PositionVO;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.CashBalanceVO;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.EventRecipient;
import ch.algotrader.report.TradeReport;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.vo.TradePerformanceVO;
import ch.algotrader.vo.TransactionResultVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LogManager.getLogger(TransactionServiceImpl.class);
    private static final Logger MAIL_LOGGER = LogManager.getLogger(TransactionServiceImpl.class.getName() + ".MAIL");

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final TransactionPersistenceService transactionPersistenceService;

    private final StrategyDao strategyDao;

    private final SecurityDao securityDao;

    private final AccountDao accountDao;

    private final EventDispatcher eventDispatcher;

    private final Engine serverEngine;

    private volatile TradeReport tradeReport;

    public TransactionServiceImpl(
            final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final TransactionPersistenceService transactionPersistenceService,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final EventDispatcher eventDispatcher,
            final Engine serverEngine) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(transactionPersistenceService, "TransactionPersistenceService is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.transactionPersistenceService = transactionPersistenceService;
        this.strategyDao = strategyDao;
        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.eventDispatcher = eventDispatcher;
        this.serverEngine = serverEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void createTransaction(final Fill fill) {

        Validate.notNull(fill, "Fill is null");

        long startTime = System.nanoTime();
        LOGGER.debug("createTransaction start");

        Order order = fill.getOrder();
        order.initializeSecurity(HibernateInitializer.INSTANCE);
        order.initializeAccount(HibernateInitializer.INSTANCE);
        order.initializeExchange(HibernateInitializer.INSTANCE);

        // reload the strategy and security to get potential changes
        Strategy strategy = this.strategyDao.load(order.getStrategy().getId());
        Security security = this.securityDao.findByIdInclFamilyUnderlyingExchangeAndBrokerParameters(order.getSecurity().getId());

        SecurityFamily securityFamily = security.getSecurityFamily();
        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setDateTime(fill.getExtDateTime());
        transaction.setExtId(fill.getExtId());
        transaction.setIntOrderId(order.getIntId());
        transaction.setExtOrderId(order.getExtId());
        transaction.setQuantity(quantity);
        transaction.setPrice(fill.getPrice());
        transaction.setType(transactionType);
        transaction.setSecurity(security);
        transaction.setStrategy(strategy);
        transaction.setCurrency(securityFamily.getCurrency());
        transaction.setAccount(order.getAccount());

        String broker = order.getAccount().getBroker();
        if (fill.getExecutionCommission() != null) {
            transaction.setExecutionCommission(fill.getExecutionCommission());
        } else if (securityFamily.getExecutionCommission(broker) != null) {
            transaction.setExecutionCommission(RoundUtil.getBigDecimal(Math.abs(quantity * securityFamily.getExecutionCommission(broker).doubleValue())));
        }

        if (fill.getClearingCommission() != null) {
            transaction.setClearingCommission(fill.getClearingCommission());
        } else if (securityFamily.getClearingCommission(broker) != null) {
            transaction.setClearingCommission(RoundUtil.getBigDecimal(Math.abs(quantity * securityFamily.getClearingCommission(broker).doubleValue())));
        }

        if (fill.getFee() != null) {
            transaction.setFee(fill.getFee());
        } else if (securityFamily.getFee(broker) != null) {
            transaction.setFee(RoundUtil.getBigDecimal(Math.abs(quantity * securityFamily.getFee(broker).doubleValue())));
        }

        processTransaction(transaction);

        LOGGER.debug("createTransaction end");
        MetricsUtil.accountEnd("CreateTransactionSubscriber", startTime);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void createTransaction(final ExternalFill fill) {

        long startTime = System.nanoTime();
        LOGGER.debug("createTransaction start");


        Security security = fill.getSecurity();
        SecurityFamily securityFamily = security != null ? security.getSecurityFamily() : null;
        Currency currency = securityFamily != null ? securityFamily.getCurrency() : fill.getCurrency();
        Account account = fill.getAccount();
        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setDateTime(fill.getExtDateTime());
        transaction.setExtId(fill.getExtId());
        transaction.setIntOrderId(null);
        transaction.setExtOrderId(fill.getExtOrderId());
        transaction.setQuantity(quantity);
        transaction.setPrice(fill.getPrice());
        transaction.setType(transactionType);
        transaction.setSecurity(security);
        transaction.setStrategy(fill.getStrategy());
        transaction.setCurrency(currency);
        transaction.setAccount(account);

        String broker = account != null ? account.getBroker() : null;
        if (fill.getExecutionCommission() != null) {
            transaction.setExecutionCommission(fill.getExecutionCommission());
        } else if (securityFamily != null) {
            BigDecimal executionCommission = securityFamily.getExecutionCommission(broker);
            if (executionCommission != null) {
                transaction.setExecutionCommission(RoundUtil.getBigDecimal(Math.abs(quantity * executionCommission.doubleValue())));
            }
        }

        if (fill.getClearingCommission() != null) {
            transaction.setClearingCommission(fill.getClearingCommission());
        } else if (securityFamily != null) {
            BigDecimal clearingCommission = securityFamily.getClearingCommission(broker);
            if (clearingCommission != null) {
                transaction.setClearingCommission(RoundUtil.getBigDecimal(Math.abs(quantity * clearingCommission.doubleValue())));
            }
        }

        if (fill.getFee() != null) {
            transaction.setFee(fill.getFee());
        } else if (securityFamily != null) {
            final BigDecimal fee = securityFamily.getFee(broker);
            if (fee != null) {
                transaction.setFee(RoundUtil.getBigDecimal(Math.abs(quantity * fee.doubleValue())));
            }
        }

        processTransaction(transaction);

        LOGGER.debug("createTransaction end");
        MetricsUtil.accountEnd("CreateTransactionSubscriber", startTime);
    }

    /**
     * {@inheritDoc}
     */
    // This method needs to be non-transaction in ensure correct creation of position and cash balance records in a separate transaction
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void createTransaction(final long securityId, final String strategyName, final String extId, final Date dateTime, final long quantity, final BigDecimal price,
            final BigDecimal executionCommission, final BigDecimal clearingCommission, final BigDecimal fee, final Currency currency, final TransactionType transactionType, final String accountName,
            final String description) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(dateTime, "Date time is null");
        Validate.notNull(price, "Price is null");
        Validate.notNull(transactionType, "Transaction type is null");

        Currency currencyNonFinal = currency;
        long quantityNonFinal = quantity;
        // validations

        Strategy strategy= this.strategyDao.findByName(strategyName);
        if (strategy == null) {
            throw new ServiceException("strategy " + strategyName + " was not found", null);
        }

        int scale = this.commonConfig.getPortfolioDigits();
        Security security = this.securityDao.get(securityId);
        if (TransactionType.BUY.equals(transactionType) || TransactionType.SELL.equals(transactionType) || TransactionType.EXPIRATION.equals(transactionType)
                || TransactionType.TRANSFER.equals(transactionType)) {

            if (security == null) {
                throw new IllegalArgumentException("security " + securityId + " was not found");
            }

            currencyNonFinal = security.getSecurityFamily().getCurrency();
            scale = security.getSecurityFamily().getScale();

            if (TransactionType.BUY.equals(transactionType)) {
                quantityNonFinal = Math.abs(quantityNonFinal);
            } else if (TransactionType.SELL.equals(transactionType)) {
                quantityNonFinal = -Math.abs(quantityNonFinal);
            }

            // for EXPIRATION or TRANSFER the actual quantity istaken

        } else if (TransactionType.CREDIT.equals(transactionType) || TransactionType.INTREST_RECEIVED.equals(transactionType) || TransactionType.DIVIDEND.equals(transactionType)
                || TransactionType.REFUND.equals(transactionType)) {

            if (currencyNonFinal == null) {
                throw new IllegalArgumentException("need to define a currency for " + transactionType);
            }

            quantityNonFinal = 1;

        } else if (TransactionType.DEBIT.equals(transactionType) || TransactionType.INTREST_PAID.equals(transactionType) || TransactionType.FEES.equals(transactionType)) {

            if (currencyNonFinal == null) {
                throw new IllegalArgumentException("need to define a currency for " + transactionType);
            }

            quantityNonFinal = -1;

        } else if (TransactionType.REBALANCE.equals(transactionType)) {

            throw new IllegalArgumentException("transaction type REBALANCE not allowed");
        }

        Account account = accountName != null ? this.accountDao.findByName(accountName) : null;

        // create the transaction
        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setUuid(UUID.randomUUID().toString());
        transaction.setDateTime(dateTime);
        transaction.setExtId(extId);
        transaction.setQuantity(quantityNonFinal);
        transaction.setPrice(price.setScale(scale));
        transaction.setType(transactionType);
        transaction.setSecurity(security);
        transaction.setStrategy(strategy);
        transaction.setCurrency(currencyNonFinal);
        transaction.setExecutionCommission(executionCommission);
        transaction.setClearingCommission(clearingCommission);
        transaction.setFee(fee);
        transaction.setAccount(account);
        transaction.setDescription(description);

        processTransaction(transaction);

    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void recordTransaction(final Transaction transaction) {

        if (!this.coreConfig.isPositionCheckDisabled()) {

            this.transactionPersistenceService.ensurePositionAndCashBalance(transaction);
        }

        this.transactionPersistenceService.saveTransaction(transaction);
    }

    // This method needs to be non-transaction in ensure correct creation of position and cash balance records in a separate transaction
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void persistTransaction(final Transaction transaction) {

        if (!this.coreConfig.isPositionCheckDisabled()) {

            this.transactionPersistenceService.ensurePositionAndCashBalance(transaction);
        }
        TransactionResultVO transactionResult = this.transactionPersistenceService.saveTransaction(transaction);
        handleTransactionResult(transaction, transactionResult);
    }

    private TransactionResultVO handleTransactionResult(final Transaction transaction, final TransactionResultVO transactionResult) {

        TradePerformanceVO tradePerformance = transactionResult.getTradePerformance();
        if (tradePerformance != null && tradePerformance.getProfit() != 0.0) {

            // propagate the TradePerformance event
            this.serverEngine.sendEvent(tradePerformance);

            // log trade report
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("executed transaction: {},profit={},profitPct=", transaction,
                        RoundUtil.getBigDecimal(tradePerformance.getProfit()),
                        RoundUtil.getBigDecimal(tradePerformance.getProfitPct()));
            }
            printTransaction(transaction, tradePerformance);
        } else {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("executed transaction: {}", transaction);
            }
        }
        List<CashBalanceVO> cashBalances = transactionResult.getCashBalances();
        if (!cashBalances.isEmpty()) {
            Strategy strategy = transaction.getStrategy();
            for (CashBalanceVO cashBalance: cashBalances) {
                this.eventDispatcher.sendEvent(strategy.getName(), cashBalance);
            }
        }

        return transactionResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logFillSummary(final List<Fill> fills) {

        if (fills.size() > 0 && !this.commonConfig.isSimulation()) {

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
            if (MAIL_LOGGER.isInfoEnabled()) {
                MAIL_LOGGER.info("executed transaction: {},{},{},avgPrice={},{},strategy={}",
                        fill.getSide(),
                        totalQuantity,
                        order.getSecurity(),
                        RoundUtil.getBigDecimal(totalPrice / totalQuantity, securityFamily.getScale()),
                        securityFamily.getCurrency(),
                        order.getStrategy());
            }
            //@formatter:on
        }

    }

    @Override
    public void logFillSummary(final Map<?, ?>[] insertStream, Map<?, ?>[] removeStream) {

        List<Fill> fills = new ArrayList<>();
        for (Map<?, ?> element : insertStream) {
            Fill fill = (Fill) element.get("fill");
            fills.add(fill);
        }
        logFillSummary(fills);
    }

    private void processTransaction(final Transaction transaction) {

        if (!this.coreConfig.isPositionCheckDisabled()) {

            this.transactionPersistenceService.ensurePositionAndCashBalance(transaction);
        }
        TransactionResultVO transactionResult = this.transactionPersistenceService.saveTransaction(transaction);
        handleTransactionResult(transaction, transactionResult);

        // propagate the transaction to the corresponding strategy and AlgoTrader Server
        Strategy strategy = transaction.getStrategy();
        TransactionVO transactionEvent = transaction.convertToVO();
        this.eventDispatcher.sendEvent(strategy.getName(), transactionEvent);
        this.eventDispatcher.broadcast(transactionEvent, EventRecipient.SERVER_LISTENERS);

        // we need the transaction entity inside the server engine to create OrderComplitionVO
        this.serverEngine.sendEvent(transaction);

        // propagate the positionMutation to the corresponding strategy
        PositionVO positionMutation = transactionResult.getPositionMutation();
        if (positionMutation != null) {
            this.eventDispatcher.sendEvent(strategy.getName(), positionMutation);
            this.eventDispatcher.broadcast(positionMutation, EventRecipient.SERVER_LISTENERS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String resetCashBalances() {

        return this.transactionPersistenceService.resetCashBalances();
    }

    private void printTransaction(final Transaction transaction, final TradePerformanceVO tradePerformance) {

        if (!this.commonConfig.isDisableReports()) {
            synchronized(this) {
                try {
                    if (this.tradeReport == null) {
                        this.tradeReport = TradeReport.create();
                    }
                    this.tradeReport.write(transaction, tradePerformance);
                } catch (IOException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }
    }

}
