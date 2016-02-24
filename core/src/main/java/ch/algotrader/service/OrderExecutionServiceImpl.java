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

import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.broker.StrategyTopicRouter;
import ch.algotrader.broker.SubscriptionTopic;
import ch.algotrader.broker.eviction.TopicEvictionVO;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.EventRecipient;
import ch.algotrader.ordermgmt.OrderBook;

/**
 * Internal order execution service intended to handle persistence and propagation
 * of various trading events such as order status update and order fills as well
 * as maintain order execution status in the order book.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class OrderExecutionServiceImpl implements OrderExecutionService {

    private static final Logger LOGGER = LogManager.getLogger(OrderExecutionServiceImpl.class);

    private final CommonConfig commonConfig;
    private final OrderPersistenceService orderPersistService;
    private final OrderBook orderBook;
    private final EventDispatcher eventDispatcher;
    private final Engine serverEngine;

    public OrderExecutionServiceImpl(final CommonConfig commonConfig,
                                     final OrderPersistenceService orderPersistService,
                                     final OrderBook orderBook,
                                     final EventDispatcher eventDispatcher,
                                     final EngineManager engineManager,
                                     final Engine serverEngine) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(orderPersistService, "OrderPersistStrategy is null");
        Validate.notNull(orderBook, "OpenOrderRegistry is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.orderPersistService = orderPersistService;
        this.orderBook = orderBook;
        this.eventDispatcher = eventDispatcher;
        this.serverEngine = serverEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOrderStatus(final OrderStatus orderStatus) {

        Validate.notNull(orderStatus, "Order status is null");

        String intId = orderStatus.getIntId();
        Order order = this.orderBook.getOpenOrderByIntId(intId);
        if (order == null) {
            throw new ServiceException("Open order with IntID " + intId + " not found");
        }

        this.orderBook.updateExecutionStatus(order.getIntId(), orderStatus.getExtId(), orderStatus.getStatus(),
                orderStatus.getFilledQuantity(), orderStatus.getRemainingQuantity());

        if (orderStatus.getDateTime() == null) {
            if (orderStatus.getExtDateTime() != null) {
                orderStatus.setDateTime(orderStatus.getExtDateTime());
            } else {
                orderStatus.setDateTime(this.serverEngine.getCurrentTime());
            }
        }

        // send the fill to the strategy that placed the corresponding order
        Strategy strategy = order.getStrategy();
        this.eventDispatcher.sendEvent(strategy.getName(), orderStatus.convertToVO());

        if (!this.commonConfig.isSimulation()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("propagated orderStatus: {}", orderStatus);
            }
            // and ignore order status message with synthetic (non-positive) sequence number
            if (orderStatus.getSequenceNumber() > 0) {

                this.orderPersistService.persistOrderStatus(orderStatus);
            }
        }

        switch (orderStatus.getStatus()) {
            case EXECUTED:
            case REJECTED:
            case CANCELED:
                Optional<String> strategyName = Optional.of(strategy.getName());
                TopicEvictionVO eviction1 = new TopicEvictionVO(StrategyTopicRouter.create(SubscriptionTopic.ORDER.getBaseTopic(), strategyName, intId));
                this.eventDispatcher.broadcast(eviction1, EventRecipient.REMOTE_ONLY);
                TopicEvictionVO eviction2 = new TopicEvictionVO(StrategyTopicRouter.create(SubscriptionTopic.ORDER_STATUS.getBaseTopic(), strategyName, intId));
                this.eventDispatcher.broadcast(eviction2, EventRecipient.REMOTE_ONLY);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleFill(final Fill fill) {

        Validate.notNull(fill, "Fill is null");

        Order order = fill.getOrder();
        // send the fill to the strategy that placed the corresponding order
        Strategy strategy = order.getStrategy();
        this.eventDispatcher.sendEvent(strategy.getName(), fill.convertToVO());

        if (!this.commonConfig.isSimulation()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("received fill {} for order {}", fill, order);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleFill(final ExternalFill fill) {

        Validate.notNull(fill, "ExternalFill is null");

        if (!this.commonConfig.isSimulation()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("received external fill {}", fill);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleOrderCompletion(final OrderCompletionVO orderCompletion) {

        Validate.notNull(orderCompletion, "OrderCompletionVO is null");

        this.eventDispatcher.sendEvent(orderCompletion.getStrategy(), orderCompletion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleRestatedOrder(final Order initialOrder, final Order restatedOrder) {

        Validate.notNull(initialOrder, "Order is null");
        Validate.notNull(restatedOrder, "Order is null");

        String previousIntId = initialOrder.getIntId();
        if (previousIntId != null) {
            this.orderBook.remove(previousIntId);
        }
        this.orderBook.add(restatedOrder);

        if (!this.commonConfig.isSimulation()) {

            this.orderPersistService.persistOrder(restatedOrder);
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("restated order {}", restatedOrder);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String lookupIntId(final String extId) {

        return this.orderBook.lookupIntId(extId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderDetailsVO getOpenOrderDetailsByIntId(final String intId) {

        return this.orderBook.getOpenOrderDetailsByIntId(intId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderStatusVO getStatusByIntId(final String intId) {

        Validate.notEmpty(intId, "Order IntId is empty");

        return this.orderBook.getStatusByIntId(intId);
    }

    @Override
    public Order getOpenOrderByIntId(final String intId) {

        Validate.notEmpty(intId, "Order IntId is empty");

        return this.orderBook.getOpenOrderByIntId(intId);
    }

    @Override
    public Order getOrderByIntId(String intId) {

        Validate.notEmpty(intId, "Order IntId is empty");

        return this.orderBook.getByIntId(intId);
    }
}
