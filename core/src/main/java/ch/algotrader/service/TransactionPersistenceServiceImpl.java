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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.accounting.PositionTrackerImpl;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.HibernateInitializer;
import ch.algotrader.dao.PositionDao;
import ch.algotrader.dao.TransactionDao;
import ch.algotrader.dao.strategy.CashBalanceDao;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionVO;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.CashBalanceVO;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.util.collection.BigDecimalMap;
import ch.algotrader.util.collection.Pair;
import ch.algotrader.vo.CurrencyAmountVO;
import ch.algotrader.vo.TradePerformanceVO;
import ch.algotrader.vo.TransactionResultVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public abstract class TransactionPersistenceServiceImpl implements TransactionPersistenceService {

    private static final Logger LOGGER = LogManager.getLogger(TransactionPersistenceServiceImpl.class);

    private final CommonConfig commonConfig;

    private final PortfolioService portfolioService;

    private final PositionDao positionDao;

    private final TransactionDao transactionDao;

    private final CashBalanceDao cashBalanceDao;

    public TransactionPersistenceServiceImpl(
            final CommonConfig commonConfig,
            final PortfolioService portfolioService,
            final PositionDao positionDao,
            final TransactionDao transactionDao,
            final CashBalanceDao cashBalanceDao) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(positionDao, "PositionDao is null");
        Validate.notNull(transactionDao, "TransactionDao is null");
        Validate.notNull(cashBalanceDao, "CashBalanceDao is null");

        this.commonConfig = commonConfig;
        this.portfolioService = portfolioService;
        this.positionDao = positionDao;
        this.transactionDao = transactionDao;
        this.cashBalanceDao = cashBalanceDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void ensurePositionAndCashBalance(final Transaction transaction);

    /**
     * {@inheritDoc}
     */
    @Override
    @Retryable(maxAttempts = 5, value = {LockAcquisitionException.class, CannotAcquireLockException.class})
    @Transactional(propagation = Propagation.REQUIRED)
    public TransactionResultVO saveTransaction(final Transaction transaction) {

        Validate.notNull(transaction, "Transaction is null");

        PositionVO positionMutation = null;
        TradePerformanceVO tradePerformance = null;

        // position handling (incl ClosePositionVO and TradePerformanceVO)
        Strategy strategy = transaction.getStrategy();
        Security security = transaction.getSecurity();
        if (security != null) {

            Position position;
            if (this.commonConfig.isSimulation()) {
                position = this.positionDao.findBySecurityAndStrategy(security.getId(), strategy.getName());
            } else {
                position = this.positionDao.findBySecurityAndStrategyIdLocked(security.getId(), strategy.getId());
            }
            if (position == null) {
                throw new ServiceException("Position for strategy " + strategy.getName() +
                        " / security " + security.getId() + " not found ", null);
            }

            // process the transaction (adjust quantity, cost and realizedPL)
            tradePerformance = PositionTrackerImpl.INSTANCE.processTransaction(position, transaction);

            // associate the position
            transaction.setPosition(position);

            // if no position was open before initialize the openPosition event
            positionMutation = position.convertToVO();
        }

        transaction.initializeSecurity(HibernateInitializer.INSTANCE);

        Collection<CurrencyAmountVO> attributions = transaction.getAttributions();
        List<CashBalanceVO> cashBalances = new ArrayList<>(attributions.size());
        // add the amount to the corresponding cashBalance
        for (CurrencyAmountVO amount : attributions) {

            Currency currency = amount.getCurrency();
            CashBalance cashBalance;
            if (this.commonConfig.isSimulation()) {
                cashBalance = this.cashBalanceDao.findByStrategyAndCurrency(strategy, currency);
            } else {
                cashBalance = this.cashBalanceDao.findByStrategyAndCurrencyLocked(strategy, currency);
            }
            if (cashBalance == null) {
                throw new ServiceException("Cash balance for strategy " + strategy.getName() +
                        " / currency " + currency + " not found ", null);
            }
            cashBalance.setAmount(cashBalance.getAmount().add(amount.getAmount()));
            cashBalances.add(cashBalance.convertToVO());
        }

        // save a portfolioValue (if necessary)
        this.portfolioService.savePortfolioValue(transaction);

        // create the transaction
        this.transactionDao.save(transaction);

        return new TransactionResultVO(positionMutation, tradePerformance, cashBalances);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveTransactions(final Collection<Transaction> transactions) {

        Validate.notNull(transactions, "Transaction list is null");

        for (Transaction transaction : transactions) {

            saveTransaction(transaction);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String resetCashBalances() {

        // get all existing cashBalances
        Collection<CashBalance> existingCashBalances = this.cashBalanceDao.loadAll();

        // sum all transactions
        Collection<Transaction> transactions = this.transactionDao.loadAll();
        BigDecimalMap<Pair<Strategy, Currency>> map = new BigDecimalMap<>();
        for (Transaction transaction : transactions) {

            transaction.initializeSecurity(HibernateInitializer.INSTANCE);

            // process all currenyAmounts
            for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
                map.increment(new Pair<>(transaction.getStrategy(), currencyAmount.getCurrency()), currencyAmount.getAmount());
            }
        }

        // create cash balances
        StringBuilder buffer = new StringBuilder();
        for (Map.Entry<Pair<Strategy, Currency>, BigDecimal> entry : map.entrySet()) {

            Strategy strategy = entry.getKey().getFirst();
            Currency currency = entry.getKey().getSecond();
            BigDecimal amount = entry.getValue().setScale(this.commonConfig.getPortfolioDigits(), BigDecimal.ROUND_HALF_UP);

            CashBalance cashBalance = this.cashBalanceDao.findByStrategyAndCurrency(strategy, currency);
            if (cashBalance != null) {

                existingCashBalances.remove(cashBalance);

                BigDecimal oldAmount = cashBalance.getAmount();
                if (oldAmount.doubleValue() != amount.doubleValue()) {

                    cashBalance.setAmount(amount);

                    String info = "adjusted cashBalance " + cashBalance + " from " + oldAmount;
                    LOGGER.info(info);
                    buffer.append(info + "\n");

                }

            } else {

                cashBalance = CashBalance.Factory.newInstance();
                cashBalance.setCurrency(currency);
                cashBalance.setAmount(amount);
                cashBalance.setStrategy(strategy);

                this.cashBalanceDao.save(cashBalance);

                String info = "created cashBalance " + cashBalance;
                LOGGER.info(info);
                buffer.append(info + "\n");

            }
        }

        // remove all obsolete cashBalances
        for (CashBalance cashBalance : existingCashBalances) {

            Strategy strategy = cashBalance.getStrategy();

            String info = "removed cashBalance " + cashBalance;
            LOGGER.info(info);
            buffer.append(info + "\n");
        }

        this.cashBalanceDao.deleteAll(existingCashBalances);

        return buffer.toString();

    }

}
