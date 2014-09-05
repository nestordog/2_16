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
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.CashBalanceDao;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.collection.BigDecimalMap;
import ch.algotrader.util.collection.Pair;
import ch.algotrader.util.spring.HibernateSession;
import ch.algotrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class CashBalanceServiceImpl implements CashBalanceService {

    private static Logger logger = MyLogger.getLogger(CashBalanceServiceImpl.class.getName());

    private final CommonConfig commonConfig;

    private final CashBalanceDao cashBalanceDao;

    private final StrategyDao strategyDao;

    private final TransactionDao transactionDao;

    public CashBalanceServiceImpl(final CommonConfig commonConfig,
            final CashBalanceDao cashBalanceDao,
            final StrategyDao strategyDao,
            final TransactionDao transactionDao) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(cashBalanceDao, "CashBalanceDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(transactionDao, "TransactionDao is null");

        this.commonConfig = commonConfig;
        this.cashBalanceDao = cashBalanceDao;
        this.strategyDao = strategyDao;
        this.transactionDao = transactionDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void processTransaction(final Transaction transaction) {

        Validate.notNull(transaction, "Transaction is null");

        try {
            // process all currenyAmounts
            for (CurrencyAmountVO currencyAmount : transaction.getAttributions()) {
                processAmount(transaction.getStrategy().getName(), currencyAmount);
            }
        } catch (Exception ex) {
            throw new CashBalanceServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void processAmount(final String strategyName, final CurrencyAmountVO amount) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(amount, "Amount is null");
        Validate.notNull(amount.getCurrency(), "Currency is null");
        Validate.notNull(amount.getAmount(), "Amount is null");

        try {
            Strategy strategy = this.strategyDao.findByName(strategyName);
            CashBalance cashBalance;
            if (this.commonConfig.isSimulation()) {
                cashBalance = this.cashBalanceDao.findByStrategyAndCurrency(strategy, amount.getCurrency());
            } else {
                cashBalance = this.cashBalanceDao.findByStrategyAndCurrencyLocked(strategy, amount.getCurrency());
            }

            // create the cashBalance, if it does not exist yet
            if (cashBalance == null) {

                cashBalance = CashBalance.Factory.newInstance();

                // associate currency, amount and strategy
                cashBalance.setCurrency(amount.getCurrency());
                cashBalance.setAmount(amount.getAmount());
                cashBalance.setStrategy(strategy);

                this.cashBalanceDao.create(cashBalance);

                // reverse-associate strategy (after cashBalance has received an id)
                strategy.getCashBalances().add(cashBalance);

            } else {

                cashBalance.setAmount(cashBalance.getAmount().add(amount.getAmount()));
            }
        } catch (Exception ex) {
            throw new CashBalanceServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String resetCashBalances() {

        try {
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

                CashBalance cashBalance;
                if (this.commonConfig.isSimulation()) {
                    cashBalance = this.cashBalanceDao.findByStrategyAndCurrency(strategy, currency);
                } else {
                    cashBalance = this.cashBalanceDao.findByStrategyAndCurrencyLocked(strategy, currency);
                }

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
        } catch (Exception ex) {
            throw new CashBalanceServiceException(ex.getMessage(), ex);
        }
    }

}
