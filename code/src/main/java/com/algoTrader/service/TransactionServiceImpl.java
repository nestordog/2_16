package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Order;
import com.algoTrader.entity.OrderImpl;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.TransactionImpl;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.OrderVO;
import com.algoTrader.vo.TradePerformanceVO;

public abstract class TransactionServiceImpl extends TransactionServiceBase {

    private static Logger logger = MyLogger.getLogger(TransactionServiceImpl.class.getName());
    private static Logger mailLogger = MyLogger.getLogger(TransactionServiceImpl.class.getName() + ".MAIL");
    private static Logger simulationLogger = MyLogger.getLogger(SimulationServiceImpl.class.getName() + ".RESULT");

    private static boolean externalTransactionsEnabled = ConfigurationUtil.getBaseConfig().getBoolean("externalTransactionsEnabled");
    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static boolean logTransactions = ConfigurationUtil.getBaseConfig().getBoolean("simulation.logTransactions");
    private static long eventsPerDay = ConfigurationUtil.getBaseConfig().getLong("simulation.eventsPerDay");

    protected Order handleExecuteTransaction(OrderVO orderVO) throws Exception {

        // construct a order-entity from the orderVO
        Order order = orderVOToEntity(orderVO);

        return executeTransaction(order);
    }

    protected Order handleExecuteTransaction(Order order) throws Exception {

        Strategy strategy = order.getStrategy();
        Security security = order.getSecurity();
        TransactionType transactionType = order.getTransactionType();
        long requestedQuantity = order.getRequestedQuantity();

        if (requestedQuantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than 0");
        }

        if (!OrderStatus.PREARRANGED.equals(order.getStatus())) {
            if (!simulation && externalTransactionsEnabled &&
                    (TransactionType.BUY.equals(transactionType) || TransactionType.SELL.equals(transactionType))) {
                executeExternalTransaction(order);
            } else {
                executeInternalTransaction(order);
            }
        }

        Collection<Transaction> transactions = order.getTransactions();
        long totalQuantity = 0;
        double totalPrice = 0.0;
        double totalCommission = 0.0;
        double totalProfit = 0.0;
        double profit = 0.0;
        double profitPct = 0.0;
        double avgAge = 0;

        for (Transaction transaction : transactions) {

            transaction.setType(transactionType);
            transaction.setSecurity(security);
            transaction.setCurrency(security.getSecurityFamily().getCurrency());

            // Strategy
            transaction.setStrategy(strategy);
            strategy.getTransactions().add(transaction);

            // Position
            Position position = getPositionDao().findBySecurityAndStrategy(security.getId(), strategy.getName());
            if (position == null) {

                position = new PositionImpl();
                position.setQuantity(transaction.getQuantity());

                position.setExitValue(null);
                position.setMaintenanceMargin(null);

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
                    double value = transaction.getValueDouble();
                    profit = value - cost;
                    profitPct = position.isLong() ? ((value - cost) / cost) : ((cost - value) / cost);
                    avgAge = position.getAverageAge();
                }

                position.setQuantity(position.getQuantity() + transaction.getQuantity());

                if (!position.isOpen()) {
                    position.setExitValue(null);
                    position.setMaintenanceMargin(null);
                }

                position.getTransactions().add(transaction);
                transaction.setPosition(position);

                getPositionDao().update(position);
            }

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

                getRuleService().sendEvent(StrategyImpl.BASE, tradePerformance);
            }

            String logMessage = "executed transaction type: " + transactionType +
                " quantity: " + transaction.getQuantity() +
                " of " + security.getSymbol() +
                " price: " + transaction.getPrice() +
                " commission: " + transaction.getCommission() +
                ((profit != 0.0) ? (
                    " profit: " + RoundUtil.getBigDecimal(profit) +
                    " profitPct: " + RoundUtil.getBigDecimal(profitPct) +
                    " avgAge: " + RoundUtil.getBigDecimal(avgAge))
                    : "");

            totalQuantity += transaction.getQuantity();
            totalPrice += transaction.getPrice().doubleValue() * transaction.getQuantity();
            totalCommission += transaction.getCommission().doubleValue();
            totalProfit += profit;

