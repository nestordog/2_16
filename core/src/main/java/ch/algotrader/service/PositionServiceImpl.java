/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
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

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.entity.ClosePositionVOProducer;
import ch.algotrader.entity.ExpirePositionVOProducer;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.PositionDao;
import ch.algotrader.entity.Transaction;
import ch.algotrader.entity.TransactionDao;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TransactionType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.esper.callback.TradeCallback;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.option.OptionUtil;
import ch.algotrader.util.PositionUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.util.collection.Pair;
import ch.algotrader.vo.ClosePositionVO;
import ch.algotrader.vo.ExpirePositionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class PositionServiceImpl implements PositionService {

    private static Logger logger = LogManager.getLogger(PositionServiceImpl.class.getName());

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final TransactionService transactionService;

    private final MarketDataService marketDataService;

    private final OrderService orderService;

    private final PortfolioService portfolioService;

    private final LocalLookupService localLookupService;

    private final PositionDao positionDao;

    private final SecurityDao securityDao;

    private final StrategyDao strategyDao;

    private final TransactionDao transactionDao;

    private final EventDispatcher eventDispatcher;

    private final EngineManager engineManager;

    private final Engine serverEngine;

    public PositionServiceImpl(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final TransactionService transactionService,
            final MarketDataService marketDataService,
            final OrderService orderService,
            final PortfolioService portfolioService,
            final LocalLookupService localLookupService,
            final PositionDao positionDao,
            final SecurityDao securityDao,
            final StrategyDao strategyDao,
            final TransactionDao transactionDao,
            final EventDispatcher eventDispatcher,
            final EngineManager engineManager,
            final Engine serverEngine) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(transactionService, "TransactionService is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(orderService, "OrderService is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(localLookupService, "LocalLookupService is null");
        Validate.notNull(positionDao, "PositionDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(transactionDao, "TransactionDao is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.transactionService = transactionService;
        this.marketDataService = marketDataService;
        this.orderService = orderService;
        this.portfolioService = portfolioService;
        this.localLookupService = localLookupService;
        this.positionDao = positionDao;
        this.securityDao = securityDao;
        this.strategyDao = strategyDao;
        this.transactionDao = transactionDao;
        this.eventDispatcher = eventDispatcher;
        this.engineManager = engineManager;
        this.serverEngine = serverEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeAllPositionsByStrategy(final String strategyName, final boolean unsubscribe) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        for (Position position : this.positionDao.findOpenPositionsByStrategy(strategyName)) {
            if (position.isOpen()) {
                closePosition(position.getId(), unsubscribe);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closePosition(final int positionId, final boolean unsubscribe) {

        final Position position = this.positionDao.get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        Security security = position.getSecurity();

        if (position.isOpen()) {

            // handle Combinations by the combination service
            if (security instanceof Combination) {
                throw new PositionServiceException("Cannot close Combination position");
            } else {
                reduceOrClosePosition(position, position.getQuantity(), unsubscribe);
            }

        } else {

            // if there was no open position but unsubscribe was requested do that anyway
            if (unsubscribe) {
                this.marketDataService.unsubscribe(position.getStrategy().getName(), security.getId());
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Position createNonTradeablePosition(final String strategyName, final int securityId, final long quantity) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        Security security = this.securityDao.get(securityId);
        Strategy strategy = this.strategyDao.findByName(strategyName);

        if (security.getSecurityFamily().isTradeable()) {
            throw new PositionServiceException(security + " is tradeable, can only creat non-tradeable positions");
        }

        Position position = Position.Factory.newInstance();
        position.setQuantity(quantity);

        position.setExitValue(null);
        position.setMaintenanceMargin(null);

        // associate strategy and security
        position.setStrategy(strategy);
        position.setSecurity(security);

        this.positionDao.save(position);

        // reverse-associate the security (after position has received an id)
        security.getPositions().add(position);

        logger.info("created non-tradeable position on " + security + " for strategy " + strategyName + " quantity " + quantity);

        return position;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Position modifyNonTradeablePosition(final int positionId, final long quantity) {

        Position position = this.positionDao.getLocked(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        position.setQuantity(quantity);

        logger.info("modified non-tradeable position " + positionId + " new quantity " + quantity);

        return position;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteNonTradeablePosition(final int positionId, final boolean unsubscribe) {

        Position position = this.positionDao.get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        Security security = position.getSecurity();

        if (security.getSecurityFamily().isTradeable()) {
            throw new PositionServiceException(security + " is tradeable, can only delete non-tradeable positions");
        }

        ClosePositionVO closePositionVO = ClosePositionVOProducer.INSTANCE.convert(position);

        // propagate the ClosePosition event
        this.eventDispatcher.sendEvent(position.getStrategy().getName(), closePositionVO);

        // remove the association
        position.getSecurity().removePositions(position);

        this.positionDao.delete(position);

        logger.info("deleted non-tradeable position " + position.getId() + " on " + security + " for strategy " + position.getStrategy().getName());

        // unsubscribe if necessary
        if (unsubscribe) {
            this.marketDataService.unsubscribe(position.getStrategy().getName(), position.getSecurity().getId());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reducePosition(final int positionId, final long quantity) {

        Position position = this.positionDao.get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        if (Math.abs(quantity) > Math.abs(position.getQuantity())) {
            throw new PositionServiceException("position reduction of " + quantity + " for position " + position.getId() + " is greater than current quantity " + position.getQuantity());
        } else {
            reduceOrClosePosition(position, quantity, false);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transferPosition(final int positionId, final String targetStrategyName) {

        Validate.notEmpty(targetStrategyName, "Target strategy name is empty");

        Position position = this.positionDao.get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        Strategy targetStrategy = this.strategyDao.findByName(targetStrategyName);
        Security security = position.getSecurity();
        SecurityFamily family = security.getSecurityFamily();
        MarketDataEvent marketDataEvent = this.localLookupService.getCurrentMarketDataEvent(security.getId());
        BigDecimal price = RoundUtil.getBigDecimal(position.getMarketPrice(marketDataEvent), family.getScale());

        // debit transaction
        Transaction debitTransaction = Transaction.Factory.newInstance();
        debitTransaction.setDateTime(this.engineManager.getCurrentEPTime());
        debitTransaction.setQuantity(-position.getQuantity());
        debitTransaction.setPrice(price);
        debitTransaction.setCurrency(family.getCurrency());
        debitTransaction.setType(TransactionType.TRANSFER);
        debitTransaction.setSecurity(security);
        debitTransaction.setStrategy(position.getStrategy());

        // persiste the transaction
        this.transactionService.persistTransaction(debitTransaction);

        // credit transaction
        Transaction creditTransaction = Transaction.Factory.newInstance();
        creditTransaction.setDateTime(this.engineManager.getCurrentEPTime());
        creditTransaction.setQuantity(position.getQuantity());
        creditTransaction.setPrice(price);
        creditTransaction.setCurrency(family.getCurrency());
        creditTransaction.setType(TransactionType.TRANSFER);
        creditTransaction.setSecurity(security);
        creditTransaction.setStrategy(targetStrategy);

        // persiste the transaction
        this.transactionService.persistTransaction(creditTransaction);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void setMargins() {

        List<Position> positions = this.positionDao.findOpenPositions();

        for (Position position : positions) {
            setMargin(position);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Position setMargin(final int positionId) {

        Position position = this.positionDao.get(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        setMargin(position);

        return position;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void expirePositions() {

        Date date = this.engineManager.getCurrentEPTime();
        Collection<Position> positions = this.positionDao.findExpirablePositions(date);

        for (Position position : positions) {
            expirePosition(position);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Position setExitValue(final int positionId, final BigDecimal exitValue, final boolean force) {

        Validate.notNull(exitValue, "Exit value is null");

        BigDecimal exitValueNonFinal = exitValue;
        Position position = this.positionDao.getLocked(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        // set the scale
        int scale = position.getSecurity().getSecurityFamily().getScale();
        exitValueNonFinal = exitValueNonFinal.setScale(scale, BigDecimal.ROUND_HALF_UP);

        // prevent exitValues near Zero
        if (!(position.getSecurity() instanceof Combination) && exitValueNonFinal.doubleValue() <= 0.05) {
            logger.warn("setting of exitValue below 0.05 is prohibited: " + exitValueNonFinal);
            return position;
        }

        // The new ExitValues should not be set lower (higher) than the existing ExitValue for long (short) positions. This check can be overwritten by setting force to true
        if (!force) {
            if (Direction.SHORT.equals(position.getDirection()) && position.getExitValue() != null && exitValueNonFinal.compareTo(position.getExitValue()) > 0) {
                logger.warn("exit value " + exitValueNonFinal + " is higher than existing exit value " + position.getExitValue() + " of short position " + positionId);
                return position;
            } else if (Direction.LONG.equals(position.getDirection()) && position.getExitValue() != null && exitValueNonFinal.compareTo(position.getExitValue()) < 0) {
                logger.warn("exit value " + exitValueNonFinal + " is lower than existing exit value " + position.getExitValue() + " of long position " + positionId);
                return position;
            }
        }

        // The new ExitValues cannot be higher (lower) than the currentValue for long (short) positions
        MarketDataEvent marketDataEvent = this.localLookupService.getCurrentMarketDataEvent(position.getSecurity().getId());
        if (marketDataEvent != null) {
            BigDecimal currentValue = marketDataEvent.getCurrentValue();
            if (Direction.SHORT.equals(position.getDirection()) && exitValueNonFinal.compareTo(currentValue) < 0) {
                throw new PositionServiceException("ExitValue (" + exitValueNonFinal + ") for short-position " + position.getId() + " is lower than currentValue: " + currentValue);
            } else if (Direction.LONG.equals(position.getDirection()) && exitValueNonFinal.compareTo(currentValue) > 0) {
                throw new PositionServiceException("ExitValue (" + exitValueNonFinal + ") for long-position " + position.getId() + " is higher than currentValue: " + currentValue);
            }
        }

        // set the exitValue
        position.setExitValue(exitValueNonFinal);

        logger.info("set exit value of position " + position.getId() + " to " + exitValueNonFinal);

        return position;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Position removeExitValue(final int positionId) {

        Position position = this.positionDao.getLocked(positionId);
        if (position == null) {
            throw new IllegalArgumentException("position with id " + positionId + " does not exist");
        }

        if (position.getExitValue() != null) {

            position.setExitValue(null);

            logger.info("removed exit value of position " + positionId);
        }

        return position;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String resetPositions() {

        Collection<Transaction> transactions = this.transactionDao.findAllTradesInclSecurity();

        // process all transactions to establis current position states
        Map<Pair<Security, Strategy>, Position> positionMap = new HashMap<Pair<Security, Strategy>, Position>();
        for (Transaction transaction : transactions) {

            // crate a position if we come across a security for the first time
            Position position = positionMap.get(new Pair<Security, Strategy>(transaction.getSecurity(), transaction.getStrategy()));
            if (position == null) {
                position = PositionUtil.processFirstTransaction(transaction);
                positionMap.put(new Pair<Security, Strategy>(position.getSecurity(), position.getStrategy()), position);
            } else {
                PositionUtil.processTransaction(position, transaction);
            }
        }

        // update positions
        StringBuffer buffer = new StringBuffer();
        for (Position targetOpenPosition : positionMap.values()) {

            Position actualOpenPosition = this.positionDao.findBySecurityAndStrategy(targetOpenPosition.getSecurity().getId(), targetOpenPosition.getStrategy().getName());

            // create if it does not exist
            if (actualOpenPosition == null) {

                String warning = "position on security " + targetOpenPosition.getSecurity() + " strategy " + targetOpenPosition.getStrategy() + " quantity " + targetOpenPosition.getQuantity()
                        + " does not exist";
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

    private void reduceOrClosePosition(final Position position, long quantity, final boolean unsubscribe) {

        Strategy strategy = position.getStrategy();
        Security security = position.getSecurity();

        Side side = (position.getQuantity() > 0) ? Side.SELL : Side.BUY;

        Order order = this.orderService.createOrderByOrderPreference(this.coreConfig.getDefaultOrderPreference());

        order.setStrategy(strategy);
        order.setSecurity(security);
        order.setQuantity(Math.abs(quantity));
        order.setSide(side);

        // unsubscribe is requested / notify non-full executions in live-trading
        if (this.commonConfig.isSimulation()) {
            if (unsubscribe) {
                this.marketDataService.unsubscribe(order.getStrategy().getName(), order.getSecurity().getId());
            }
        } else {
            this.serverEngine.addTradeCallback(Collections.singleton(order), new TradeCallback(true) {
                @Override
                public void onTradeCompleted(List<OrderStatus> orderStati) throws Exception {
                    if (unsubscribe) {
                        for (OrderStatus orderStatus : orderStati) {
                            Order order = orderStatus.getOrder();
                            if (Status.EXECUTED.equals(orderStatus.getStatus())) {
                                // use ServiceLocator because TradeCallback is executed in a new thread
                                PositionServiceImpl.this.marketDataService.unsubscribe(order.getStrategy().getName(), order.getSecurity().getId());
                            }
                        }
                    }
                }
            });
        }

        this.orderService.sendOrder(order);
    }

    private void setMargin(Position position) {

        Security security = position.getSecurity();
        double currentValue = this.localLookupService.getCurrentValueDouble(security.getId());
        double underlyingCurrentValue = this.localLookupService.getCurrentValueDouble(security.getUnderlying().getId());
        double marginPerContract = security.getMargin(currentValue, underlyingCurrentValue);

        if (marginPerContract != 0) {

            long numberOfContracts = Math.abs(position.getQuantity());
            BigDecimal totalMargin = RoundUtil.getBigDecimal(marginPerContract * numberOfContracts);

            position.setMaintenanceMargin(totalMargin);

            double maintenanceMargin = this.portfolioService.getMaintenanceMarginDouble(position.getStrategy().getName());

            logger.debug("set margin of position " + position.getId() + " to: " + RoundUtil.getBigDecimal(marginPerContract) + " total margin: " + RoundUtil.getBigDecimal(maintenanceMargin));
        }
    }

    private void expirePosition(Position position) {

        Security security = position.getSecurity();

        ExpirePositionVO expirePositionEvent = ExpirePositionVOProducer.INSTANCE.convert(position);

        Transaction transaction = Transaction.Factory.newInstance();
        transaction.setDateTime(this.engineManager.getCurrentEPTime());
        transaction.setType(TransactionType.EXPIRATION);
        transaction.setQuantity(-position.getQuantity());
        transaction.setSecurity(security);
        transaction.setStrategy(position.getStrategy());
        transaction.setCurrency(security.getSecurityFamily().getCurrency());

        if (security instanceof Option) {

            Option option = (Option) security;
            int scale = security.getSecurityFamily().getScale();
            double underlyingSpot = this.localLookupService.getCurrentValueDouble(security.getUnderlying().getId());
            double intrinsicValue = OptionUtil.getIntrinsicValue(option, underlyingSpot);
            BigDecimal price = RoundUtil.getBigDecimal(intrinsicValue, scale);
            transaction.setPrice(price);

        } else if (security instanceof Future) {

            BigDecimal price = this.localLookupService.getCurrentValue(security.getUnderlying().getId());
            transaction.setPrice(price);

        } else {
            throw new IllegalArgumentException("Expiration not allowed for " + security.getClass().getName());
        }

        // perisite the transaction
        this.transactionService.persistTransaction(transaction);

        // unsubscribe the security
        this.marketDataService.unsubscribe(position.getStrategy().getName(), security.getId());

        // propagate the ExpirePosition event
        this.eventDispatcher.sendEvent(position.getStrategy().getName(), expirePositionEvent);
    }
}
