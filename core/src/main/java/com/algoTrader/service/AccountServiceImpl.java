package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.commons.math.util.MathUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

public abstract class AccountServiceImpl extends AccountServiceBase {

    private static Logger logger = MyLogger.getLogger(AccountServiceImpl.class.getName());

    private @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;

    @Override
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
                transaction.setCurrency(this.portfolioBaseCurrency);
                transaction.setType(TransactionType.REBALANCE);
                transaction.setStrategy(strategy);
                getTransactionDao().create(transaction);

                // add the amount to the balance
                getCashBalanceService().addAmount(transaction);

                strategy.getTransactions().add(transaction);
                getStrategyDao().update(strategy);

                logger.info("rebalanced strategy " + strategy.getName() + " " + RoundUtil.getBigDecimal(targetNetLiqValue - actualNetLiqValue));
            }
        }

        if (totalAllocation != 1.0) {
            logger.warn("the total of all allocations is: " + totalAllocation + " where it should be 1.0");
        }
    }
}
