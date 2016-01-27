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
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.trade.LimitOrderDao;
import ch.algotrader.dao.trade.MarketOrderDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderPropertyDao;
import ch.algotrader.dao.trade.OrderStatusDao;
import ch.algotrader.dao.trade.StopLimitOrderDao;
import ch.algotrader.dao.trade.StopOrderDao;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderProperty;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;

/**
 * {@link OrderPersistenceService} implementation that directly
 * commits orders and order events to a persistent store.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class OrderPersistenceServiceImpl implements OrderPersistenceService {

    private static final int MAX_REASON_LEN = 255;

    private static final Logger LOGGER = LogManager.getLogger(OrderPersistenceServiceImpl.class);

    private final OrderDao orderDao;

    private final MarketOrderDao marketOrderDao;

    private final LimitOrderDao limitOrderDao;

    private final StopOrderDao stopOrderDao;

    private final StopLimitOrderDao stopLimitOrderDao;

    private final OrderPropertyDao orderPropertyDao;

    private final OrderStatusDao orderStatusDao;

    public OrderPersistenceServiceImpl(
            final OrderDao orderDao,
            final MarketOrderDao marketOrderDao,
            final LimitOrderDao limitOrderDao,
            final StopOrderDao stopOrderDao,
            final StopLimitOrderDao stopLimitOrderDao,
            final OrderPropertyDao orderPropertyDao,
            final OrderStatusDao orderStatusDao) {

        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(marketOrderDao, "MarketOrderDao is null");
        Validate.notNull(limitOrderDao, "LimitOrderDao is null");
        Validate.notNull(stopOrderDao, "StopOrderDao is null");
        Validate.notNull(stopLimitOrderDao, "StopLimitOrderDao is null");
        Validate.notNull(orderPropertyDao, "OrderPropertyDao is null");
        Validate.notNull(orderStatusDao, "OrderStatusDao is null");

        this.orderDao = orderDao;
        this.marketOrderDao = marketOrderDao;
        this.limitOrderDao = limitOrderDao;
        this.stopOrderDao = stopOrderDao;
        this.stopLimitOrderDao = stopLimitOrderDao;
        this.orderPropertyDao = orderPropertyDao;
        this.orderStatusDao = orderStatusDao;
    }

    @Override
    @Retryable(maxAttempts = 5, value = {LockAcquisitionException.class, CannotAcquireLockException.class})
    @Async("orderPersistExecutor")
    @Transactional(propagation = Propagation.REQUIRED)
    public void persistOrder(final Order order) {

        if (order instanceof MarketOrder) {
            this.marketOrderDao.save((MarketOrder) order);
        } else if (order instanceof LimitOrder) {
            this.limitOrderDao.save((LimitOrder) order);
        } else if (order instanceof StopOrder) {
            this.stopOrderDao.save((StopOrder) order);
        } else if (order instanceof StopLimitOrder) {
            this.stopLimitOrderDao.save((StopLimitOrder) order);
        } else {
            throw new IllegalStateException("Unexpected order type " + order.getClass());
        }

        // save order properties
        if (order.getOrderProperties() != null && !order.getOrderProperties().isEmpty()) {
            for (OrderProperty orderProperty : order.getOrderProperties().values()) {
                this.orderPropertyDao.save(orderProperty);
            }
        }
    }

    @Override
    @Retryable(maxAttempts = 5, value = {LockAcquisitionException.class, CannotAcquireLockException.class})
    @Async("orderPersistExecutor")
    @Transactional(propagation = Propagation.REQUIRED)
    public void persistOrderStatus(final OrderStatus orderStatus) {

        if (orderStatus.getId() != 0) {
            LOGGER.error("OrderStatus may not be updated");
            return;
        }
        Order order = orderStatus.getOrder();
        if (order == null) {
            LOGGER.error("OrderStatus does not have an order associated with it");
            return;
        }
        if (order.getId() == 0 ) {
            // reload persistent order instance
            String intId = order.getIntId();
            Order persistentOrder = this.orderDao.findByIntId(intId);
            if (persistentOrder == null) {
                throw new ServiceException("Order with IntId " + intId + " has not been persisted");
            }
            orderStatus.setOrder(persistentOrder);
        }
        String extId = orderStatus.getExtId();
        if (extId != null && order.getExtId() == null) {
            order.setExtId(extId);
            this.orderDao.persist(order).setExtId(extId);
        }
        if (orderStatus.getId() == 0) {

            // Truncate reason
            String reason = orderStatus.getReason();
            if (reason != null && reason.length() > MAX_REASON_LEN) {
                orderStatus.setReason(reason.substring(0, MAX_REASON_LEN));
            }

            this.orderStatusDao.save(orderStatus);
        }
    }

}
