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
package com.algoTrader.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.collections15.keyvalue.MultiKey;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronSequenceGenerator;

import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.entity.strategy.Strategy;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class PortfolioPersistenceServiceImpl extends PortfolioPersistenceServiceBase {

    private static Logger logger = MyLogger.getLogger(PortfolioPersistenceServiceImpl.class.getName());

    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private @Value("${misc.rebalanceMinAmount}") double rebalanceMinAmount;
    private @Value("${simulation}") boolean simulation;

    @Override
    protected void handleRebalancePortfolio() throws Exception {

        Strategy base = getStrategyDao().findBase();
        Collection<Strategy> strategies = getStrategyDao().loadAll();
        double portfolioNetLiqValue = getPortfolioService().getNetLiqValueDouble();

        double totalAllocation = 0.0;
        double totalRebalanceAmount = 0.0;
        Collection<Transaction> transactions = new ArrayList<Transaction>();
        for (Strategy strategy : strategies) {

            totalAllocation += strategy.getAllocation();

            if (strategy.isBase()) {
                continue;
            }

            double actualNetLiqValue = MathUtils.round(getPortfolioService().getNetLiqValueDouble(strategy.getName()), 2);
            double targetNetLiqValue = MathUtils.round(portfolioNetLiqValue * strategy.getAllocation(), 2);
            double rebalanceAmount = targetNetLiqValue - actualNetLiqValue;

            if (Math.abs(rebalanceAmount) >= this.rebalanceMinAmount) {

                totalRebalanceAmount += rebalanceAmount;

                Transaction transaction = new TransactionImpl();
                transaction.setDateTime(DateUtil.getCurrentEPTime());
                transaction.setQuantity(targetNetLiqValue > actualNetLiqValue ? +1 : -1);
                transaction.setPrice(RoundUtil.getBigDecimal(Math.abs(rebalanceAmount)));
                transaction.setCurrency(this.portfolioBaseCurrency);
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

            Transaction transaction = new TransactionImpl();
            transaction.setDateTime(DateUtil.getCurrentEPTime());
            transaction.setQuantity((int) Math.signum(-1.0 * totalRebalanceAmount));
            transaction.setPrice(RoundUtil.getBigDecimal(Math.abs(totalRebalanceAmount)));
            transaction.setCurrency(this.portfolioBaseCurrency);
            transaction.setType(TransactionType.REBALANCE);
            transaction.setStrategy(base);

            transactions.add(transaction);

        } else {

            logger.info("no rebalancing is performed because all rebalancing amounts are below min amount " + this.rebalanceMinAmount);
        }

        for (Transaction transaction : transactions) {

            getTransactionService().persistTransaction(transaction);
        }
    }

    @Override
    protected void handleSavePortfolioValues() throws Exception {

        for (Strategy strategy : getStrategyDao().findAutoActivateStrategies()) {

            PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(strategy.getName());

            // truncate Date to hour
            portfolioValue.setDateTime(DateUtils.truncate(portfolioValue.getDateTime(), Calendar.HOUR));

            getPortfolioValueDao().create(portfolioValue);
        }
    }

    @Override
    protected void handleSavePortfolioValue(Transaction transaction) throws Exception {

        // do not save PortfolioValue in simulation
        if (this.simulation) {
            return;
        }

        Date date = transaction.getDateTime();

        // only process performanceRelevant transactions
        if (transaction.isPerformanceRelevant()) {

            // create and save the portfolio value
            PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(transaction.getStrategy().getName());

            portfolioValue.setCashFlow(transaction.getGrossValue());

            getPortfolioValueDao().create(portfolioValue);

            // use the date of the portfolioValue as it might be slightly after the transaction date
            date = portfolioValue.getDateTime();
        }

        // if there have been PortfolioValues created since this transaction, they will need to be recreated (including PortfolioValues of Base)
        List<PortfolioValue> portfolioValues = getPortfolioValueDao().findByStrategyOrBaseAndMinDate(transaction.getStrategy().getName(), date);

        if (portfolioValues.size() > 0) {

            getPortfolioValueDao().remove(portfolioValues);

            restorePortfolioValues(transaction.getStrategy(), transaction.getDateTime(), new Date());

            if (!transaction.getStrategy().isBase()) {

                Strategy strategy = getStrategyDao().findBase();
                restorePortfolioValues(strategy, transaction.getDateTime(), new Date());
            }
        }
    }

    @Override
    protected void handleRestorePortfolioValues(Strategy strategy, Date fromDate, Date toDate) throws Exception {

        // same cron as SAVE_PORTFOLIO_VALUE (but one hour behind to get 14 - 24)
        CronSequenceGenerator cron = new CronSequenceGenerator("0 0 13-23 * * 1-5", TimeZone.getDefault());

        // adjust fromDate and toDate by one one or to be inline with above
        fromDate = DateUtils.addHours(fromDate, -1);
        toDate = DateUtils.addHours(toDate, -1);

        // group PortfolioValues by strategyId and date
        Map<MultiKey<Long>, PortfolioValue> portfolioValueMap = new HashMap<MultiKey<Long>, PortfolioValue>();

        // create portfolioValues for all cron time slots
        Date date = cron.next(fromDate);
        while (date.compareTo(toDate) <= 0) {

            Date actualDate = DateUtils.addHours(date, 1); // to get 14 - 24

            PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(strategy.getName(), actualDate);
            if (portfolioValue.getNetLiqValueDouble() == 0) {
                date = cron.next(date);
                continue;
            } else {
                MultiKey<Long> key = new MultiKey<Long>((long) strategy.getId(), actualDate.getTime());
                portfolioValueMap.put(key, portfolioValue);

                logger.info("processed portfolioValue for " + strategy.getName() + " " + actualDate);

                date = cron.next(date);
            }
        }

        // save values for all cashFlows
        List<Transaction> transactions = getTransactionDao().findCashflowsByStrategyAndMinDate(strategy.getName(), fromDate);
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
            MultiKey<Long> key = new MultiKey<Long>((long)transaction.getStrategy().getId(), transaction.getDateTime().getTime());
            if (portfolioValueMap.containsKey(key)) {
                PortfolioValue portfolioValue = portfolioValueMap.get(key);
                if (portfolioValue.getCashFlow() != null) {
                    portfolioValue.setCashFlow(portfolioValue.getCashFlow().add(transaction.getGrossValue()));
                } else {
                    portfolioValue.setCashFlow(transaction.getGrossValue());
                }
            } else {
                PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(transaction.getStrategy().getName(), transaction.getDateTime());
                portfolioValue.setCashFlow(transaction.getGrossValue());
                portfolioValueMap.put(key, portfolioValue);
            }

            logger.info("processed portfolioValue for " + transaction.getStrategy().getName() + " " + transaction.getDateTime() + " cashflow " + transaction.getGrossValue());
        }

        // perisist the PortfolioValues
        getPortfolioValueDao().create(portfolioValueMap.values());
    }
}