            if (simulation && logTransactions) {
                simulationLogger.info(logMessage);
            } else {
                logger.info(logMessage);
            }
        }

        if (order.getTransactions().size() > 0 && !simulation) {
            mailLogger.info("executed transaction type: " + transactionType +
                    " totalQuantity: " + totalQuantity +
                    " of " + security.getSymbol() +
                    " avgPrice: " + RoundUtil.getBigDecimal(totalPrice / totalQuantity) +
                    " commission: " + totalCommission +
                    " netLiqValue: " + strategy.getNetLiqValue() +
                    ((totalProfit != 0) ? (
                        " profit: " + RoundUtil.getBigDecimal(totalProfit))
                        : ""));

        }
        return order;
    }

    private void executeInternalTransaction(Order order) {

        Transaction transaction = new TransactionImpl();
        transaction.setDateTime(DateUtil.getCurrentEPTime());

        Security security = order.getSecurity();
        Position position = getPositionDao().findBySecurityAndStrategy(security.getId(), order.getStrategy().getName());
        Tick tick = security.getLastTick();

        if (tick == null) {
            throw new TransactionServiceException("no last tick available for security " + security);
        }

        double currentValue = tick.getCurrentValueDouble();

        // in daily / hourly / 30min / 15min simulation, if exitValue is reached during the day, take the exitValue
        // instead of the currentValue! because we will have passed the exitValue in the meantime
        if (simulation && TransactionType.BUY.equals(order.getTransactionType()) && (eventsPerDay <= 33)) {

            double exitValue = position.getExitValueDouble();
            if (currentValue > exitValue && DateUtil.compareToTime(security.getSecurityFamily().getMarketOpen()) > 0) {

                logger.info("adjusted currentValue (" + currentValue + ") to exitValue (" + exitValue+ ") in closePosition for order on " + order.getSecurity().getSymbol());
                currentValue = exitValue;
            }
        }

        int contractSize = security.getSecurityFamily().getContractSize();

        if (TransactionType.SELL.equals(order.getTransactionType())) {

            double bid = tick.getBid().doubleValue();

            transaction.setPrice(RoundUtil.getBigDecimal(bid));
            transaction.setQuantity(-Math.abs(order.getRequestedQuantity()));

        } else if (TransactionType.BUY.equals(order.getTransactionType())) {

            double ask = tick.getAsk().doubleValue();

            transaction.setPrice(RoundUtil.getBigDecimal(ask));
            transaction.setQuantity(Math.abs(order.getRequestedQuantity()));

        } else if (TransactionType.EXPIRATION.equals(order.getTransactionType())) {

            long quantity = -(int) Math.signum(position.getQuantity()) * Math.abs(order.getRequestedQuantity());
            transaction.setQuantity(quantity);

            if (security instanceof StockOption) {

                StockOption stockOption = (StockOption) security;
                double underlayingSpot = security.getUnderlaying().getLastTick().getCurrentValueDouble();
                double intrinsicValue = StockOptionUtil.getIntrinsicValue(stockOption, underlayingSpot);
                BigDecimal price = RoundUtil.getBigDecimal(intrinsicValue * contractSize);
                transaction.setPrice(price);

            } else if (security instanceof Future) {

                BigDecimal price = security.getUnderlaying().getLastTick().getCurrentValue();
                transaction.setPrice(price);

            } else {
                throw new IllegalArgumentException("EXPIRATION only allowed for " + security.getClass().getName());
            }
        }

        if (TransactionType.SELL.equals(order.getTransactionType()) || TransactionType.BUY.equals(order.getTransactionType())) {

            if(security.getSecurityFamily().getCommission() == null) {
                throw new RuntimeException("commission is undefined for " + security.getSymbol());
            }

            double commission = Math.abs(order.getRequestedQuantity() * security.getSecurityFamily().getCommission().doubleValue());
            transaction.setCommission(RoundUtil.getBigDecimal(commission));
        } else {
            transaction.setCommission(new BigDecimal(0));
        }
        transaction.setNumber(null);

        order.setStatus(OrderStatus.AUTOMATIC);
        order.getTransactions().add(transaction);
    }

    protected double getPrice(Order order, double spreadPosition, double bid, double ask) {

        double price = 0.0;
        if (TransactionType.BUY.equals(order.getTransactionType())) {
            price = bid + spreadPosition * (ask - bid);
        } else if (TransactionType.SELL.equals(order.getTransactionType())) {
            price = ask - spreadPosition * (ask - bid);
        }

        double tickSize = order.getSecurity().getSecurityFamily().getTickSize();

        return RoundUtil.roundToNextN(price, tickSize);
    }

    /**
     * implemented here because Order is nonPersistent
     */
    private Order orderVOToEntity(OrderVO orderVO) {

        Order order = new OrderImpl();
        order.setStrategy(getStrategyDao().findByName(orderVO.getStrategyName()));
        order.setSecurity(getSecurityDao().load(orderVO.getSecurityId()));
        order.setRequestedQuantity(orderVO.getRequestedQuantity());
        order.setTransactionType(orderVO.getTransactionType());

        return order;
    }
}
