/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderBook;

/**
 * Internal Algo order service intended to initiate algo order operations
 * such as submission of a new order, modification or cancellation of
 * an existing order, order validation, as well as handle child order status
 * updates and fills.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class AlgoOrderServiceImpl implements AlgoOrderService {

    private static final Logger LOGGER = LogManager.getLogger(AlgoOrderServiceImpl.class);

    private final static AtomicLong ORDER_COUNT = new AtomicLong(0);

    private final SimpleOrderService simpleOrderService;

    private final MarketDataCacheService marketDataCacheService;

    private final OrderBook orderBook;

    private final EventDispatcher eventDispatcher;

    private final Engine serverEngine;

    public AlgoOrderServiceImpl(
            final SimpleOrderService simpleOrderService,
            final MarketDataCacheService marketDataCacheService,
            final OrderBook orderBook,
            final EventDispatcher eventDispatcher,
            final Engine serverEngine) {

        Validate.notNull(simpleOrderService, "ServerOrderService is null");
        Validate.notNull(marketDataCacheService, "MarketDataCache is null");
        Validate.notNull(orderBook, "OpenOrderRegistry is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.simpleOrderService = simpleOrderService;
        this.marketDataCacheService = marketDataCacheService;
        this.orderBook = orderBook;
        this.eventDispatcher = eventDispatcher;
        this.serverEngine = serverEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateOrder(final AlgoOrder order) throws OrderValidationException {

        Validate.notNull(order, "Order is null");

        // validate general properties
        if (order.getSide() == null) {
            throw new OrderValidationException("Missing order side: " + order);
        }
        if (order.getQuantity() <= 0) {
            throw new OrderValidationException("Order quantity cannot be zero or negative: " + order);
        }

        // validate order specific properties
        order.validate();

        // check that the security is tradeable
        Security security = order.getSecurity();
        if (!security.getSecurityFamily().isTradeable()) {
            throw new OrderValidationException(security + " is not tradeable: " + order);
        }

        MarketDataEventVO marketDataEvent = this.marketDataCacheService.getCurrentMarketDataEvent(security.getId());
        if (marketDataEvent == null) {
            throw new OrderValidationException("no marketDataEvent available to initialize SlicingOrder");
        } else if (!(marketDataEvent instanceof TickVO)) {
            throw new OrderValidationException("only ticks are supported, " + marketDataEvent.getClass() + " are not supported");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOrder(final AlgoOrder order) {

        Validate.notNull(order, "Order is null");

        // validate the order before sending it
        try {
            validateOrder(order);
        } catch (OrderValidationException ex) {
            throw new ServiceException(ex);
        }

        if (order.getDateTime() == null) {
            order.setDateTime(this.serverEngine.getCurrentTime());
        }
        order.setIntId(getNextOrderId(order.getAccount()));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("send algo order: {}", order);
        }

        this.orderBook.add(order);

        this.serverEngine.sendEvent(order);
    }

    @Override
    public void modifyOrder(final AlgoOrder order) {

        throw new ServiceException("Modification of algo orders not supported");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOrder(final AlgoOrder order) {

        Validate.notNull(order, "Order is null");

        String algoOrderId = order.getIntId();
        ExecutionStatusVO executionStatus = this.orderBook.getStatusByIntId(algoOrderId);
        if (executionStatus != null) {
            OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
            algoOrderStatus.setIntId(algoOrderId);
            algoOrderStatus.setStatus(Status.CANCELED);
            algoOrderStatus.setFilledQuantity(executionStatus.getFilledQuantity());
            algoOrderStatus.setRemainingQuantity(executionStatus.getRemainingQuantity());
            algoOrderStatus.setOrder(order);

            this.orderBook.updateExecutionStatus(algoOrderId, null, algoOrderStatus.getStatus(),
                    algoOrderStatus.getFilledQuantity(), algoOrderStatus.getRemainingQuantity());

            propagateOrderStatus(algoOrderStatus);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("cancelled algo order: {}", order);
            }
        }

        Collection<Order> openOrders = this.orderBook.getOpenOrdersByParentIntId(order.getIntId());
        for (Order childOrder: openOrders) {
            if (childOrder instanceof SimpleOrder) {
                this.simpleOrderService.cancelOrder((SimpleOrder) childOrder);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextOrderId(final Account account) {
        return "a" + Long.toString(ORDER_COUNT.incrementAndGet());
    }

    private void propagateOrderStatus(final OrderStatus orderStatus) {

        // send the order into the AlgoTrader Server engine to be correlated with fills
        this.serverEngine.sendEvent(orderStatus);

        // also send the order to the strategy that placed the order
        Order order = orderStatus.getOrder();
        Strategy strategy = order.getStrategy();
        if (!strategy.isServer()) {

            this.eventDispatcher.sendEvent(strategy.getName(), orderStatus.convertToVO());
        }
    }

}
