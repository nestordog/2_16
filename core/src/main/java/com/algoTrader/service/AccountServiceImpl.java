package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
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
    private @Value("${misc.rebalanceThreshold}") double rebalanceThreshold;

    @Override
    protected void handleRebalancePortfolio() throws Exception {

        double portfolioNetLiqValue = getPortfolioService().getNetLiqValueDouble();
        double totalAllocation = 0.0;
        double totalRebalanceAmount = 0.0;
        double maxRebalanceAmount = 0.0;
        Collection<Strategy> strategies = getStrategyDao().loadAll();
        Collection<Transaction> transactions = new ArrayList<Transaction>();
        for (Strategy strategy : strategies) {

            double actualNetLiqValue = MathUtils.round(getPortfolioService().getNetLiqValueDouble(strategy.getName()), 2);
            double targetNetLiqValue = MathUtils.round(portfolioNetLiqValue * strategy.getAllocation(), 2);
            double rebalanceAmount = targetNetLiqValue - actualNetLiqValue;

            totalAllocation += strategy.getAllocation();
            totalRebalanceAmount += rebalanceAmount;
            maxRebalanceAmount = Math.max(maxRebalanceAmount, rebalanceAmount);

            if (targetNetLiqValue != actualNetLiqValue) {
                Transaction transaction = new TransactionImpl();
                transaction.setDateTime(DateUtil.getCurrentEPTime());
                transaction.setQuantity(targetNetLiqValue > actualNetLiqValue ? +1 : -1);
                transaction.setPrice(RoundUtil.getBigDecimal(Math.abs(rebalanceAmount)));
                transaction.setCommission(new BigDecimal(0.0));
                transaction.setCurrency(this.portfolioBaseCurrency);
                transaction.setType(TransactionType.REBALANCE);
                transaction.setStrategy(strategy);

                transactions.add(transaction);
            }
        }

        if (MathUtils.round(totalAllocation, 2) != 1.0) {
            logger.warn("the total of all allocations is: " + totalAllocation + " where it should be 1.0");
            return;
        }

        if (MathUtils.round(totalRebalanceAmount, 0) != 0.0) {
            logger.warn("the total of all rebalance transactions is: " + totalRebalanceAmount + " where it should be 0.0");
            return;
        }

        if (maxRebalanceAmount / portfolioNetLiqValue < this.rebalanceThreshold) {
            logger.info("no rebalancing is performed because maximum rebalancing amount " + RoundUtil.getBigDecimal(maxRebalanceAmount) + " is less than "
                    + this.rebalanceThreshold + " of the entire portfolio of " + RoundUtil.getBigDecimal(portfolioNetLiqValue));
            return;
        }

        for (Transaction transaction : transactions) {

            // persist the transaction
            getTransactionService().persistTransaction(transaction);
        }
    }

    @Override
    protected void handleSavePortfolioValues() throws Exception {

        for (Strategy strategy : getStrategyDao().findAutoActivateStrategies()) {

            getPortfolioValueDao().create(createPortfolioValue(strategy));
        }
    }

    @Override
    protected void handleSavePortfolioValue(Strategy strategy, Transaction transaction) throws Exception {

        if (TransactionType.BUY.equals(transaction.getType()) || TransactionType.SELL.equals(transaction.getType()) || TransactionType.EXPIRATION.equals(transaction.getType())) {
            return; // nothing to do in case of BUY, SELL or EXPIRATION
        }

        PortfolioValue portfolioValue = createPortfolioValue(strategy);

        portfolioValue.setCashFlow(transaction.getGrossValue());

        getPortfolioValueDao().create(portfolioValue);
    }

    @Override
    protected PortfolioValue handleCreatePortfolioValue(Strategy strategy) {

        BigDecimal cashBalance;
        BigDecimal securitiesCurrentValue;
        BigDecimal maintenanceMargin;
        double leverage;
        if (strategy.isBase()) {
            cashBalance = getPortfolioService().getCashBalance();
            securitiesCurrentValue = getPortfolioService().getSecuritiesCurrentValue();
            maintenanceMargin = getPortfolioService().getMaintenanceMargin();
            leverage = getPortfolioService().getLeverage();

        } else {
            cashBalance = getPortfolioService().getCashBalance(strategy.getName());
            securitiesCurrentValue = getPortfolioService().getSecuritiesCurrentValue(strategy.getName());
            maintenanceMargin = getPortfolioService().getMaintenanceMargin(strategy.getName());
            leverage = getPortfolioService().getLeverage(strategy.getName());
        }

        PortfolioValue portfolioValue = PortfolioValue.Factory.newInstance();

        portfolioValue.setStrategy(strategy);
        portfolioValue.setDateTime(DateUtil.getCurrentEPTime());
        portfolioValue.setCashBalance(cashBalance);
        portfolioValue.setSecuritiesCurrentValue(securitiesCurrentValue);
        portfolioValue.setMaintenanceMargin(maintenanceMargin);
        portfolioValue.setNetLiqValue(cashBalance.add(securitiesCurrentValue)); // add here to prevent another lookup
        portfolioValue.setLeverage(Double.isNaN(leverage) ? 0 : leverage);
        portfolioValue.setAllocation(strategy.getAllocation());

        return portfolioValue;
    }
}
