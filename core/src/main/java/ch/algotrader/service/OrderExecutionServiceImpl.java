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

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.ExternalFill;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletionVO;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderRegistry;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class OrderExecutionServiceImpl implements OrderExecutionService {

    private static final Logger LOGGER = LogManager.getLogger(OrderExecutionServiceImpl.class);

    private final CommonConfig commonConfig;
    private final OrderPersistenceService orderPersistService;
    private final OrderRegistry orderRegistry;
    private final EventDispatcher eventDispatcher;
    private final Engine serverEngine;

    public OrderExecutionServiceImpl(final CommonConfig commonConfig,
                                     final OrderPersistenceService orderPersistService,
                                     final OrderRegistry orderRegistry,
                                     final EventDispatcher eventDispatcher,
                                     final EngineManager engineManager,
                                     final Engine serverEngine) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(orderPersistService, "OrderPersistStrategy is null");
        Validate.notNull(orderRegistry, "OpenOrderRegistry is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.orderPersistService = orderPersistService;
        this.orderRegistry = orderRegistry;
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
        Order order = this.orderRegistry.getOpenOrderByIntId(intId);
        if (order == null) {
            throw new ServiceException("Open order with IntID " + intId + " not found");
        }

        this.orderRegistry.updateExecutionStatus(order.getIntId(), orderStatus.getExtId(), orderStatus.getStatus(),
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
        if (!strategy.isServer()) {

            this.eventDispatcher.sendEvent(strategy.getName(), orderStatus.convertToVO());
        }

        if (!this.commonConfig.isSimulation()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("propagated orderStatus: {}", orderStatus);
            }
            // only store OrderStatus for non AlgoOrders
            // and ignore order status message with synthetic (non-positive) sequence number
            if (orderStatus.getSequenceNumber() > 0 && !(order instanceof AlgoOrder)) {

                this.orderPersistService.persistOrderStatus(orderStatus);
            }
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
        if (!strategy.isServer()) {
            this.eventDispatcher.sendEvent(strategy.getName(), fill.convertToVO());
        }

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

        if (!StrategyImpl.SERVER.equalsIgnoreCase(orderCompletion.getStrategy())) {
            this.eventDispatcher.sendEvent(orderCompletion.getStrategy(), orderCompletion);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String lookupIntId(final String extId) {

        return this.orderRegistry.lookupIntId(extId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderDetailsVO getOpenOrderDetailsByIntId(final String intId) {

        return this.orderRegistry.getOpenOrderDetailsByIntId(intId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecutionStatusVO getStatusByIntId(final String intId) {

        Validate.notEmpty(intId, "Order IntId is empty");

        return this.orderRegistry.getStatusByIntId(intId);
    }

    @Override
    public Order getOpenOrderByIntId(final String intId) {

        Validate.notEmpty(intId, "Order IntId is empty");

        return this.orderRegistry.getOpenOrderByIntId(intId);
    }

}
