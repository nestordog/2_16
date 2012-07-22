package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionImpl;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Combination;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.trade.InitializingOrderI;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.entity.trade.TradeCallback;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.ClosePositionVO;
import com.algoTrader.vo.ExpirePositionVO;
import com.algoTrader.vo.TransactionSummaryVO;

public class PositionServiceImpl extends PositionServiceBase {

    private static Logger logger = MyLogger.getLogger(PositionServiceImpl.class.getName());

    @Override
    protected void handleCloseAllPositionsByStrategy(String strategyName, boolean unsubscribe) throws Exception {

        for (Position position : getPositionDao().findOpenPositionsByStrategy(strategyName)) {
            if (position.isOpen()) {
                closePosition(position.getId(), unsubscribe);
            }
        }
    }

    @Override
    protected void handleClosePosition(int positionId, final boolean unsubscribe) throws Exception {

        final Position position = getPositionDao().get(positionId);

        if (position.isOpen()) {

            // handle Combinations by the combination service
            if (position.getSecurity() instanceof Combination) {
                getCombinationService().closeCombination(position.getSecurity().getId(), position.getStrategy().getName());
            } else {
                reduceOrClosePosition(position, position.getQuantity(), unsubscribe);
            }

        } else {

            // if there was no open position but unsubscribe was requested do that anyway
            if (unsubscribe) {
                getMarketDataService().unsubscribe(position.getStrategy().getName(), position.getSecurity().getId());
            }
        }
    }

    @Override
    protected void handleReducePosition(int positionId, long quantity) throws Exception {

        Position position = getPositionDao().get(positionId);

        if (Math.abs(quantity) > Math.abs(position.getQuantity())) {
            throw new PositionServiceException("position reduction of " + quantity + " for position " + position.getId() + " is greater than current quantity " + position.getQuantity());
        } else {
            reduceOrClosePosition(position, quantity, false);
        }
    }

    private void reduceOrClosePosition(final Position position, long quantity, final boolean unsubscribe) {

        Strategy strategy = position.getStrategy();
        Security security = position.getSecurityInitialized();

        Side side = (position.getQuantity() > 0) ? Side.SELL : Side.BUY;

        // prepare the order
        Order order = getOrderPreferenceDao().createOrder(strategy.getName(), security.getClass());

        order.setStrategy(strategy);
        order.setSecurity(security);
        order.setQuantity(Math.abs(quantity));
        order.setSide(side);

        // initialize the order if necessary
        if (order instanceof InitializingOrderI) {
            ((InitializingOrderI) order).init(null);
        }

        // unsubscribe is requested / notify non-full executions
        EsperManager.addTradeCallback(StrategyImpl.BASE, Collections.singleton(order), new TradeCallback() {
            @Override
            public void onTradeCompleted(List<OrderStatus> orderStati) throws Exception {
                for (OrderStatus orderStatus : orderStati) {
                    Order order = orderStatus.getParentOrder();
                    if (Status.EXECUTED.equals(orderStatus.getStatus())) {
                        if (unsubscribe) {
                            MarketDataService marketDataService = ServiceLocator.instance().getMarketDataService();
                            marketDataService.unsubscribe(order.getStrategy().getName(), order.getSecurity().getId());
                        }
                    } else {
                        logger.error("reduction / closing of position " + position.getId() +
                                " did not executed fully, filledQty: " + orderStatus.getFilledQuantity() +
                                " remainingQty: " + orderStatus.getRemainingQuantity());
                    }
                }
            }
        });

        getOrderService().sendOrder(order);
    }

    @Override
    protected Position handleCreateNonTradeablePosition(String strategyName, int securityId, long quantity) {

        Security security = getSecurityDao().get(securityId);
        Strategy strategy = getStrategyDao().findByName(strategyName);

        if (security.getSecurityFamily().isTradeable()) {
            throw new PositionServiceException(security + " is tradeable, can only creat non-tradeable positions");
        }

        Position position = new PositionImpl();
        position.setQuantity(quantity);

        position.setExitValue(null);
        position.setMaintenanceMargin(null);

        // associate the security
        security.addPositions(position);

        // associate the strategy
        strategy.addPositions(position);

        getPositionDao().create(position);

        logger.info("created non-tradeable position on " + security + " for strategy " + strategyName + " quantity " + quantity);

        return position;
    }

