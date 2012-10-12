package com.algoTrader.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.strategy.PortfolioValue;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

public abstract class AccountServiceImpl extends AccountServiceBase {

    private static Logger logger = MyLogger.getLogger(AccountServiceImpl.class.getName());

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

            internalSavePortfolioValue(portfolioValue);
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

        internalSavePortfolioValue(portfolioValue);
    }

    private void internalSavePortfolioValue(PortfolioValue portfolioValue) {

        // netLiqValue and securitiesCurrentValue might be null if there is a security without a last tick
        if (portfolioValue.getSecuritiesCurrentValue() != null && portfolioValue.getNetLiqValue() != null) {
            getPortfolioValueDao().create(portfolioValue);
        }
    }
}
