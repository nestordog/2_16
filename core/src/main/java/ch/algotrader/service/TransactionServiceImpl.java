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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.ServiceLocator;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountDao;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.PortfolioValueDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.esper.subscriber.Subscriber;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.PositionUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.util.metric.MetricsUtil;
import ch.algotrader.util.spring.HibernateSession;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.OpenPositionVO;
import ch.algotrader.vo.PositionMutationVO;
import ch.algotrader.vo.TradePerformanceVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class TransactionServiceImpl implements TransactionService {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());
    private static Logger mailLogger = MyLogger.getLogger(TransactionServiceImpl.class.getName() + ".MAIL");
    private static Logger simulationLogger = MyLogger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final CashBalanceService cashBalanceService;

    private final PortfolioService portfolioService;

    private final SessionFactory sessionFactory;

    private final PositionDao positionDao;

    private final TransactionDao transactionDao;

    private final StrategyDao strategyDao;

    private final SecurityDao securityDao;

    private final AccountDao accountDao;

    private final PortfolioValueDao portfolioValueDao;

    public TransactionServiceImpl(
            final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final CashBalanceService cashBalanceService,
            final PortfolioService portfolioService,
            final SessionFactory sessionFactory,
            final PositionDao positionDao,
            final TransactionDao transactionDao,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final PortfolioValueDao portfolioValueDao) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(cashBalanceService, "CashBalanceService is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(positionDao, "PositionDao is null");
        Validate.notNull(transactionDao, "TransactionDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(portfolioValueDao, "PortfolioValueDao is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.cashBalanceService = cashBalanceService;
        this.portfolioService = portfolioService;
        this.sessionFactory = sessionFactory;
        this.positionDao = positionDao;
        this.transactionDao = transactionDao;
        this.strategyDao = strategyDao;
        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.portfolioValueDao = portfolioValueDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTransaction(final Fill fill) {

        Validate.notNull(fill, "Fill is null");

        try {
        Order order = fill.getOrder();
        Broker broker = order.getAccount().getBroker();

        // reload the strategy and security to get potential changes
            Strategy strategy = this.strategyDao.load(order.getStrategy().getId());
            Security security = this.securityDao.load(order.getSecurity().getId());

        // and update the strategy and security of the order
        order.setStrategy(strategy);
        order.setSecurity(security);

        SecurityFamily securityFamily = security.getSecurityFamily();
        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();

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
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createTransaction(final int securityId, final String strategyName, final String extId, final Date dateTime, final long quantity, final BigDecimal price,
            final BigDecimal executionCommission, final BigDecimal clearingCommission, final BigDecimal fee, final Currency currency, final TransactionType transactionType, final String accountName,
            final String description) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(dateTime, "Date time is null");
        Validate.notNull(price, "Price is null");
        Validate.notNull(transactionType, "Transaction type is null");

        try {
            Currency currencyNonFinal = currency;
            long quantityNonFinal = quantity;
        // validations
            Strategy strategy = this.strategyDao.findByName(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("strategy " + strategyName + " was not found");
        }

            int scale = this.commonConfig.getPortfolioDigits();
            Security security = this.securityDao.findById(securityId);
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
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
    }
            }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public PositionMutationVO persistTransaction(final Transaction transaction) {

        Validate.notNull(transaction, "Transaction is null");

        try {
        OpenPositionVO openPositionVO = null;
        ClosePositionVO closePositionVO = null;
        TradePerformanceVO tradePerformance = null;

        // position handling (incl ClosePositionVO and TradePerformanceVO)
        if (transaction.getSecurity() != null) {

            // create a new position if necessary
            boolean existingOpenPosition = false;
            Position position;
                if (this.commonConfig.isSimulation()) {
                    position = this.positionDao.findBySecurityAndStrategyIdLocked(transaction.getSecurity().getId(), transaction.getStrategy().getId());
            } else {
                    position = this.positionDao.findBySecurityAndStrategy(transaction.getSecurity().getId(), transaction.getStrategy().getName());
            }

            if (position == null) {

                position = PositionUtil.processFirstTransaction(transaction);

                // associate strategy
                position.setStrategy(transaction.getStrategy());

                    this.positionDao.create(position);

                // associate reverse-relations (after position has received an id)
                transaction.setPosition(position);
                transaction.getSecurity().addPositions(position);

            } else {

                existingOpenPosition = position.isOpen();

                // get the closePositionVO (must be done before closing the position)
                    closePositionVO = this.positionDao.toClosePositionVO(position);

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
                    openPositionVO = this.positionDao.toOpenPositionVO(position);
            }
        }

        // add the amount to the corresponding cashBalance
            this.cashBalanceService.processTransaction(transaction);

        // save a portfolioValue (if necessary)
            savePortfolioValue(transaction);

        // create the transaction
            this.transactionDao.create(transaction);

            CommonConfig commonConfig = this.commonConfig;

        // prepare log message and propagate tradePerformance
        String logMessage = "executed transaction: " + transaction;
        if (tradePerformance != null && tradePerformance.getProfit() != 0.0) {

            logMessage += ",profit=" + RoundUtil.getBigDecimal(tradePerformance.getProfit()) + ",profitPct=" + RoundUtil.getBigDecimal(tradePerformance.getProfitPct());

            // propagate the TradePerformance event
            if (commonConfig.isSimulation() && EngineLocator.instance().hasBaseEngine()) {
                EngineLocator.instance().getBaseEngine().sendEvent(tradePerformance);
            }
        }

        if (commonConfig.isSimulation() && commonConfig.isSimulationLogTransactions()) {
            simulationLogger.info(logMessage);
        } else {
            logger.info(logMessage);
        }

        // return PositionMutation event if existent
        return openPositionVO != null ? openPositionVO : closePositionVO != null ? closePositionVO : null;
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
    }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateFill(final Fill fill) {

        Validate.notNull(fill, "Fill is null");

        try {
        // send the fill to the strategy that placed the corresponding order
        if (!fill.getOrder().getStrategy().isBase()) {
            EngineLocator.instance().sendEvent(fill.getOrder().getStrategy().getName(), fill);
        }

            if (!this.commonConfig.isSimulation()) {
            logger.info("received fill: " + fill + " for order: " + fill.getOrder());
        }
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logFillSummary(final List<Fill> fills) {

        try {
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
            mailLogger.info("executed transaction: " +
                    fill.getSide() +
                    "," + totalQuantity +
                    "," + order.getSecurity() +
                    ",avgPrice=" + RoundUtil.getBigDecimal(totalPrice / totalQuantity, securityFamily.getScale()) +
                    "," + securityFamily.getCurrency() +
                    ",strategy=" + order.getStrategy());
            //@formatter:on
        }
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
        }
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void rebalancePortfolio() {

        try {
            Strategy base = this.strategyDao.findBase();
            Collection<Strategy> strategies = this.strategyDao.loadAll();
            double portfolioNetLiqValue = this.portfolioService.getNetLiqValueDouble();

            double totalAllocation = 0.0;
            double totalRebalanceAmount = 0.0;
            Collection<Transaction> transactions = new ArrayList<Transaction>();
            for (Strategy strategy : strategies) {

                totalAllocation += strategy.getAllocation();

                if (strategy.isBase()) {
                    continue;
                }

                double actualNetLiqValue = MathUtils.round(this.portfolioService.getNetLiqValueDouble(strategy.getName()), 2);
                double targetNetLiqValue = MathUtils.round(portfolioNetLiqValue * strategy.getAllocation(), 2);
                double rebalanceAmount = targetNetLiqValue - actualNetLiqValue;

                if (Math.abs(rebalanceAmount) >= this.coreConfig.getRebalanceMinAmount().doubleValue()) {

                    totalRebalanceAmount += rebalanceAmount;

                    Transaction transaction = Transaction.Factory.newInstance();
                    transaction.setDateTime(DateUtil.getCurrentEPTime());
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

            // add BASE REBALANCE transaction to offset totalRebalanceAmount
            if (transactions.size() != 0) {

                Transaction transaction = Transaction.Factory.newInstance();
                transaction.setDateTime(DateUtil.getCurrentEPTime());
                transaction.setQuantity((int) Math.signum(-1.0 * totalRebalanceAmount));
                transaction.setPrice(RoundUtil.getBigDecimal(Math.abs(totalRebalanceAmount)));
                transaction.setCurrency(this.commonConfig.getPortfolioBaseCurrency());
                transaction.setType(TransactionType.REBALANCE);
                transaction.setStrategy(base);

                transactions.add(transaction);

            } else {

                logger.info("no rebalancing is performed because all rebalancing amounts are below min amount " + this.coreConfig.getRebalanceMinAmount());
            }

            for (Transaction transaction : transactions) {

                persistTransaction(transaction);
            }
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void savePortfolioValues() {

        try {
            for (Strategy strategy : this.strategyDao.findAutoActivateStrategies()) {

                PortfolioValue portfolioValue = this.portfolioService.getPortfolioValue(strategy.getName());

                // truncate Date to hour
                portfolioValue.setDateTime(DateUtils.truncate(portfolioValue.getDateTime(), Calendar.HOUR));

                this.portfolioValueDao.create(portfolioValue);
            }
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void savePortfolioValue(final Transaction transaction) {

        Validate.notNull(transaction, "Transaction is null");

        try {
            // do not save PortfolioValue in simulation
            if (this.commonConfig.isSimulation()) {
                return;
            }

            // only process performanceRelevant transactions
            if (transaction.isPerformanceRelevant()) {

                // check if there is an existing portfolio value
                Collection<PortfolioValue> portfolioValues = this.portfolioValueDao.findByStrategyAndMinDate(transaction.getStrategy().getName(), transaction.getDateTime());

                if (portfolioValues.size() > 0) {

                    logger.warn("transaction date is in the past, please restore portfolio values");

                } else {

                    // create and save the portfolio value
                    PortfolioValue portfolioValue = this.portfolioService.getPortfolioValue(transaction.getStrategy().getName());

                    portfolioValue.setCashFlow(transaction.getGrossValue());

                    this.portfolioValueDao.create(portfolioValue);
                }
            }
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void restorePortfolioValues(final Strategy strategy, final Date fromDate, final Date toDate) {

        Validate.notNull(strategy, "Strategy is null");
        Validate.notNull(fromDate, "From date is null");
        Validate.notNull(toDate, "To date is null");

        try {

            // delete existing portfolio values;
            List<PortfolioValue> portfolioValues = this.portfolioValueDao.findByStrategyAndMinDate(strategy.getName(), fromDate);

            if (portfolioValues.size() > 0) {

                this.portfolioValueDao.remove(portfolioValues);

                // need to flush since new portfoliovalues will be created with same date and strategy
                this.sessionFactory.getCurrentSession().flush();
            }

            // init cron
            CronSequenceGenerator cron = new CronSequenceGenerator("0 0 * * * 1-5", TimeZone.getDefault());

            // group PortfolioValues by strategyId and date
            Map<MultiKey<Long>, PortfolioValue> portfolioValueMap = new HashMap<MultiKey<Long>, PortfolioValue>();

            // create portfolioValues for all cron time slots
            Date date = cron.next(DateUtils.addHours(fromDate, -1));
            while (date.compareTo(toDate) <= 0) {

                PortfolioValue portfolioValue = this.portfolioService.getPortfolioValue(strategy.getName(), date);
                if (portfolioValue.getNetLiqValueDouble() == 0) {
                    date = cron.next(date);
                    continue;
                } else {
                    MultiKey<Long> key = new MultiKey<Long>((long) strategy.getId(), date.getTime());
                    portfolioValueMap.put(key, portfolioValue);

                    logger.info("processed portfolioValue for " + strategy.getName() + " " + date);

                    date = cron.next(date);
                }
            }

            // save values for all cashFlows
            List<Transaction> transactions = this.transactionDao.findCashflowsByStrategyAndMinDate(strategy.getName(), fromDate);
            for (Transaction transaction : transactions) {

                // only process performanceRelevant transactions
                if (!transaction.isPerformanceRelevant()) {
                    continue;
                }

                // do not save before fromDate
                if (transaction.getDateTime().compareTo(fromDate) < 0) {
                    continue;
                }

                // if there is an existing PortfolioValue, add the cashFlow
                MultiKey<Long> key = new MultiKey<Long>((long) transaction.getStrategy().getId(), transaction.getDateTime().getTime());
                if (portfolioValueMap.containsKey(key)) {
                    PortfolioValue portfolioValue = portfolioValueMap.get(key);
                    if (portfolioValue.getCashFlow() != null) {
                        portfolioValue.setCashFlow(portfolioValue.getCashFlow().add(transaction.getGrossValue()));
                    } else {
                        portfolioValue.setCashFlow(transaction.getGrossValue());
                    }
                } else {
                    PortfolioValue portfolioValue = this.portfolioService.getPortfolioValue(transaction.getStrategy().getName(), transaction.getDateTime());
                    portfolioValue.setCashFlow(transaction.getGrossValue());
                    portfolioValueMap.put(key, portfolioValue);
                }

                logger.info("processed portfolioValue for " + transaction.getStrategy().getName() + " " + transaction.getDateTime() + " cashflow " + transaction.getGrossValue());
            }

            // perisist the PortfolioValues
            this.portfolioValueDao.create(portfolioValueMap.values());
        } catch (Exception ex) {
            throw new TransactionServiceException(ex.getMessage(), ex);
        }
    }

}
