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
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.CashBalanceDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.PositionUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.BigDecimalMap;
import ch.algotrader.util.collection.Pair;
import ch.algotrader.util.spring.HibernateSession;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.CurrencyAmountVO;
import ch.algotrader.vo.OpenPositionVO;
import ch.algotrader.vo.PositionMutationVO;
import ch.algotrader.vo.TradePerformanceVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public abstract class TransactionPersistenceServiceImpl implements TransactionPersistenceService {

    private static Logger logger = MyLogger.getLogger(TransactionPersistenceServiceImpl.class.getName());
    private static Logger simulationLogger = MyLogger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");

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
    @Transactional(propagation = Propagation.REQUIRED)
    public PositionMutationVO saveTransaction(final Transaction transaction) {

        Validate.notNull(transaction, "Transaction is null");

        OpenPositionVO openPositionVO = null;
        ClosePositionVO closePositionVO = null;
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
                throw new TransactionServiceException("Position for strategy " + strategy.getName() +
                        " / security " + security.getId() + " not found ", null);
            }

            boolean existingOpenPosition = position.isOpen();

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

            // if no position was open before initialize the openPosition event
            if (!existingOpenPosition) {
                openPositionVO = this.positionDao.toOpenPositionVO(position);
            }
        }

        // add the amount to the corresponding cashBalance
        for (CurrencyAmountVO amount : transaction.getAttributions()) {

            Currency currency = amount.getCurrency();
            CashBalance cashBalance;
            if (this.commonConfig.isSimulation()) {
                cashBalance = this.cashBalanceDao.findByStrategyAndCurrency(strategy, currency);
            } else {
                cashBalance = this.cashBalanceDao.findByStrategyAndCurrencyLocked(strategy, currency);
            }
            if (cashBalance == null) {
                throw new TransactionServiceException("Cash balance for strategy " + strategy.getName() +
                        " / currency " + currency + " not found ", null);
            }
            cashBalance.setAmount(cashBalance.getAmount().add(amount.getAmount()));
        }

        // save a portfolioValue (if necessary)
        this.portfolioService.savePortfolioValue(transaction);

        // create the transaction
        this.transactionDao.create(transaction);

        // prepare log message and propagate tradePerformance
        String logMessage = "executed transaction: " + transaction;
        if (tradePerformance != null && tradePerformance.getProfit() != 0.0) {

            logMessage += ",profit=" + RoundUtil.getBigDecimal(tradePerformance.getProfit()) + ",profitPct=" + RoundUtil.getBigDecimal(tradePerformance.getProfitPct());

            // propagate the TradePerformance event
            if (this.commonConfig.isSimulation() && EngineLocator.instance().hasServerEngine()) {
                EngineLocator.instance().getServerEngine().sendEvent(tradePerformance);
            }
        }

        if (this.commonConfig.isSimulation() && this.commonConfig.isSimulationLogTransactions()) {
            simulationLogger.info(logMessage);
        } else {
            logger.info(logMessage);
        }

        // return PositionMutation event if existent
        return openPositionVO != null ? openPositionVO : closePositionVO != null ? closePositionVO : null;

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
        BigDecimalMap<Pair<Strategy, Currency>> map = new BigDecimalMap<Pair<Strategy, Currency>>();
        for (Transaction transaction : transactions) {

            // process all currenyAmounts
            for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
                map.increment(new Pair<Strategy, Currency>(transaction.getStrategy(), currencyAmount.getCurrency()), currencyAmount.getAmount());
            }
        }

        // create cash balances
        StringBuffer buffer = new StringBuffer();
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
                    logger.info(info);
                    buffer.append(info + "\n");

                }

            } else {

                cashBalance = CashBalance.Factory.newInstance();
                cashBalance.setCurrency(currency);
                cashBalance.setAmount(amount);
                cashBalance.setStrategy(strategy);

                this.cashBalanceDao.create(cashBalance);

                // reverse-associate with strategy (after cashBalance has received an id)
                strategy.getCashBalances().add(cashBalance);

                String info = "created cashBalance " + cashBalance;
                logger.info(info);
                buffer.append(info + "\n");

            }
        }

        // remove all obsolete cashBalances
        for (CashBalance cashBalance : existingCashBalances) {

            Strategy strategy = cashBalance.getStrategy();
            strategy.getCashBalances().remove(cashBalance);

            String info = "removed cashBalance " + cashBalance;
            logger.info(info);
            buffer.append(info + "\n");
        }

        this.cashBalanceDao.remove(existingCashBalances);

        return buffer.toString();

    }

}
