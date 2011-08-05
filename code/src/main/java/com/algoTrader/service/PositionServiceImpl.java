package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Order;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.security.Expirable;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.enumeration.OrderStatus;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.OrderVO;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class PositionServiceImpl extends PositionServiceBase {

    private static Logger logger = MyLogger.getLogger(PositionServiceImpl.class.getName());

    protected void handleCloseAllPositions() throws Exception {

        for (Position position : getPositionDao().loadAll()) {
            if (position.isOpen()) {
                closePosition(position.getId());
            }
        }
    }

    protected void handleCloseAllPositionsByStrategy(String strategyName) throws Exception {

        for (Position position : getPositionDao().findOpenPositionsByStrategy(strategyName)) {
            if (position.isOpen()) {
                closePosition(position.getId());
            }
        }
    }

    protected void handleClosePosition(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        reducePosition(positionId, Math.abs(position.getQuantity()));

        // close children if any
        if (position.getChildren().size() != 0) {
            for (Position childPosition : position.getChildren()) {

                // childPosition
                closePosition(childPosition.getId());

                // remove the parent position
                removeParentPosition(childPosition.getId());
            }
        }
    }

    protected void handleReducePosition(int positionId, long quantity) throws Exception {

        Position position = getPositionDao().load(positionId);
        Security security = position.getSecurity();

        OrderVO order = new OrderVO();
        order.setStrategyName(position.getStrategy().getName());
        order.setSecurityId(security.getId());
        order.setRequestedQuantity(Math.abs(quantity));
        order.setTransactionType((position.getQuantity() > 0) ? TransactionType.SELL : TransactionType.BUY);

        getTransactionService().executeTransaction(order);

    }

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
            throw new PositionServiceException("ExitValue (" + exitValue + ") for short-position " + position.getId() + " is lower than currentValue: " + currentValue);
        } else if (Direction.LONG.equals(position.getDirection()) && exitValue > currentValue) {
            throw new PositionServiceException("ExitValue (" + exitValue + ") for long-position " + position.getId() + " is higher than currentValue: " + currentValue);
        }

        position.setExitValue(exitValue);
        getPositionDao().update(position);

        logger.info("set exit value " + position.getSecurity().getSymbol() + " to " + exitValue);
    }

    @Override
    protected void handleRemoveExitValue(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        position.setExitValue(null);
        getPositionDao().update(position);

        logger.info("removed exit value of " + position.getSecurity().getSymbol());

    }

    @Override
    protected void handleSetProfitTarget(int positionId, double profitValue, double profitLockIn) throws Exception {

        Position position = getPositionDao().load(positionId);

        // profit value cannot be lower than currentValue
        double currentValue = position.getSecurity().getLastTick().getCurrentValueDouble();
        if (Direction.SHORT.equals(position.getDirection()) && profitValue > currentValue) {
            throw new PositionServiceException("ProfitValue (" + profitValue + ") for short-position " + position.getId() + " is higher than currentValue: "
                    + currentValue);
        } else if (Direction.LONG.equals(position.getDirection()) && profitValue < currentValue) {
            throw new PositionServiceException("ProfitValue (" + profitValue + ") for long-position " + position.getId() + " is lower than currentValue: "
                    + currentValue);
        }

        position.setProfitValue(profitValue);
        position.setProfitLockIn(profitLockIn);
        getPositionDao().update(position);

        logger.info("set profit value " + position.getSecurity().getSymbol() + " to " + profitValue + " and profit lock in to " + profitLockIn);
    }

    protected void handleSetMargin(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);
        setMargin(position);
    }

    protected void handleSetMargin(Position position) throws Exception {

        Security security = position.getSecurity();
        double marginPerContract = security.getMargin();

        if (marginPerContract != 0) {

            long numberOfContracts = Math.abs(position.getQuantity());
            BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);
            position.setMaintenanceMargin(totalMargin);

            getPositionDao().update(position);

            double maintenanceMargin = position.getStrategy().getMaintenanceMarginDouble();

            logger.debug("set margin for " + security.getSymbol()
                    + " to " + RoundUtil.getBigDecimal(marginPerContract)
                    + " total margin: " + RoundUtil.getBigDecimal(maintenanceMargin));
        }
    }

    protected void handleSetMargins() throws Exception {

        List<Position> positions = getPositionDao().findOpenPositions();

        for (Position position : positions) {
            setMargin(position);
        }
    }

    protected void handleExpirePositions() throws Exception {

        Date date = DateUtil.getCurrentEPTime();
        List<Position> positions = getPositionDao().findExpirablePositions(date);

        for (Position position : positions) {
            expirePosition(position);
        }
    }

    protected void handleExpirePosition(Position position) throws Exception {

        Security security = position.getSecurity();
        if (!(security instanceof Expirable)) {
            logger.warn("position is not expirable");
            return;
        }

        // reverse the quantity
        long numberOfContracts = Math.abs(position.getQuantity());

        OrderVO order = new OrderVO();
        order.setStrategyName(position.getStrategy().getName());
        order.setSecurityId(security.getId());
        order.setRequestedQuantity(numberOfContracts);
        order.setTransactionType(TransactionType.EXPIRATION);

        Order executedOrder = getTransactionService().executeTransaction(order);

        // only remove the security from the watchlist, if the transaction did execute fully.
        // otherwise the next tick will execute the reminder of the order
        if (OrderStatus.EXECUTED.equals(executedOrder.getStatus()) || OrderStatus.AUTOMATIC.equals(executedOrder.getStatus())) {

            getMarketDataService().removeFromWatchlist(position.getStrategy(), security);
        }
    }

    @Override
    protected void handleAddChildPosition(int parentPositionId, int childPositionId) throws Exception {

        Position parentPosition = getPositionDao().load(parentPositionId);
        Position childPosition = getPositionDao().load(childPositionId);

        childPosition.setParent(parentPosition);
        parentPosition.getChildren().add(childPosition);

        getPositionDao().update(parentPosition);
        getPositionDao().update(childPosition);

        logger.info("added child position " + childPositionId + " to parent position " + parentPositionId);
    }

    @Override
    protected void handleRemoveParentPosition(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);
        Position parentPosition = position.getParent();

        if (parentPosition != null) {

            parentPosition.getChildren().remove(position);
            position.setParent(null);

            getPositionDao().update(parentPosition);
            getPositionDao().update(position);

            logger.info("removed parent position of position " + positionId);
        }
    }

    public static class ClosePositionSubscriber {

        public void update(int positionId) {

            long startTime = System.currentTimeMillis();
            logger.debug("closePosition start");

            ServiceLocator.serverInstance().getPositionService().closePosition(positionId);

            logger.debug("closePosition end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class SetExitValueSubscriber {

        public void update(int positionId, double exitValue) {

            long startTime = System.currentTimeMillis();
            logger.debug("setExitValue start");

            ServiceLocator.commonInstance().getPositionService().setExitValue(positionId, exitValue, false);

            logger.debug("setExitValue end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class SetMarginsListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {

            long startTime = System.currentTimeMillis();
            logger.debug("setMargins start");

            ServiceLocator.serverInstance().getPositionService().setMargins();

            logger.debug("setMargins end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }

    public static class ExpirePositionListener implements UpdateListener {

        public void update(EventBean[] newEvents, EventBean[] oldEvents) {

            long startTime = System.currentTimeMillis();
            logger.debug("expirePosition start");

            ServiceLocator.serverInstance().getPositionService().expirePositions();

            logger.debug("expirePosition end (" + (System.currentTimeMillis() - startTime) + "ms execution)");
        }
    }
}
