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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.HibernateInitializer;
import ch.algotrader.dao.exchange.ExchangeDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderPreferenceDao;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderVO;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderVO;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderI;
import ch.algotrader.entity.trade.OrderPreference;
import ch.algotrader.entity.trade.OrderStatusVO;
import ch.algotrader.entity.trade.OrderVO;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.SimpleOrderI;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopLimitOrderVO;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderVO;
import ch.algotrader.entity.trade.algo.AlgoOrder;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.util.BeanUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LogManager.getLogger(OrderServiceImpl.class);
    private static final Logger NOTIFICATION_LOGGER = LogManager.getLogger("ch.algotrader.service.NOTIFICATION");

    private final OrderDao orderDao;

    private final StrategyDao strategyDao;

    private final SecurityDao securityDao;

    private final AccountDao accountDao;

    private final ExchangeDao exchangeDao;

    private final OrderPreferenceDao orderPreferenceDao;

    private final SimpleOrderService simpleOrderService;

    private final AlgoOrderService algoOrderService;

    private final OrderBook orderBook;

    public OrderServiceImpl(
            final SimpleOrderService simpleOrderService,
            final AlgoOrderService algoOrderService,
            final OrderDao orderDao,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final ExchangeDao exchangeDao,
            final OrderPreferenceDao orderPreferenceDao,
            final OrderBook orderBook) {

        Validate.notNull(simpleOrderService, "ServerOrderService is null");
        Validate.notNull(algoOrderService, "AlgoOrderService is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(exchangeDao, "ExchangeDao is null");
        Validate.notNull(orderPreferenceDao, "OrderPreferenceDao is null");
        Validate.notNull(orderBook, "orderBook is null");

        this.simpleOrderService = simpleOrderService;
        this.algoOrderService = algoOrderService;
        this.orderDao = orderDao;
        this.strategyDao = strategyDao;
        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.exchangeDao = exchangeDao;
        this.orderPreferenceDao = orderPreferenceDao;
        this.orderBook = orderBook;
    }

    @Override
    public Order createOrderByOrderPreference(final String name) {

        Validate.notEmpty(name, "Name is empty");

        OrderPreference orderPreference = this.orderPreferenceDao.findByName(name);
        if (orderPreference == null) {
            throw new ServiceException("Order preference '" + name + "' not found");
        }
        Class<?> orderClazz;
        Order order;
        try {

            // create an order instance
            orderClazz = Class.forName(orderPreference.getOrderType().getValue());
            order = (Order) orderClazz.newInstance();

            // populate the order with the properties
            BeanUtil.populate(order, orderPreference.getPropertyNameValueMap());

        } catch (ReflectiveOperationException e) {
            throw new ServiceException(e);
        }

        // set the account if defined
        if (orderPreference.getDefaultAccount() != null) {
            order.setAccount(orderPreference.getDefaultAccount());
        }

        return order;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateOrder(final Order order) throws OrderValidationException {

        Validate.notNull(order, "Order is null");

        if (order instanceof SimpleOrder) {
            this.simpleOrderService.validateOrder((SimpleOrder) order);
        } else if (order instanceof AlgoOrder) {
            this.algoOrderService.validateOrder((AlgoOrder) order);
        } else {
            throw new ServiceException("Unexpected order class: " + order.getClass());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        // validate strategy and security
        Validate.notNull(order.getStrategy());
        Validate.notNull(order.getSecurity());

        order.setStrategy(this.strategyDao.load(order.getStrategy().getId()));
        order.initializeStrategy(HibernateInitializer.INSTANCE);
        order.setSecurity(this.securityDao.findByIdInclFamilyUnderlyingExchangeAndBrokerParameters(order.getSecurity().getId()));
        order.initializeSecurity(HibernateInitializer.INSTANCE);

        // reload the account if necessary to get potential changes
        if (order.getAccount() != null) {
            order.setAccount(this.accountDao.load(order.getAccount().getId()));
            order.initializeAccount(HibernateInitializer.INSTANCE);
        }

        // reload the exchange if necessary to get potential changes
        if (order.getExchange() != null) {
            order.setExchange(this.exchangeDao.load(order.getExchange().getId()));
            order.initializeExchange(HibernateInitializer.INSTANCE);
        }

        if (order instanceof SimpleOrder) {
            return this.simpleOrderService.sendOrder((SimpleOrder) order);
        } else if (order instanceof AlgoOrder) {
            return this.algoOrderService.sendOrder((AlgoOrder) order);
        } else {
            throw new ServiceException("Unexpected order class: " + order.getClass());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> sendOrders(final Collection<Order> orders) {

        List<String> ids = new ArrayList<>(orders.size());
        for (Order order : orders) {
            ids.add(sendOrder(order));
        }
        return ids;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String cancelOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        if (order instanceof SimpleOrder) {
            return this.simpleOrderService.cancelOrder((SimpleOrder) order);
        } else if (order instanceof AlgoOrder) {
            return this.algoOrderService.cancelOrder((AlgoOrder) order);
        } else {
            throw new ServiceException("Unexpected order class: " + order.getClass());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String cancelOrder(final String intId) {

        Validate.notNull(intId, "Int id is null");

        Order order = this.orderBook.getOpenOrderByIntId(intId);
        if (order != null) {
            return cancelOrder(order);
        } else {
            order = this.orderBook.getByIntId(intId);
            if (order == null) {
                throw new ServiceException("Could not find order with IntId " + intId);
            } else {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Order {} already completed", intId);
                }
            }
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelAllOrders() {

        final List<Order> orders = this.orderBook.getAllOpenOrders();
        if (LOGGER.isInfoEnabled() && !orders.isEmpty()) {
            LOGGER.info("Canceling {} open orders", orders.size());
        }
        for (Order order: orders) {
            cancelOrder(order);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String modifyOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        if (order instanceof SimpleOrder) {
            return this.simpleOrderService.modifyOrder((SimpleOrder) order);
        } else if (order instanceof AlgoOrder) {
            return this.algoOrderService.modifyOrder((AlgoOrder) order);
        } else {
            throw new ServiceException("Unexpected order class: " + order.getClass());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String modifyOrder(final String intId, final Map<String, String> properties) {

        Validate.notNull(intId, "Int id is null");
        Validate.notNull(properties, "Properties is null");

        Order order = this.orderBook.getOpenOrderByIntId(intId);
        if (order == null) {
            throw new ServiceException("Could not find open order with IntId " + intId);
        }
        Order newOrder;
        try {
            newOrder = BeanUtil.cloneAndPopulate(order, properties);
            newOrder.setId(0);
        } catch (ReflectiveOperationException ex) {
            throw new ServiceException(ex);
        }

        return modifyOrder(newOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextOrderId(final Class<? extends OrderI> orderClass, final long accountId) {

        Account account = this.accountDao.load(accountId);
        if (account == null) {
            throw new ServiceException("Unknown account id: " + accountId);
        }
        if (SimpleOrderI.class.isAssignableFrom(orderClass)) {
            return this.simpleOrderService.getNextOrderId(account);
        } else if (AlgoOrder.class.isAssignableFrom(orderClass)) {
            return this.algoOrderService.getNextOrderId(account);
        } else {
            throw new ServiceException("Unexpected order class: " + orderClass);
        }

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
    public List<Order> getOpenOrdersByStrategy(final long strategyId) {

        return this.orderBook.getOpenOrdersByStrategy(strategyId);
    }

    @Override
    public List<Order> getOpenOrdersBySecurity(final long securityId) {

        return this.orderBook.getOpenOrdersBySecurity(securityId);
    }

    @Override
    public List<Order> getOpenOrdersByStrategyAndSecurity(final long strategyId, final long securityId) {

        return this.orderBook.getOpenOrdersByStrategyAndSecurity(strategyId, securityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Order getOrderByIntId(String intId) {

        Order order = this.orderBook.getByIntId(intId);
        if (order != null) {
            return order;
        } else {
            return this.orderDao.findByIntId(intId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderDetailsVO> getOpenOrderDetails() {

        return this.orderBook.getOpenOrderDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderDetailsVO> getRecentOrderDetails() {

        return this.orderBook.getRecentOrderDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evictExecutedOrders() {

        this.orderBook.evictCompleted();
    }

    private SimpleOrder convert(final OrderVO orderVO) {

        SimpleOrder order;
        if (orderVO instanceof MarketOrderVO) {

            order = MarketOrder.Factory.newInstance();
        } else if (orderVO instanceof LimitOrderVO) {

            LimitOrder limitOrder = LimitOrder.Factory.newInstance();
            limitOrder.setLimit(((LimitOrderVO) orderVO).getLimit());
            order = limitOrder;
        } else if (orderVO instanceof StopOrderVO) {

            StopOrder stopOrder = StopOrder.Factory.newInstance();
            stopOrder.setStop(((StopOrderVO) orderVO).getStop());
            order = stopOrder;
        } else if (orderVO instanceof StopLimitOrderVO) {

            StopLimitOrder stopLimitOrder = StopLimitOrder.Factory.newInstance();
            stopLimitOrder.setLimit(((StopLimitOrderVO) orderVO).getLimit());
            stopLimitOrder.setStop(((StopLimitOrderVO) orderVO).getStop());
            order = stopLimitOrder;
        } else {
            throw new IllegalArgumentException("Unexpected order VO class: " + orderVO.getClass());
        }

        order.setIntId(orderVO.getIntId());
        order.setSide(orderVO.getSide());
        order.setQuantity(orderVO.getQuantity());
        order.setTif(orderVO.getTif());
        order.setTifDateTime(orderVO.getTifDateTime());
        order.setDateTime(orderVO.getDateTime());

        order.setStrategy(this.strategyDao.load(orderVO.getStrategyId()));
        order.initializeStrategy(HibernateInitializer.INSTANCE);
        order.setSecurity(this.securityDao.findByIdInclFamilyUnderlyingExchangeAndBrokerParameters(orderVO.getSecurityId()));
        order.initializeSecurity(HibernateInitializer.INSTANCE);

        // reload the account if necessary to get potential changes
        if (orderVO.getAccountId() > 0) {
            order.setAccount(this.accountDao.load(orderVO.getAccountId()));
            order.initializeAccount(HibernateInitializer.INSTANCE);
        }

        // reload the exchange if necessary to get potential changes
        if (orderVO.getExchangeId() > 0) {
            order.setExchange(this.exchangeDao.load(orderVO.getExchangeId()));
            order.initializeExchange(HibernateInitializer.INSTANCE);
        }

        // validate the order before sending it
        try {
            validateOrder(order);
        } catch (OrderValidationException ex) {
            throw new ServiceException(ex);
        }
        return order;
    }

    @Override
    public String sendOrder(final OrderVO order) {

        return this.simpleOrderService.sendOrder(convert(order));
    }

    @Override
    public String modifyOrder(OrderVO order) {

        return this.simpleOrderService.modifyOrder(convert(order));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void suggestOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        NOTIFICATION_LOGGER.info("order " + order);

    }

}