    @Override
    protected Position handleModifyNonTradeablePosition(int positionId, long quantity) {

        Position position = getPositionDao().get(positionId);

        if (position == null) {
            throw new PositionServiceException("position " + positionId + " could not be found");
        }

        position.setQuantity(quantity);

        logger.info("modified non-tradeable position " + positionId + " new quantity " + quantity);

        return position;
    }

    @Override
    protected void handleDeleteNonTradeablePosition(int positionId, boolean unsubscribe) throws Exception {

        Position position = getPositionDao().get(positionId);

        Security security = position.getSecurity();

        if (security.getSecurityFamily().isTradeable()) {
            throw new PositionServiceException(security + " is tradeable, can only delete non-tradeable positions");
        }

        ClosePositionVO closePositionVO = getPositionDao().toClosePositionVO(position);

        // propagate the ClosePosition event
        EsperManager.sendEvent(position.getStrategy().getName(), closePositionVO);

        getPositionDao().remove(position);

        logger.info("deleted non-tradeable position " + position.getId() + " on " + security + " for strategy " + position.getStrategy().getName());

        // unsubscribe if necessary
        if (unsubscribe) {
            getMarketDataService().unsubscribe(position.getStrategy().getName(), position.getSecurity().getId());
        }
    }

    @Override
    protected Position handleSetExitValue(int positionId, double exitValue, boolean force) throws MathException {

        Position position = getPositionDao().get(positionId);

        // prevent exitValues near Zero
        if (exitValue <= 0.05) {
            logger.warn("setting of exitValue below 0.05 is prohibited: " + exitValue);
            return position;
        }

        // in generall, exit value should not be set higher (lower) than existing exitValue for long (short) positions
        if (!force) {
            if (Direction.SHORT.equals(position.getDirection()) && position.getExitValue() != null && exitValue > position.getExitValueDouble()) {
                logger.warn("exit value " + exitValue + " is higher than existing exit value " + position.getExitValue() + " of short position " + positionId);
                return position;
            } else if (Direction.LONG.equals(position.getDirection()) && position.getExitValue() != null && exitValue < position.getExitValueDouble()) {
                logger.warn("exit value " + exitValue + " is lower than existing exit value " + position.getExitValue() + " of long position " + positionId);
                return position;
            }
        }

        // exitValue cannot be lower than currentValue
        Tick tick = position.getSecurity().getLastTick();
        if (tick != null) {
            double currentValue = tick.getCurrentValueDouble();
            if (Direction.SHORT.equals(position.getDirection()) && exitValue < currentValue) {
                throw new PositionServiceException("ExitValue (" + exitValue + ") for short-position " + position.getId() + " is lower than currentValue: " + currentValue);
            } else if (Direction.LONG.equals(position.getDirection()) && exitValue > currentValue) {
                throw new PositionServiceException("ExitValue (" + exitValue + ") for long-position " + position.getId() + " is higher than currentValue: " + currentValue);
            }
        }

        position.setExitValue(exitValue);

        int scale = position.getSecurity().getSecurityFamily().getScale();
        logger.info("set exit value of position " + position.getId() + " to " + RoundUtil.getBigDecimal(exitValue, scale));

        return position;
    }

    @Override
    protected Position handleRemoveExitValue(int positionId) throws Exception {

        Position position = getPositionDao().get(positionId);

        if (position.getExitValue() != null) {

            position.setExitValue(null);

            logger.info("removed exit value of position " + positionId);
        }

        return position;
    }

    @Override
    protected Position handleSetMargin(int positionId) throws Exception {

        Position position = getPositionDao().get(positionId);

        setMargin(position);

        return position;
    }

    @Override
    protected void handleSetMargins() throws Exception {

        List<Position> positions = getPositionDao().findOpenPositions();

        for (Position position : positions) {
            setMargin(position);
        }
    }

