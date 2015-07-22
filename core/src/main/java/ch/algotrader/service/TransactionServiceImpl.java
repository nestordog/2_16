/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.vo.PositionMutationVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LogManager.getLogger(TransactionServiceImpl.class);
    private static final Logger MAIL_LOGGER = LogManager.getLogger(TransactionServiceImpl.class.getName() + ".MAIL");

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final TransactionPersistenceService transactionPersistenceService;

    private final PortfolioService portfolioService;

    private final StrategyDao strategyDao;

    private final SecurityDao securityDao;

    private final AccountDao accountDao;

    private final EventDispatcher eventDispatcher;

    private final EngineManager engineManager;

    private final Engine serverEngine;

    public TransactionServiceImpl(
            final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final TransactionPersistenceService transactionPersistenceService,
            final PortfolioService portfolioService,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final EventDispatcher eventDispatcher,
            final EngineManager engineManager,
            final Engine serverEngine) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(transactionPersistenceService, "TransactionPersistenceService is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.transactionPersistenceService = transactionPersistenceService;
        this.portfolioService = portfolioService;
        this.strategyDao = strategyDao;
        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.eventDispatcher = eventDispatcher;
        this.engineManager = engineManager;
        this.serverEngine = serverEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void createTransaction(final Fill fill) {

        long startTime = System.nanoTime();
        LOGGER.debug("createTransaction start");

        Validate.notNull(fill, "Fill is null");

        Order order = fill.getOrder();
        Broker broker = order.getAccount().getBroker();

        // reload the strategy and security to get potential changes
        Strategy strategy = this.strategyDao.load(order.getStrategy().getId());
        Security security = this.securityDao.findByIdInclFamilyUnderlyingExchangeAndBrokerParameters(order.getSecurity().getId());

        // and update the strategy and security of the order
        order.setStrategy(strategy);
        order.setSecurity(security);

        SecurityFamily securityFamily = security.getSecurityFamily();
        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setUuid(UUID.randomUUID().toString());
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
        transaction.setAccount(order.getAccount());

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

        Account account = this.accountDao.findByName(accountName);

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

    // This method needs to be non-transaction in ensure correct creation of position and cash balance records in a separate transaction
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void persistTransaction(final Transaction transaction) {

        if (!this.coreConfig.isPositionCheckDisabled()) {

            this.transactionPersistenceService.ensurePositionAndCashBalance(transaction);
        }
        this.transactionPersistenceService.saveTransaction(transaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateFill(final Fill fill) {

        Validate.notNull(fill, "Fill is null");

        // send the fill to the strategy that placed the corresponding order
        if (!fill.getOrder().getStrategy().isServer()) {
            this.eventDispatcher.sendEvent(fill.getOrder().getStrategy().getName(), fill);
        }

        if (!this.commonConfig.isSimulation()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("received fill: {} for order: {}", fill, fill.getOrder());
            }
        }

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
        PositionMutationVO positionMutationEvent = this.transactionPersistenceService.saveTransaction(transaction);

        // propagate the positionMutationEvent to the corresponding strategy
        if (positionMutationEvent != null) {
            this.eventDispatcher.sendEvent(positionMutationEvent.getStrategy(), positionMutationEvent);
        }

        // propagate the transaction to the corresponding strategy and AlgoTrader Server
        if (!transaction.getStrategy().isServer()) {
            this.eventDispatcher.sendEvent(transaction.getStrategy().getName(), transaction);
        }

        this.serverEngine.sendEvent(transaction);
    }

    /**
     * {@inheritDoc}
     */
    // This method needs to be non-transaction in ensure correct creation of position and cash balance records in a separate transaction
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void rebalancePortfolio() {

        Strategy server = this.strategyDao.findServer();
        Collection<Strategy> strategies = this.strategyDao.loadAll();
        double portfolioNetLiqValue = this.portfolioService.getNetLiqValueDouble();

        double totalAllocation = 0.0;
        double totalRebalanceAmount = 0.0;
        Collection<Transaction> transactions = new ArrayList<>();
        for (Strategy strategy : strategies) {

            totalAllocation += strategy.getAllocation();

            if (strategy.isServer()) {
                continue;
            }

            double actualNetLiqValue = MathUtils.round(this.portfolioService.getNetLiqValueDouble(strategy.getName()), 2);
            double targetNetLiqValue = MathUtils.round(portfolioNetLiqValue * strategy.getAllocation(), 2);
            double rebalanceAmount = targetNetLiqValue - actualNetLiqValue;

            if (Math.abs(rebalanceAmount) >= this.coreConfig.getRebalanceMinAmount().doubleValue()) {

                totalRebalanceAmount += rebalanceAmount;

                Transaction transaction = Transaction.Factory.newInstance();
                transaction.setUuid(UUID.randomUUID().toString());
                transaction.setDateTime(this.engineManager.getCurrentEPTime());
                transaction.setQuantity(targetNetLiqValue > actualNetLiqValue ? +1 : -1);
                transaction.setPrice(RoundUtil.getBigDecimal(Math.abs(rebalanceAmount)));
                transaction.setCurrency(this.commonConfig.getPortfolioBaseCurrency());
                transaction.setType(TransactionType.REBALANCE);
                transaction.setStrategy(strategy);

                transactions.add(transaction);
            }
        }

        // check allocations add up to 1.0
        if (MathUtils.round(totalAllocation, 2) != 1.0) {
            throw new IllegalStateException("the total of all allocations is: " + totalAllocation + " where it should be 1.0");
        }

        // add REBALANCE transaction to offset totalRebalanceAmount
        if (transactions.size() != 0) {

            Transaction transaction = Transaction.Factory.newInstance();
            transaction.setUuid(UUID.randomUUID().toString());
            transaction.setDateTime(this.engineManager.getCurrentEPTime());
            transaction.setQuantity((int) Math.signum(-1.0 * totalRebalanceAmount));
            transaction.setPrice(RoundUtil.getBigDecimal(Math.abs(totalRebalanceAmount)));
            transaction.setCurrency(this.commonConfig.getPortfolioBaseCurrency());
            transaction.setType(TransactionType.REBALANCE);
            transaction.setStrategy(server);

            transactions.add(transaction);

        } else {

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("no rebalancing is performed because all rebalancing amounts are below min amount {}", this.coreConfig.getRebalanceMinAmount());
            }
        }

        if (!this.coreConfig.isPositionCheckDisabled()) {

            for (Transaction transaction : transactions) {

                this.transactionPersistenceService.ensurePositionAndCashBalance(transaction);
            }
        }

        this.transactionPersistenceService.saveTransactions(transactions);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String resetCashBalances() {

        return this.transactionPersistenceService.resetCashBalances();
    }

}
