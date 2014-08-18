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
import org.springframework.scheduling.support.CronSequenceGenerator;

import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioPersistenceServiceImpl extends PortfolioPersistenceServiceBase {

    private static Logger logger = MyLogger.getLogger(PortfolioPersistenceServiceImpl.class.getName());

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

            if (Math.abs(rebalanceAmount) >= getCoreConfig().getRebalanceMinAmount().doubleValue()) {

                totalRebalanceAmount += rebalanceAmount;

                Transaction transaction = Transaction.Factory.newInstance();
                transaction.setDateTime(DateUtil.getCurrentEPTime());
                transaction.setQuantity(targetNetLiqValue > actualNetLiqValue ? +1 : -1);
                transaction.setPrice(RoundUtil.getBigDecimal(Math.abs(rebalanceAmount)));
                transaction.setCurrency(getCommonConfig().getPortfolioBaseCurrency());
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
            transaction.setCurrency(getCommonConfig().getPortfolioBaseCurrency());
            transaction.setType(TransactionType.REBALANCE);
            transaction.setStrategy(base);

            transactions.add(transaction);

        } else {

            logger.info("no rebalancing is performed because all rebalancing amounts are below min amount " + getCoreConfig().getRebalanceMinAmount());
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
        if (getCommonConfig().isSimulation()) {
            return;
        }

        // only process performanceRelevant transactions
        if (transaction.isPerformanceRelevant()) {

            // check if there is an existing portfolio value
            Collection<PortfolioValue> portfolioValues = getPortfolioValueDao().findByStrategyAndMinDate(transaction.getStrategy().getName(), transaction.getDateTime());

            if (portfolioValues.size() > 0) {

                logger.warn("transaction date is in the past, please restore portfolio values");

            } else {

                // create and save the portfolio value
                PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(transaction.getStrategy().getName());

                portfolioValue.setCashFlow(transaction.getGrossValue());

                getPortfolioValueDao().create(portfolioValue);
            }
        }
    }

    @Override
    protected void handleRestorePortfolioValues(Strategy strategy, Date fromDate, Date toDate) throws Exception {

        // delete existing portfolio values;
        List<PortfolioValue> portfolioValues = getPortfolioValueDao().findByStrategyAndMinDate(strategy.getName(), fromDate);

        if (portfolioValues.size() > 0) {

            getPortfolioValueDao().remove(portfolioValues);

            // need to flush since new portfoliovalues will be created with same date and strategy
            getSessionFactory().getCurrentSession().flush();
        }

        // init cron
        CronSequenceGenerator cron = new CronSequenceGenerator("0 0 * * * 1-5", TimeZone.getDefault());

        // group PortfolioValues by strategyId and date
        Map<MultiKey<Long>, PortfolioValue> portfolioValueMap = new HashMap<MultiKey<Long>, PortfolioValue>();

        // create portfolioValues for all cron time slots
        Date date = cron.next(DateUtils.addHours(fromDate, -1));
        while (date.compareTo(toDate) <= 0) {

            PortfolioValue portfolioValue = getPortfolioService().getPortfolioValue(strategy.getName(), date);
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
