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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.algo.AlgoOrderExecService;

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

    private final CommonConfig commonConfig;

    private final SimpleOrderService simpleOrderService;

    private final OrderBook orderBook;

    private final EventDispatcher eventDispatcher;

    private final Engine serverEngine;

    private final Map<Class<? extends AlgoOrder>, AlgoOrderExecService> algoExecServiceMap;

    public AlgoOrderServiceImpl(
            final CommonConfig commonConfig,
            final SimpleOrderService simpleOrderService,
            final OrderBook orderBook,
            final EventDispatcher eventDispatcher,
            final Engine serverEngine,
            final Map<Class<? extends AlgoOrder>, AlgoOrderExecService> algoExecServiceMap) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(simpleOrderService, "ServerOrderService is null");
        Validate.notNull(orderBook, "OpenOrderRegistry is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.simpleOrderService = simpleOrderService;
        this.orderBook = orderBook;
        this.eventDispatcher = eventDispatcher;
        this.serverEngine = serverEngine;
        this.algoExecServiceMap = new ConcurrentHashMap<>(algoExecServiceMap);
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

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(order.getClass());
        algoOrderExecService.validateOrder(order);
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

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(order.getClass());
        algoOrderExecService.sendOrder(order);

    }

    @Override
    public void modifyOrder(final AlgoOrder order) {

        // Todo modify order

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(order.getClass());
        algoOrderExecService.modifyOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOrder(final AlgoOrder order) {

        Validate.notNull(order, "Order is null");

        String algoOrderId = order.getIntId();
        OrderStatusVO executionStatus = this.orderBook.getStatusByIntId(algoOrderId);
        if (executionStatus != null) {
            OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
            algoOrderStatus.setIntId(algoOrderId);
            algoOrderStatus.setStatus(Status.CANCELED);
            algoOrderStatus.setFilledQuantity(executionStatus.getFilledQuantity());
            algoOrderStatus.setRemainingQuantity(executionStatus.getRemainingQuantity());
            algoOrderStatus.setOrder(order);

            this.orderBook.updateExecutionStatus(algoOrderId, null, algoOrderStatus.getStatus(),
                    algoOrderStatus.getFilledQuantity(), algoOrderStatus.getRemainingQuantity());

            // send the order into the AlgoTrader Server engine to be correlated with fills
            this.serverEngine.sendEvent(algoOrderStatus);

            // also send the order to the strategy that placed the order
            Strategy strategy = order.getStrategy();
            if (!strategy.isServer()) {

                this.eventDispatcher.sendEvent(strategy.getName(), algoOrderStatus.convertToVO());
            }

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

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(order.getClass());
        algoOrderExecService.cancelOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextOrderId(final Account account) {
        return "a" + Long.toString(ORDER_COUNT.incrementAndGet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleChildOrderStatus(final OrderStatus orderStatus) {

        Order order = orderStatus.getOrder();
        if (order.getParentOrder() instanceof AlgoOrder && orderStatus.getStatus() == Status.SUBMITTED) {

            AlgoOrder algoOrder = (AlgoOrder) order.getParentOrder();
            String algoOrderId = algoOrder.getIntId();
            OrderStatusVO execStatus = this.orderBook.getStatusByIntId(algoOrderId);
            if (execStatus != null && execStatus.getStatus() == Status.OPEN) {
                OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
                algoOrderStatus.setStatus(Status.SUBMITTED);
                algoOrderStatus.setIntId(algoOrderId);
                algoOrderStatus.setExtDateTime(this.serverEngine.getCurrentTime());
                algoOrderStatus.setDateTime(algoOrderStatus.getExtDateTime());
                algoOrderStatus.setFilledQuantity(0L);
                algoOrderStatus.setRemainingQuantity(execStatus.getRemainingQuantity());
                algoOrderStatus.setOrder(algoOrder);

                this.orderBook.updateExecutionStatus(algoOrderId, null, algoOrderStatus.getStatus(), algoOrderStatus.getFilledQuantity(), algoOrderStatus.getRemainingQuantity());

                Strategy strategy = order.getStrategy();

                this.serverEngine.sendEvent(algoOrderStatus);
                this.eventDispatcher.sendEvent(strategy.getName(), algoOrderStatus.convertToVO());

                AlgoOrderExecService algoOrderExecService = getAlgoExecService(algoOrder.getClass());
                algoOrderExecService.handleOrderStatus(algoOrderStatus);

                if (!this.commonConfig.isSimulation()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("propagated orderStatus: {}", algoOrderStatus);
                    }
                }
            }
        }
    }

    @Override
    public void handleChildFill(final Fill fill) {

        Order order = fill.getOrder();
        if (order.getParentOrder() instanceof AlgoOrder) {

            AlgoOrder algoOrder = (AlgoOrder) order.getParentOrder();
            String algoOrderId = algoOrder.getIntId();
            OrderStatusVO execStatus = this.orderBook.getStatusByIntId(algoOrderId);
            if (execStatus != null) {
                OrderStatus orderStatus = OrderStatus.Factory.newInstance();
                orderStatus.setStatus(execStatus.getRemainingQuantity() - fill.getQuantity() > 0 ? Status.PARTIALLY_EXECUTED : Status.EXECUTED);
                orderStatus.setIntId(algoOrderId);
                orderStatus.setExtDateTime(this.serverEngine.getCurrentTime());
                orderStatus.setDateTime(orderStatus.getExtDateTime());
                orderStatus.setFilledQuantity(execStatus.getFilledQuantity() + fill.getQuantity());
                orderStatus.setRemainingQuantity(execStatus.getRemainingQuantity() - fill.getQuantity());
                orderStatus.setOrder(algoOrder);

                this.orderBook.updateExecutionStatus(algoOrderId, null, orderStatus.getStatus(),
                        orderStatus.getFilledQuantity(), orderStatus.getRemainingQuantity());

                Strategy strategy = order.getStrategy();

                this.serverEngine.sendEvent(orderStatus);
                this.eventDispatcher.sendEvent(strategy.getName(), orderStatus.convertToVO());

                AlgoOrderExecService algoOrderExecService = getAlgoExecService(algoOrder.getClass());
                algoOrderExecService.handleOrderStatus(orderStatus);

                if (!this.commonConfig.isSimulation()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("propagated orderStatus: {}", orderStatus);
                    }
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private AlgoOrderExecService<AlgoOrder> getAlgoExecService(final Class<? extends AlgoOrder> clazz) {

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = this.algoExecServiceMap.get(clazz);
        if (algoOrderExecService == null) {
            throw new ServiceException("Unsupported algo order class: " + clazz);
        }
        return algoOrderExecService;
    }
}
