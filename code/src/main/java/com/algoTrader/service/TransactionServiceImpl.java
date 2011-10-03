package com.algoTrader.service;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.TradePerformanceVO;

public abstract class TransactionServiceImpl extends TransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());
    private static Logger simulationLogger = MyLogger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static boolean logTransactions = ConfigurationUtil.getBaseConfig().getBoolean("simulation.logTransactions");

    protected void handleCreateTransaction(Fill fill) throws Exception {

        Strategy strategy = fill.getParentOrder().getStrategy();
        Security security = fill.getParentOrder().getSecurity();

        // initialize the security & strategy
        HibernateUtil.lock(this.getSessionFactory(), security);
        HibernateUtil.lock(this.getSessionFactory(), strategy);

        TransactionType transactionType = Side.BUY.equals(fill.getSide()) ? TransactionType.BUY : TransactionType.SELL;
        long quantity = Side.BUY.equals(fill.getSide()) ? fill.getQuantity() : -fill.getQuantity();

        Transaction transaction = new TransactionImpl();
        transaction.setDateTime(fill.getDateTime());
        transaction.setQuantity(quantity);
        transaction.setPrice(fill.getPrice());
        transaction.setType(transactionType);
        transaction.setSecurity(security);
        transaction.setStrategy(strategy);
        transaction.setCurrency(security.getSecurityFamily().getCurrency());
        transaction.setCommission(fill.getCommission());

        persistTransaction(transaction);
        propagateTransaction(transaction);
    }

    protected void handlePersistTransaction(Transaction transaction) throws Exception {

        Strategy strategy = transaction.getStrategy();
        Security security = transaction.getSecurity();

        // Strategy
        strategy.getTransactions().add(transaction);
        double profit = 0.0;
        double profitPct = 0.0;
        double avgAge = 0;

        // Position
        Position position = getPositionDao().findBySecurityAndStrategy(security.getId(), strategy.getName());
        if (position == null) {

            position = new PositionImpl();
            position.setQuantity(transaction.getQuantity());

            position.setExitValue(null);
            position.setMaintenanceMargin(null);
            position.setProfitValue(null);
            position.setProfitLockIn(null);

            position.setSecurity(security);
            security.getPositions().add(position);

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            position.setStrategy(strategy);
            strategy.getPositions().add(position);

            getPositionDao().create(position);

        } else {

            // evaluate the profit in closing transactions
            // must get this before attaching the new transaction
            if (Long.signum(position.getQuantity()) * Long.signum(transaction.getQuantity()) == -1) {
                double cost = position.getCostDouble() * Math.abs((double) transaction.getQuantity() / (double) position.getQuantity());
                double value = transaction.getNetValueDouble();
                profit = value - cost;
                profitPct = Direction.LONG.equals(position.getDirection()) ? ((value - cost) / cost) : ((cost - value) / cost);
                avgAge = position.getAverageAge();
            }

            position.setQuantity(position.getQuantity() + transaction.getQuantity());

            if (!position.isOpen()) {
                position.setExitValue(null);
                position.setMaintenanceMargin(null);
                position.setProfitValue(null);
                position.setProfitLockIn(null);
            }

            position.getTransactions().add(transaction);
            transaction.setPosition(position);

            getPositionDao().update(position);
        }

        // add the amount to the corresponding cashBalance
        getCashBalanceService().addAmount(transaction);

        // update all entities
        getTransactionDao().create(transaction);
        getStrategyDao().update(strategy);
        getSecurityDao().update(security);

        // create a TradePerformanceVO and send it back into Esper
        if (profit != 0.0) {
            TradePerformanceVO tradePerformance = new TradePerformanceVO();
            tradePerformance.setProfit(profit);
            tradePerformance.setProfitPct(profitPct);
            tradePerformance.setAvgAge(avgAge);
            tradePerformance.setWinning(profit > 0);

            getRuleService().routeEvent(StrategyImpl.BASE, tradePerformance);
        }

        //@formatter:off
        String logMessage = "executed transaction type: " + transaction.getType() +
        " quantity: " + transaction.getQuantity() +
        " of " + security.getSymbol() +
        " price: " + transaction.getPrice() +
        " commission: " + transaction.getCommission() +
        ((profit != 0.0) ? (
            " profit: " + RoundUtil.getBigDecimal(profit) +
            " profitPct: " + RoundUtil.getBigDecimal(profitPct) +
            " avgAge: " + RoundUtil.getBigDecimal(avgAge))
            : "");
        //@formatter:on

        if (simulation && logTransactions) {
            simulationLogger.info(logMessage);
        } else {
            logger.info(logMessage);
        }

    }

    @Override
    protected void handlePropagateTransaction(Transaction transaction) {

        // also send the transaction to the corresponding strategy
        if (!StrategyImpl.BASE.equals(transaction.getStrategy().getName())) {
            getRuleService().sendEvent(transaction.getStrategy().getName(), transaction);
        }
    }

    @Override
    protected void handlePropagateFill(Fill fill) throws Exception {

        // send the fill to the strategy that placed the corresponding order
        if (!StrategyImpl.BASE.equals(fill.getParentOrder().getStrategy().getName())) {
            getRuleService().sendEvent(fill.getParentOrder().getStrategy().getName(), fill);
        }
    }

}
