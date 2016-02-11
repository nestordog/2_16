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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.esper.Engine;
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

    private final OrderBook orderBook;

    private final Engine serverEngine;

    private final Map<Class<? extends AlgoOrder>, AlgoOrderExecService> algoExecServiceMap;

    public AlgoOrderServiceImpl(
            final OrderBook orderBook,
            final Engine serverEngine,
            final Map<Class<? extends AlgoOrder>, AlgoOrderExecService> algoExecServiceMap) {

        Validate.notNull(orderBook, "OpenOrderRegistry is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.orderBook = orderBook;
        this.serverEngine = serverEngine;
        this.algoExecServiceMap = new ConcurrentHashMap<>(algoExecServiceMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateOrder(final AlgoOrder order) throws OrderValidationException {

        Validate.notNull(order, "Order is null");

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(order.getClass());
        algoOrderExecService.validateOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendOrder(final AlgoOrder order) {

        Validate.notNull(order, "Order is null");

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(order.getClass());

        String intId = order.getIntId();
        if (intId == null) {
            intId = getNextOrderId(order.getAccount());
            order.setIntId(intId);
        }
        // validate the order before sending it
        try {
            algoOrderExecService.validateOrder(order);
        } catch (OrderValidationException ex) {
            throw new ServiceException(ex);
        }

        if (order.getDateTime() == null) {
            order.setDateTime(this.serverEngine.getCurrentTime());
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("send algo order: {}", order);
        }

        this.orderBook.add(order);

        this.serverEngine.sendEvent(order);

        algoOrderExecService.sendOrder(order);

        return intId;
    }

    @Override
    public String modifyOrder(final AlgoOrder order) {

        String intId = order.getIntId();
        Order existingOrder = this.orderBook.getOpenOrderByIntId(intId);
        if (existingOrder == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("algo order {} is not open", intId);
            }
            return null;
        }
        if (!Objects.equals(order.getAccount(), existingOrder.getAccount())) {
            throw new ServiceException("Algo order account of cannot be modified");
        }
        if (!Objects.equals(order.getSecurity(), existingOrder.getSecurity())) {
            throw new ServiceException("Algo order security cannot be modified");
        }
        if (!Objects.equals(order.getStrategy(), existingOrder.getStrategy())) {
            throw new ServiceException("Algo order strategy cannot be modified");
        }
        if (!Objects.equals(order.getExchange(), existingOrder.getExchange())) {
            throw new ServiceException("Algo order exchange cannot be modified");
        }

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("modify algo order: {}", order);
        }

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(order.getClass());
        algoOrderExecService.modifyOrder(order);

        this.orderBook.replace(order);

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String cancelOrder(final AlgoOrder order) {

        Validate.notNull(order, "Order is null");

        AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(order.getClass());
        algoOrderExecService.cancelOrder(order);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("cancelled algo order: {}", order);
        }

        return null;
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
        if (order.getParentOrder() instanceof AlgoOrder) {

            AlgoOrder algoOrder = (AlgoOrder) order.getParentOrder();
            AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(algoOrder.getClass());
            algoOrderExecService.onChildOrderStatus(algoOrder, orderStatus);
        }

    }

    @Override
    public void handleChildFill(final Fill fill) {

        Order order = fill.getOrder();
        if (order.getParentOrder() instanceof AlgoOrder) {

            AlgoOrder algoOrder = (AlgoOrder) order.getParentOrder();
            AlgoOrderExecService<AlgoOrder> algoOrderExecService = getAlgoExecService(algoOrder.getClass());
            algoOrderExecService.onChildFill(algoOrder, fill);
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
