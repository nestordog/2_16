package com.algoTrader.service;

import org.apache.commons.math.MathException;
import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Security;
import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.MyLogger;
import com.algoTrader.vo.OrderVO;

public class PositionServiceImpl extends PositionServiceBase {

    private static Logger logger = MyLogger.getLogger(StockOptionServiceImpl.class.getName());

    protected void handleClosePosition(int positionId) throws Exception {

        Position position = getPositionDao().load(positionId);

        reducePosition(positionId, Math.abs(position.getQuantity()));
    }

    protected void handleReducePosition(int positionId, long quantity) throws Exception {

        Position position = getPositionDao().load(positionId);
        Security security = position.getSecurity();

        OrderVO order = new OrderVO();
        order.setStrategyName(position.getStrategy().getName());
        order.setSecurityId(security.getId());
        order.setRequestedQuantity(Math.abs(quantity));
        order.setTransactionType((position.getQuantity() > 0) ? TransactionType.SELL : TransactionType.BUY);

        getDispatcherService().getTransactionService().executeTransaction(position.getStrategy().getName(), order);

        // only remove the stockOption from the watchlist, if the position is closed
        if (!position.isOpen()) {
            getDispatcherService().getTickService().removeFromWatchlist(position.getStrategy(), security);
        }

        // if there is a and OPEN_POSITION rule active for this stockOption deactivate it
        getRuleService().deactivateDependingOnTarget(position.getStrategy().getName(), "OPEN_POSITION", security.getId());
    }

    protected void handleSetExitValue(int positionId, double exitValue, boolean force) throws MathException {

        Position position = getPositionDao().load(positionId);

        // there needs to be a position
        if (position == null) {
            throw new StockOptionServiceException("position does not exist: " + positionId);
        }

        // in generall there should have been set a exitValue on creation of the
        // position
        if (!force && position.getExitValue() == null) {
            logger.warn("no exitValue was set for position: " + positionId);
            return;
        }

        // we don't want to set the exitValue to Zero
        if (exitValue <= 0.05) {
            logger.warn("setting of exitValue below 0.05 is prohibited: " + exitValue);
            return;
        }

        // in generall, exit value should not be set higher than existing
        // exitValue
        if (!force && exitValue > position.getExitValue().doubleValue()) {
            logger.warn("exit value " + exitValue + " is higher than existing exit value " + position.getExitValue() + " of position " + positionId);
            return;
        }

        // exitValue cannot be lower than currentValue
        double currentValue = position.getSecurity().getLastTick().getCurrentValueDouble();
        if (exitValue < currentValue ) {
            throw new StockOptionServiceException("ExitValue (" + exitValue + ") for position " + position.getId() + " is lower than currentValue: " + currentValue);
        }

        position.setExitValue(exitValue);
        getPositionDao().update(position);

        logger.info("set exit value " + position.getSecurity().getSymbol() + " to " + exitValue);
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
}
