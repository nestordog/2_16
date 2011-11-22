package com.algoTrader.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Transaction;
import com.algoTrader.entity.security.Future;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.trade.MarketOrder;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderCallback;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.stockOption.StockOptionUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.ExpirePositionVO;

public class PositionServiceImpl extends PositionServiceBase {

    private static Logger logger = MyLogger.getLogger(PositionServiceImpl.class.getName());
    private static DecimalFormat format = new DecimalFormat("#,##0.0000");

    @Override
    protected void handleCloseAllPositionsByStrategy(String strategyName, boolean removeFromWatchlist) throws Exception {

        for (Position position : getPositionDao().findOpenPositionsByStrategy(strategyName)) {
            if (position.isOpen()) {
                closePosition(position.getId(), removeFromWatchlist);
            }
        }
    }

    @Override
    protected void handleClosePosition(int positionId, boolean removeFromWatchlist) throws Exception {

        Position position = getPositionDao().load(positionId);

        if (position.isOpen()) {

            Strategy strategy = position.getStrategy();
            Security security = position.getSecurity();

            Side side = (position.getQuantity() > 0) ? Side.SELL : Side.BUY;

            // prepare the order
            MarketOrder order = MarketOrder.Factory.newInstance();
            order.setStrategy(strategy);
            order.setSecurity(security);
            order.setQuantity(Math.abs(position.getQuantity()));
            order.setSide(side);

            // create an OrderCallback if removeFromWatchlist is requested
            if (removeFromWatchlist) {

                getRuleService().addOrderCallback(StrategyImpl.BASE, new Order[] { order }, new OrderCallback() {
                    @Override
                    public void orderCompleted(OrderStatus[] orderStati) throws Exception {
                        MarketDataService marketDataService = ServiceLocator.commonInstance().getMarketDataService();
                        for (OrderStatus orderStatus : orderStati) {
                            if (Status.EXECUTED.equals(orderStatus.getStatus())) {
                                Order order = orderStatus.getParentOrder();
                                marketDataService.removeFromWatchlist(order.getStrategy().getName(), order.getSecurity().getId());
                            }
                        }
                    }
                });
            }

            getOrderService().sendOrder(order);

        } else {

            // if there was no open position bat removeFromWatchlist was requested do that anyway
            getMarketDataService().removeFromWatchlist(position.getStrategy().getName(), position.getSecurity().getId());
        }
    }

    @Override
    protected void handleReducePosition(int positionId, long quantity) throws Exception {

        Position position = getPositionDao().load(positionId);
        Strategy strategy = position.getStrategy();
        Security security = position.getSecurity();

        Side side = (position.getQuantity() > 0) ? Side.SELL : Side.BUY;

        MarketOrder order = MarketOrder.Factory.newInstance();
        order.setStrategy(strategy);
        order.setSecurity(security);
        order.setQuantity(Math.abs(quantity));
        order.setSide(side);

        getOrderService().sendOrder(order);
    }

    @Override
    protected void handleSetExitValue(int positionId, double exitValue, boolean force) throws MathException {

        Position position = getPositionDao().load(positionId);

        // we don't want to set the exitValue to (almost)Zero
        if (exitValue <= 0.05) {
            logger.warn("setting of exitValue below 0.05 is prohibited: " + exitValue);
            return;
        }

        // in generall, exit value should not be set higher (lower) than existing exitValue for long (short) positions
        if (!force) {
            if (Direction.SHORT.equals(position.getDirection()) && exitValue > position.getExitValueDouble()) {
                logger.warn("exit value " + exitValue + " is higher than existing exit value " + position.getExitValue() + " of short position " + positionId);
                return;
            } else if (Direction.LONG.equals(position.getDirection()) && exitValue < position.getExitValueDouble()) {
                logger.warn("exit value " + exitValue + " is lower than existing exit value " + position.getExitValue() + " of long position " + positionId);
                return;
            }
        }

        // exitValue cannot be lower than currentValue
        double currentValue = position.getSecurity().getLastTick().getCurrentValueDouble();
        if (Direction.SHORT.equals(position.getDirection()) && exitValue < currentValue) {
            throw new PositionServiceException("ExitValue (" + exitValue + ") for short-position " + position.getId() + " is lower than currentValue: "
                    + currentValue);
        } else if (Direction.LONG.equals(position.getDirection()) && exitValue > currentValue) {
            throw new PositionServiceException("ExitValue (" + exitValue + ") for long-position " + position.getId() + " is higher than currentValue: "
                    + currentValue);
        }

        position.setExitValue(exitValue);
        getPositionDao().update(position);

        logger.info("set exit value " + position.getSecurity().getSymbol() + " to " + format.format(exitValue));
    }

