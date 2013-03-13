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
import java.util.Iterator;
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
import com.algoTrader.entity.strategy.StrategyImpl;
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

    @Override
    protected void handleRebalancePortfolio() throws Exception {

        Strategy base = getStrategyDao().findByName(StrategyImpl.BASE);
        Collection<Strategy> strategies = getStrategyDao().loadAll();
        double portfolioNetLiqValue = getPortfolioService().getNetLiqValueDouble();

        double totalAllocation = 0.0;
        double totalRebalanceAmount = 0.0;
        Collection<Transaction> transactions = new ArrayList<Transaction>();
        for (Strategy strategy : strategies) {

            totalAllocation += strategy.getAllocation();

            if (StrategyImpl.BASE.equals(strategy.getName())) {
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
    protected void handleSavePortfolioValue(Strategy strategy, Transaction transaction) throws Exception {

        // trades do not affect netLiqValue / performance so no portfolioValues are saved
        if (transaction.isTrade()) {
            return;

        // do not save a portfolioValue for BASE when rebalancing
        } else if (TransactionType.REBALANCE.equals(transaction.getType()) && strategy.isBase()) {
            return;
        }

        PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(strategy.getName());

        portfolioValue.setCashFlow(transaction.getGrossValue());

        getPortfolioValueDao().create(portfolioValue);
    }

    @Override
    protected void handleRestorePortfolioValues(Date fromDate, Date toDate) throws Exception {

        // same cron as SAVE_PORTFOLIO_VALUE
        CronSequenceGenerator cron = new CronSequenceGenerator("0 0 13-23 * * 1-5", TimeZone.getDefault());

        // group PortfolioValues by strategyId and date
        Map<MultiKey<Long>, PortfolioValue> portfolioValueMap = new HashMap<MultiKey<Long>, PortfolioValue>();

        // save values for all autoActiveStrategies
        List<Strategy> strategies = getStrategyDao().findAutoActivateStrategies();

        // create portfolioValues for all cron time slots
        Date date = fromDate;
        while (date.compareTo(toDate) <= 0) {

            date = cron.next(date);
            Date actualDate = DateUtils.addHours(date, 1); // to get 14 - 24
            for (Strategy strategy : strategies) {

                PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(strategy.getName(), actualDate);
                MultiKey<Long> key = new MultiKey<Long>((long)strategy.getId(), actualDate.getTime());
                portfolioValueMap.put(key, portfolioValue);

                logger.info("processed portfolioValue for " + strategy.getName() + " " + actualDate);
            }
        }

        // save values for all cashFlows
        List<Transaction> transactions = getTransactionDao().findAllCashflows();
        for (Transaction transaction : transactions) {

            // only consider autoActivate strategies
            if (!strategies.contains(transaction.getStrategy())) {
                continue;
            }

            // for BASE only save PortfolioValue for CREDIT and DEBIT (REBALANCE do not affect NetLiqValue and FEES, REFUND etc. are part of the performance)
            if (transaction.getStrategy().isBase()) {
                if(!TransactionType.CREDIT.equals(transaction.getType()) && !TransactionType.DEBIT.equals(transaction.getType())) {
                    continue;
                }

            // for strategies only save PortfolioValue for REBALANCE
            } else {
                if(!TransactionType.REBALANCE.equals(transaction.getType())) {
                    continue;
                }
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

        // netLiqValue might simply be zero because the strategy is not active yet
        for (Iterator<PortfolioValue> it = portfolioValueMap.values().iterator(); it.hasNext();) {
            PortfolioValue portfolioValue = it.next();
            if (portfolioValue.getNetLiqValueDouble() == 0) {
                it.remove();
            }
        }

        getPortfolioValueDao().create(portfolioValueMap.values());
    }
}
