package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public abstract class AccountServiceImpl extends AccountServiceBase {

    private static Logger logger = MyLogger.getLogger(AccountServiceImpl.class.getName());

    private static Currency baseCurrency = Currency.fromString(ConfigurationUtil.getBaseConfig().getString("baseCurrency"));

    @SuppressWarnings("unchecked")
    protected void handleRebalancePortfolio() throws Exception {

        double portfolioNetLiqValue = getStrategyDao().getPortfolioNetLiqValueDouble();
        double totalAllocation = 0.0;
        Collection<Strategy> strategies = getStrategyDao().loadAll();
        for (Strategy strategy : strategies) {

            double actualNetLiqValue = MathUtils.round(strategy.getNetLiqValueDouble(), 2);
            double targetNetLiqValue = MathUtils.round(portfolioNetLiqValue * strategy.getAllocation(), 2);
            totalAllocation += strategy.getAllocation();

            if (targetNetLiqValue != actualNetLiqValue) {
                Transaction transaction = new TransactionImpl();
                transaction.setDateTime(DateUtil.getCurrentEPTime());
                transaction.setQuantity(targetNetLiqValue > actualNetLiqValue ? +1 : -1);
                transaction.setPrice(RoundUtil.getBigDecimal(Math.abs(targetNetLiqValue - actualNetLiqValue)));
                transaction.setCommission(new BigDecimal(0.0));
                transaction.setCurrency(baseCurrency);
                transaction.setType(TransactionType.REBALANCE);
                transaction.setStrategy(strategy);
                getTransactionDao().create(transaction);

                strategy.getTransactions().add(transaction);
                getStrategyDao().update(strategy);

                logger.info("rebalanced strategy " + strategy.getName() + " " + RoundUtil.getBigDecimal(targetNetLiqValue - actualNetLiqValue));
            }
        }

        if (totalAllocation != 1.0) {
            logger.warn("the total of all allocations is: " + totalAllocation + " where it should be 1.0");
        }
    }

    public static class ProcessCashTransactionsListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {

            long startTime = System.currentTimeMillis();
            logger.debug("processCashTransactions start");

            ServiceLocator.serverInstance().getDispatcherService().getAccountService().processCashTransactions();

            logger.debug("processCashTransactions end (" + (System.currentTimeMillis() - startTime) + "ms execution)");

        }
    }

    public static class RebalancePortfolioListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {

            long startTime = System.currentTimeMillis();
            logger.debug("rebalancePortfolio start");

            ServiceLocator.serverInstance().getDispatcherService().getAccountService().rebalancePortfolio();

            logger.debug("rebalancePortfolio end (" + (System.currentTimeMillis() - startTime) + "ms execution)");

        }
    }
}