    @Override
    protected void handleRemoveExitValue(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        if (position.getExitValue() != null) {

            position.setExitValue(null);
            getPositionDao().update(position);

            logger.info("removed exit value of " + position.getSecurity().getSymbol());
        }
    }

    @Override
    protected void handleSetProfitTarget(int positionId, double profitTarget) throws Exception {

        Position position = getPositionDao().load(positionId);

        // profit value cannot be lower than currentValue
        double currentValue = position.getSecurity().getLastTick().getCurrentValueDouble();
        if (Direction.SHORT.equals(position.getDirection()) && profitTarget > currentValue) {
            throw new PositionServiceException("ProfitValue (" + profitTarget + ") for short-position " + position.getId() + " is higher than currentValue: "
                    + currentValue);
        } else if (Direction.LONG.equals(position.getDirection()) && profitTarget < currentValue) {
            throw new PositionServiceException("ProfitValue (" + profitTarget + ") for long-position " + position.getId() + " is lower than currentValue: "
                    + currentValue);
        }

        position.setProfitTarget(profitTarget);
        getPositionDao().update(position);

        logger.info("set profit target " + position.getSecurity().getSymbol() + " to " + profitTarget);
    }

    @Override
    protected void handleSetMargin(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);
        setMargin(position);
    }

    @Override
    protected void handleSetMargin(Position position) throws Exception {

        Security security = position.getSecurity();
        double marginPerContract = security.getMargin();

        if (marginPerContract != 0) {

            long numberOfContracts = Math.abs(position.getQuantity());
            BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);
            position.setMaintenanceMargin(totalMargin);

            getPositionDao().update(position);

            double maintenanceMargin = position.getStrategy().getMaintenanceMarginDouble();

            logger.debug("set margin for " + security.getSymbol() + " to " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: "
                    + RoundUtil.getBigDecimal(maintenanceMargin));
        }
    }

    @Override
    protected void handleSetMargins() throws Exception {

        List<Position> positions = getPositionDao().findOpenPositions();

        for (Position position : positions) {
            setMargin(position);
        }
    }

    @Override
    protected void handleExpirePositions() throws Exception {

        Date date = DateUtil.getCurrentEPTime();
        List<Position> positions = getPositionDao().findExpirablePositions(date);

        for (Position position : positions) {
            expirePosition(position);
        }
    }

    @Override
    protected void handleExpirePosition(Position position) throws Exception {

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
            int contractSize = security.getSecurityFamily().getContractSize();
            int scale = security.getSecurityFamily().getScale();
            double underlayingSpot = security.getUnderlaying().getLastTick().getCurrentValueDouble();
            double intrinsicValue = StockOptionUtil.getIntrinsicValue(stockOption, underlayingSpot);
            BigDecimal price = RoundUtil.getBigDecimal(intrinsicValue * contractSize, scale);
            transaction.setPrice(price);

        } else if (security instanceof Future) {

            BigDecimal price = security.getUnderlaying().getLastTick().getCurrentValue();
            transaction.setPrice(price);

        } else {
            throw new IllegalArgumentException("EXPIRATION only allowed for " + security.getClass().getName());
        }

        // perisite the transaction
        getTransactionService().persistTransaction(transaction);

        // remove the security from the watchlist
        getMarketDataService().removeFromWatchlist(position.getStrategy().getName(), security.getId());

        // propagate the ExpirePosition event
        getRuleService().sendEvent(position.getStrategy().getName(), expirePositionEvent);
    }

}
