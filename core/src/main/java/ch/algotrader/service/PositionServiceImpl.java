/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionImpl;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.StockOption;
import ch.algotrader.entity.strategy.DefaultOrderPreference;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.esper.callback.TradeCallback;
import ch.algotrader.stockOption.StockOptionUtil;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.PositionUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.ExpirePositionVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PositionServiceImpl extends PositionServiceBase {

    private static Logger logger = MyLogger.getLogger(PositionServiceImpl.class.getName());

    private @Value("${simulation}") boolean simulation;

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
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        Security security = position.getSecurityInitialized();

        if (position.isOpen()) {

            // handle Combinations by the combination service
            if (security instanceof Combination) {
                getCombinationService().closeCombination(security.getId(), position.getStrategy().getName());
            } else {
                reduceOrClosePosition(position, position.getQuantity(), unsubscribe);
            }

        } else {

            // if there was no open position but unsubscribe was requested do that anyway
            if (unsubscribe) {
                getMarketDataService().unsubscribe(position.getStrategy().getName(), security.getId());
            }
        }
    }

    @Override
    protected void handleReducePosition(int positionId, long quantity) throws Exception {

        Position position = getPositionDao().get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

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
        DefaultOrderPreference defaultOrderPreference = getDefaultOrderPreferenceDao().findByStrategyAndSecurityFamilyInclOrderPreference(strategy.getName(), security.getSecurityFamily().getId());

        if (defaultOrderPreference == null) {
            throw new IllegalStateException("no defaultOrderPreference defined for " + security.getSecurityFamily() + " and " + strategy);
        }

        Order order = defaultOrderPreference.getOrderPreference().createOrder();

        order.setStrategy(strategy);
        order.setSecurity(security);
        order.setQuantity(Math.abs(quantity));
        order.setSide(side);

        // unsubscribe is requested / notify non-full executions in live-trading
        if (this.simulation) {
            if (unsubscribe) {
                getMarketDataService().unsubscribe(order.getStrategy().getName(), order.getSecurity().getId());
            }
        } else {
            EngineLocator.instance().getBaseEngine().addTradeCallback(Collections.singleton(order), new TradeCallback(true) {
                @Override
                public void onTradeCompleted(List<OrderStatus> orderStati) throws Exception {
                    if (unsubscribe) {
                        for (OrderStatus orderStatus : orderStati) {
                            Order order = orderStatus.getOrd();
                            if (Status.EXECUTED.equals(orderStatus.getStatus())) {
                                // use ServiceLocator because TradeCallback is executed in a new thread
                                MarketDataService marketDataService = ServiceLocator.instance().getMarketDataService();
                                marketDataService.unsubscribe(order.getStrategy().getName(), order.getSecurity().getId());
                            }
                        }
                    }
                }
            });
        }

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
        position.setStrategy(strategy);

        getPositionDao().create(position);

        logger.info("created non-tradeable position on " + security + " for strategy " + strategyName + " quantity " + quantity);

        return position;
    }


    @Override
    protected void handleTransferPosition(int positionId, String targetStrategyName) throws Exception {

        Position position = getPositionDao().get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }


        Strategy targetStrategy =  getStrategyDao().findByName(targetStrategyName);
        Security security = position.getSecurity();
        SecurityFamily family = security.getSecurityFamily();
        long quantity = position.getQuantity();
        BigDecimal price = RoundUtil.getBigDecimal(position.getMarketPrice(), family.getScale());

        // debit transaction
        Transaction debitTransaction = Transaction.Factory.newInstance();
        debitTransaction.setDateTime(DateUtil.getCurrentEPTime());
        debitTransaction.setQuantity(-quantity);
        debitTransaction.setPrice(price);
        debitTransaction.setCurrency(family.getCurrency());
        debitTransaction.setType(TransactionType.TRANSFER);
        debitTransaction.setSecurity(security);
        debitTransaction.setStrategy(position.getStrategy());

        // persiste the transaction
        getTransactionService().persistTransaction(debitTransaction);

        // credit transaction
        Transaction creditTransaction = Transaction.Factory.newInstance();
        creditTransaction.setDateTime(DateUtil.getCurrentEPTime());
        creditTransaction.setQuantity(quantity);
        creditTransaction.setPrice(price);
        creditTransaction.setCurrency(family.getCurrency());
        creditTransaction.setType(TransactionType.TRANSFER);
        creditTransaction.setSecurity(security);
        creditTransaction.setStrategy(targetStrategy);

        // persiste the transaction
        getTransactionService().persistTransaction(creditTransaction);
    }

    @Override
    protected Position handleModifyNonTradeablePosition(int positionId, long quantity) {

        Position position = getPositionDao().getLocked(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        position.setQuantity(quantity);

        logger.info("modified non-tradeable position " + positionId + " new quantity " + quantity);

        return position;
    }

    @Override
    protected void handleDeleteNonTradeablePosition(int positionId, boolean unsubscribe) throws Exception {

        Position position = getPositionDao().get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        Security security = position.getSecurity();

        if (security.getSecurityFamily().isTradeable()) {
            throw new PositionServiceException(security + " is tradeable, can only delete non-tradeable positions");
        }

        ClosePositionVO closePositionVO = getPositionDao().toClosePositionVO(position);

        // propagate the ClosePosition event
        EngineLocator.instance().sendEvent(position.getStrategy().getName(), closePositionVO);

        // remove the association
        position.getSecurity().removePositions(position);

        getPositionDao().remove(position);

        logger.info("deleted non-tradeable position " + position.getId() + " on " + security + " for strategy " + position.getStrategy().getName());

        // unsubscribe if necessary
        if (unsubscribe) {
            getMarketDataService().unsubscribe(position.getStrategy().getName(), position.getSecurity().getId());
        }
    }

    @Override
    protected Position handleSetExitValue(int positionId, BigDecimal exitValue, boolean force) throws Exception {

        Position position = getPositionDao().getLocked(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        // set the scale
        int scale = position.getSecurity().getSecurityFamily().getScale();
        exitValue = exitValue.setScale(scale, BigDecimal.ROUND_HALF_UP);

        // prevent exitValues near Zero
        if (!(position.getSecurityInitialized() instanceof Combination) && exitValue.doubleValue() <= 0.05) {
            logger.warn("setting of exitValue below 0.05 is prohibited: " + exitValue);
            return position;
        }

        // The new ExitValues should not be set lower (higher) than the existing ExitValue for long (short) positions. This check can be overwritten by setting force to true
        if (!force) {
            if (Direction.SHORT.equals(position.getDirection()) && position.getExitValue() != null && exitValue.compareTo(position.getExitValue()) > 0) {
                logger.warn("exit value " + exitValue + " is higher than existing exit value " + position.getExitValue() + " of short position " + positionId);
                return position;
            } else if (Direction.LONG.equals(position.getDirection()) && position.getExitValue() != null && exitValue.compareTo(position.getExitValue()) < 0) {
                logger.warn("exit value " + exitValue + " is lower than existing exit value " + position.getExitValue() + " of long position " + positionId);
                return position;
            }
        }

        // The new ExitValues cannot be higher (lower) than the currentValue for long (short) positions
        MarketDataEvent marketDataEvent = position.getSecurity().getCurrentMarketDataEvent();
        if (marketDataEvent != null) {
            BigDecimal currentValue = marketDataEvent.getCurrentValue();
            if (Direction.SHORT.equals(position.getDirection()) && exitValue.compareTo(currentValue) < 0) {
                throw new PositionServiceException("ExitValue (" + exitValue + ") for short-position " + position.getId() + " is lower than currentValue: " + currentValue);
            } else if (Direction.LONG.equals(position.getDirection()) && exitValue.compareTo(currentValue) > 0) {
                throw new PositionServiceException("ExitValue (" + exitValue + ") for long-position " + position.getId() + " is higher than currentValue: " + currentValue);
            }
        }

        // set the exitValue
        position.setExitValue(exitValue);

        logger.info("set exit value of position " + position.getId() + " to " + exitValue);

        return position;
    }

    @Override
    protected Position handleRemoveExitValue(int positionId) throws Exception {

        Position position = getPositionDao().getLocked(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        if (position.getExitValue() != null) {

            position.setExitValue(null);

            logger.info("removed exit value of position " + positionId);
        }

        return position;
    }

    @Override
    protected Position handleSetMargin(int positionId) throws Exception {

        Position position = getPositionDao().get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

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
    protected String handleResetPositions() throws Exception {

        Collection<Transaction> transactions = getTransactionDao().findAllTradesInclSecurity();

        // process all transactions to establis current position states
        Map<Security, Position> positionMap = new HashMap<Security, Position>();
        for (Transaction transaction : transactions) {

            // crate a position if we come across a security for the first time
            Position position = positionMap.get(transaction.getSecurity());
            if (position == null) {
                position = PositionUtil.processFirstTransaction(transaction);
                positionMap.put(position.getSecurity(), position);
            } else {
                PositionUtil.processTransaction(position, transaction);
            }
        }

        // update positions
        StringBuffer buffer = new StringBuffer();
        for (Position targetOpenPosition : positionMap.values()) {

            Position actualOpenPosition = getPositionDao().findBySecurityAndStrategy(targetOpenPosition.getSecurity().getId(), targetOpenPosition.getStrategy().getName());

            // create if it does not exist
            if (actualOpenPosition == null) {

                String warning = "position on security " + targetOpenPosition.getSecurity() + " strategy " + targetOpenPosition.getStrategy() + " quantity " + targetOpenPosition.getQuantity() + " does not exist";
                logger.warn(warning);
                buffer.append(warning + "\n");

            } else {

                // check quantity
                if (actualOpenPosition.getQuantity() != targetOpenPosition.getQuantity()) {

                    long existingQty = actualOpenPosition.getQuantity();
                    actualOpenPosition.setQuantity(targetOpenPosition.getQuantity());

                    String warning = "adjusted quantity of position " + actualOpenPosition.getId() + " from " + existingQty + " to " + targetOpenPosition.getQuantity();
                    logger.warn(warning);
                    buffer.append(warning + "\n");
                }

                // check cost
                if (actualOpenPosition.getCost() != targetOpenPosition.getCost()) {

                    double existingCost = actualOpenPosition.getCost();
                    actualOpenPosition.setCost(targetOpenPosition.getCost());

                    String warning = "adjusted cost of position " + actualOpenPosition.getId() + " from " + existingCost + " to " + targetOpenPosition.getCost();
                    logger.warn(warning);
                    buffer.append(warning + "\n");
                }

                // check realizedPL
                if (actualOpenPosition.getRealizedPL() != targetOpenPosition.getRealizedPL()) {

                    double existingRealizedPL = actualOpenPosition.getRealizedPL();
                    actualOpenPosition.setRealizedPL(targetOpenPosition.getRealizedPL());

                    String warning = "adjusted realizedPL of position " + actualOpenPosition.getId() + " from " + existingRealizedPL + " to " + targetOpenPosition.getRealizedPL();
                    logger.warn(warning);
                    buffer.append(warning + "\n");
                }
            }
        }

        return buffer.toString();
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

    private void expirePosition(Position position) throws Exception {

        Security security = position.getSecurityInitialized();

        ExpirePositionVO expirePositionEvent = getPositionDao().toExpirePositionVO(position);

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setDateTime(DateUtil.getCurrentEPTime());
        transaction.setType(TransactionType.EXPIRATION);
        transaction.setQuantity(-position.getQuantity());
        transaction.setSecurity(security);
        transaction.setStrategy(position.getStrategy());
        transaction.setCurrency(security.getSecurityFamily().getCurrency());

        if (security instanceof StockOption) {

            StockOption stockOption = (StockOption) security;
            int scale = security.getSecurityFamily().getScale();
            double underlyingSpot = security.getUnderlying().getCurrentMarketDataEvent().getCurrentValueDouble();
            double intrinsicValue = StockOptionUtil.getIntrinsicValue(stockOption, underlyingSpot);
            BigDecimal price = RoundUtil.getBigDecimal(intrinsicValue, scale);
            transaction.setPrice(price);

        } else if (security instanceof Future) {

            BigDecimal price = security.getUnderlying().getCurrentMarketDataEvent().getCurrentValue();
            transaction.setPrice(price);

        } else {
            throw new IllegalArgumentException("Expiration not allowed for " + security.getClass().getName());
        }

        // perisite the transaction
        getTransactionService().persistTransaction(transaction);

        // unsubscribe the security
        getMarketDataService().unsubscribe(position.getStrategy().getName(), security.getId());

        // propagate the ExpirePosition event
        EngineLocator.instance().sendEvent(position.getStrategy().getName(), expirePositionEvent);
    }
}
