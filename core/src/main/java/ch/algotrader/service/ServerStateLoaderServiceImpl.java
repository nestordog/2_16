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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.HibernateInitializer;
import ch.algotrader.dao.PositionDao;
import ch.algotrader.dao.strategy.CashBalanceDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderStatusDao;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.strategy.CashBalance;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderBook;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
@InitializationPriority(value = InitializingServiceType.STATE_LOADER)
public class ServerStateLoaderServiceImpl implements ServerStateLoaderService, InitializingServiceI {

    private static final Logger LOGGER = LogManager.getLogger(ServerStateLoaderServiceImpl.class);

    private final SessionFactory sessionFactory;

    private final OrderDao orderDao;

    private final OrderStatusDao orderStatusDao;

    private final PositionDao positionDao;

    private final CashBalanceDao cashBalanceDao;

    private final OrderBook orderBook;

    private final EventDispatcher eventDispatcher;

    public ServerStateLoaderServiceImpl(
            final SessionFactory sessionFactory,
            final OrderDao orderDao,
            final OrderStatusDao orderStatusDao,
            final PositionDao positionDao,
            final CashBalanceDao cashBalanceDao,
            final OrderBook orderBook,
            final EventDispatcher eventDispatcher) {

        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(orderStatusDao, "OrderStatusDao is null");
        Validate.notNull(positionDao, "PositionDao is null");
        Validate.notNull(cashBalanceDao, "CashBalanceDao is null");
        Validate.notNull(orderBook, "OpenOrderRegistry is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");

        this.sessionFactory = sessionFactory;
        this.orderDao = orderDao;
        this.orderStatusDao = orderStatusDao;
        this.positionDao = positionDao;
        this.cashBalanceDao = cashBalanceDao;
        this.orderBook = orderBook;
        this.eventDispatcher = eventDispatcher;
    }

    public Map<Order, OrderStatus> loadPendingOrders() {

        List<OrderStatus> pendingOrderStati = this.orderStatusDao.findPending();
        List<Long> unacknowledgedOrderIds = this.orderDao.findUnacknowledgedOrderIds();

        if (pendingOrderStati.isEmpty() && unacknowledgedOrderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> pendingOrderIds = new ArrayList<>(unacknowledgedOrderIds.size() + pendingOrderStati.size());
        Map<Long, OrderStatus> orderStatusMap = new HashMap<>(pendingOrderStati.size());
        for (OrderStatus pendingOrderStatus: pendingOrderStati) {

            long orderId = pendingOrderStatus.getOrder().getId();
            pendingOrderIds.add(orderId);
            orderStatusMap.put(orderId, pendingOrderStatus);
        }
        pendingOrderIds.addAll(unacknowledgedOrderIds);

        // Clear session to evict Order proxies associated with order stati
        this.sessionFactory.getCurrentSession().clear();

        List<Order> orderList = this.orderDao.findByIds(pendingOrderIds);
        Map<Order, OrderStatus> pendingOrderMap = new HashMap<>(pendingOrderIds.size());
        for (Order pendingOrder: orderList) {

            OrderStatus orderStatus = orderStatusMap.get(pendingOrder.getId());
            pendingOrderMap.put(pendingOrder, orderStatus);
        }

        return pendingOrderMap;
    }

    public List<Position> getAllPositions() {

        return this.positionDao.loadAll();
    }

    @Override
    public List<CashBalance> getAllCashBalances() {

        return this.cashBalanceDao.loadAll();
    }

    @Override
    public void init() {

        List<Position> positions = getAllPositions();
        for (Position position: positions) {
            Strategy strategy = position.getStrategy();
            this.eventDispatcher.resendPastEvent(strategy.getName(), position.convertToVO());
        }

        List<CashBalance> cashBalances = getAllCashBalances();
        for (CashBalance cashBalance: cashBalances) {
            Strategy strategy = cashBalance.getStrategy();
            this.eventDispatcher.resendPastEvent(strategy.getName(), cashBalance.convertToVO());
        }

        Map<Order, OrderStatus> pendingOrderMap = loadPendingOrders();
        if (!pendingOrderMap.isEmpty()) {

            List<Order> orderList  = new ArrayList<>(pendingOrderMap.keySet());

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("{} order(s) are pending", orderList.size());
            }
            for (int i = 0; i < orderList.size(); i++) {
                Order order = orderList.get(i);
                Security security = order.getSecurity();
                security.initializeSecurityFamily(HibernateInitializer.INSTANCE);
                SecurityFamily securityFamily = security.getSecurityFamily();
                securityFamily.initializeExchange(HibernateInitializer.INSTANCE);

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("{}: {}", (i + 1), order);
                }

                Strategy strategy = order.getStrategy();
                this.eventDispatcher.resendPastEvent(strategy.getName(), order.convertToVO());
            }

            for (Map.Entry<Order, OrderStatus> entry: pendingOrderMap.entrySet()) {

                Order order = entry.getKey();
                this.orderBook.add(order);

                OrderStatus orderStatus = entry.getValue();
                if (orderStatus != null) {
                    this.orderBook.updateExecutionStatus(order.getIntId(), orderStatus.getStatus(), orderStatus.getFilledQuantity(), orderStatus.getRemainingQuantity());
                    Strategy strategy = order.getStrategy();
                    this.eventDispatcher.resendPastEvent(strategy.getName(), orderStatus.convertToVO());
                }
            }
        }
    }

}