    @Override
    protected void handleResetPositions() throws Exception {

        Collection<TransactionSummaryVO> transactionSummaries = getTransactionDao().findTransactionSummaries();

        for (TransactionSummaryVO summary : transactionSummaries) {

            Position position = getPositionDao().findBySecurityAndStrategy(summary.getSecurityId(), summary.getStrategyName());
            if (position == null) {

                logger.warn("position on security " + summary.getSecurityId() + " strategy " + summary.getStrategyName() + " quantity " + summary.getQuantity() + " does not exist");

            } else if (position.getQuantity() != summary.getQuantity()) {

                long existingQty = position.getQuantity();
                position.setQuantity(summary.getQuantity());

                logger.warn("adjusted quantity of position " + position.getId() + " from " + existingQty + " to " + summary.getQuantity());
            }
        }

        List<Position> openPositions = getPositionDao().findOpenTradeablePositions();

        for (final Position position : openPositions) {

            TransactionSummaryVO summary = CollectionUtils.find(transactionSummaries, new Predicate<TransactionSummaryVO>() {
                @Override
                public boolean evaluate(TransactionSummaryVO summary) {
                    return position.getSecurity().getId() == summary.getSecurityId() && position.getStrategy().getName().equals(summary.getStrategyName());
                }
            });

            if (summary == null) {
                logger.warn("position " + position.getId() + " should have qty=0");
            }
        }
    }

    private void setMargin(Position position) throws Exception {

        Security security = position.getSecurity();
        double marginPerContract = security.getMargin();

        if (marginPerContract != 0) {

            long numberOfContracts = Math.abs(position.getQuantity());
            BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);

            position.setMaintenanceMargin(totalMargin);

            double maintenanceMargin = getPortfolioService().getMaintenanceMarginDouble(position.getStrategy().getName());

            logger.debug("set margin of position " + position.getId() + " to: " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + RoundUtil.getBigDecimal(maintenanceMargin));
        }
    }

    @Override
    protected void handleExpirePositions() throws Exception {

        Date date = DateUtil.getCurrentEPTime();
        Collection<Position> positions = getPositionDao().findExpirablePositions(date);

        for (Position position : positions) {
            expirePosition(position);
        }
    }

    @Override
    protected void handleExpirePosition(int positionId) throws Exception {
        Position position = getPositionDao().get(positionId);

        expirePosition(position);
    }

    private void expirePosition(Position position) throws Exception {

        Security security = position.getSecurity();

        ExpirePositionVO expirePositionEvent = getPositionDao().toExpirePositionVO(position);

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setDateTime(DateUtil.getCurrentEPTime());
        transaction.setType(TransactionType.EXPIRATION);
        transaction.setQuantity(-position.getQuantity());
        transaction.setSecurity(security);
        transaction.setStrategy(position.getStrategy());
        transaction.setCurrency(security.getSecurityFamily().getCurrency());
        transaction.setCommission(new BigDecimal(0));

        if (security instanceof StockOption) {

            StockOption stockOption = (StockOption) security;
            int scale = security.getSecurityFamily().getScale();
            double underlyingSpot = security.getUnderlying().getLastTick().getCurrentValueDouble();
            double intrinsicValue = StockOptionUtil.getIntrinsicValue(stockOption, underlyingSpot);
            BigDecimal price = RoundUtil.getBigDecimal(intrinsicValue, scale);
            transaction.setPrice(price);

        } else if (security instanceof Future) {

            BigDecimal price = security.getUnderlying().getLastTick().getCurrentValue();
            transaction.setPrice(price);

        } else {
            throw new IllegalArgumentException("EXPIRATION only allowed for " + security.getClass().getName());
        }

        // perisite the transaction
        getTransactionService().persistTransaction(transaction);

        // unsubscribe the security
        getMarketDataService().unsubscribe(position.getStrategy().getName(), security.getId());

        // propagate the ExpirePosition event
        EsperManager.sendEvent(position.getStrategy().getName(), expirePositionEvent);
    }
}
