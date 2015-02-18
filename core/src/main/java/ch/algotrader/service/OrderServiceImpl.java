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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountDao;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.Allocation;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletion;
import ch.algotrader.entity.trade.OrderDao;
import ch.algotrader.entity.trade.OrderPreference;
import ch.algotrader.entity.trade.OrderPreferenceDao;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusDao;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.SubmittedOrder;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.util.spring.HibernateSession;
import ch.algotrader.vo.OrderStatusVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
@InitializationPriority(value = InitializingServiceType.CORE)
public class OrderServiceImpl implements OrderService, InitializingServiceI, ApplicationListener<ContextRefreshedEvent> {

    private static final long serialVersionUID = 3969251081188007542L;

    private static Logger logger = Logger.getLogger(OrderServiceImpl.class.getName());
    private static Logger notificationLogger = Logger.getLogger("ch.algotrader.service.NOTIFICATION");

    private final CommonConfig commonConfig;

    private final SessionFactory sessionFactory;

    private final OrderPersistenceService orderPersistService;

    private final LocalLookupService localLookupService;

    private final OrderDao orderDao;

    private final OrderStatusDao orderStatusDao;

    private final StrategyDao strategyDao;

    private final SecurityDao securityDao;

    private final AccountDao accountDao;

    private final OrderPreferenceDao orderPreferenceDao;

    private final EngineManager engineManager;

    private final Engine serverEngine;

    private final AtomicBoolean initialized;

    private final Map<OrderServiceType, ExternalOrderService> externalOrderServiceMap;

    public OrderServiceImpl(final CommonConfig commonConfig,
            final SessionFactory sessionFactory,
            final OrderPersistenceService orderPersistService,
            final LocalLookupService localLookupService,
            final OrderDao orderDao,
            final OrderStatusDao orderStatusDao,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final OrderPreferenceDao orderPreferenceDao,
            final EngineManager engineManager,
            final Engine serverEngine) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(sessionFactory, "SessionFactory is null");
        Validate.notNull(orderPersistService, "OrderPersistStrategy is null");
        Validate.notNull(localLookupService, "LocalLookupService is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(orderStatusDao, "OrderStatusDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(orderPreferenceDao, "OrderPreferenceDao is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.sessionFactory = sessionFactory;
        this.orderPersistService = orderPersistService;
        this.localLookupService = localLookupService;
        this.orderDao = orderDao;
        this.orderStatusDao = orderStatusDao;
        this.strategyDao = strategyDao;
        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.orderPreferenceDao = orderPreferenceDao;
        this.engineManager = engineManager;
        this.serverEngine = serverEngine;
        this.initialized = new AtomicBoolean(false);
        this.externalOrderServiceMap = new ConcurrentHashMap<>();
    }

    @Override
    public Order createOrderByOrderPreference(final String name) {

        Validate.notEmpty(name, "Name is empty");

        OrderPreference orderPreference = this.orderPreferenceDao.findByName(name);

        Validate.notNull(orderPreference, "unknown OrderPreference");

        Class<?> orderClazz;
        Order order;
        try {

            // create an order instance
            orderClazz = Class.forName(orderPreference.getOrderType().getValue());
            order = (Order) orderClazz.newInstance();

            // populate the order with the properties
            BeanUtil.populate(order, orderPreference.getPropertyNameValueMap());

        } catch (ReflectiveOperationException e) {
            throw new OrderServiceException(e);
        }

        // set the account if defined
        if (orderPreference.getDefaultAccount() != null) {
            order.setAccount(orderPreference.getDefaultAccount());
        }

        // set allocations if defined
        if (orderPreference.getAllocations().size() > 0) {

            if (!(order instanceof AlgoOrder)) {
                throw new IllegalStateException("allocations cannot be assigned to " + orderClazz + " (only AlgoOrders can have allocations)");
            } else {

                double totalAllocation = 0;
                for (Allocation allocation : orderPreference.getAllocations()) {
                    totalAllocation += allocation.getValue();
                }

                if (totalAllocation != 1.0) {
                    throw new IllegalStateException("sum of allocations are not 1.0 for " + toString());
                }

                AlgoOrder algoOrder = (AlgoOrder) order;
                algoOrder.setAllocations(orderPreference.getAllocations());
            }
        }

        return order;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateOrder(final Order order) throws OrderValidationException {

        Validate.notNull(order, "Order is null");

        // validate general properties
        Validate.notNull(order.getSide(), "missing side for order " + order);
        Validate.isTrue(order.getQuantity() != 0, "quanity cannot be zero for order " + order);
        Validate.isTrue(order.getQuantity() > 0, "quantity has to be positive for order " + order);

        if (order instanceof SimpleOrder) {
            Validate.notNull(order.getAccount(), "missing account for order " + order);
        }

        // validate order specific properties
        order.validate();

        // check that the security is tradeable
        Security security = order.getSecurity();
        Validate.isTrue(security.getSecurityFamily().isTradeable(), security + " is not tradeable");

        // external validation of the order
        if (order instanceof SimpleOrder) {

            getExternalOrderService(order.getAccount()).validateOrder((SimpleOrder) order);

        } else if (order instanceof AlgoOrder) {

            // check market data for AlgoOrders
            if (security.getSubscriptions().size() == 0) {
                throw new OrderValidationException(security + " is not subscribed for " + order);
            }

            MarketDataEvent marketDataEvent = this.localLookupService.getCurrentMarketDataEvent(security.getId());
            if (marketDataEvent == null) {
                throw new OrderValidationException("no marketDataEvent available to initialize SlicingOrder");
            } else if (!(marketDataEvent instanceof Tick)) {
                throw new OrderValidationException("only ticks are supported, " + marketDataEvent.getClass() + " are not supported");
            }
        }

        // TODO add internal validations (i.e. limit, amount, etc.)

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        // validate strategy and security
        Validate.notNull(order.getStrategy());
        Validate.notNull(order.getSecurity());

        // reload the strategy and security to get potential changes
        order.setStrategy(this.strategyDao.load(order.getStrategy().getId()));
        order.setSecurity(this.securityDao.findByIdInclFamilyAndUnderlying(order.getSecurity().getId()));

        // reload the account if necessary to get potential changes
        if (order.getAccount() != null) {
            order.setAccount(this.accountDao.load(order.getAccount().getId()));
        }

        // validate the order before sending it
        try {
            validateOrder(order);
        } catch (OrderValidationException ex) {
            throw new OrderServiceException(ex);
        }

        // set the dateTime property
        order.setDateTime(this.engineManager.getCurrentEPTime());

        // in case no TIF was specified set DAY
        if (order.getTif() == null) {
            order.setTif(TIF.DAY);
        }

        if (order instanceof AlgoOrder) {
            sendAlgoOrder((AlgoOrder) order);
        } else {
            Account account = order.getAccount();
            Validate.notNull(account, "missing account for order: " + order);
            getExternalOrderService(account).sendOrder((SimpleOrder) order);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOrders(final Collection<Order> orders) {

        for (Order order : orders) {
            sendOrder(order);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        // check if order exists
        if (this.orderDao.findOpenOrderByIntId(order.getIntId()) != null) {
            internalCancelOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + order.getIntId());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOrder(final String intId) {

        Validate.notNull(intId, "Int id is null");

        Order order = this.orderDao.findOpenOrderByIntId(intId);
        if (order != null) {
            internalCancelOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + intId);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelAllOrders() {

        for (Order order : this.orderDao.findAllOpenOrders()) {
            cancelOrder(order);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        // check if order exists
        if (this.orderDao.findOpenOrderByIntId(order.getIntId()) != null) {
            internalModifyOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + order.getIntId());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyOrder(final String intId, final Map<String, String> properties) {

        Validate.notNull(intId, "Int id is null");
        Validate.notNull(properties, "Properties is null");

        Order order = this.orderDao.findOpenOrderByIntId(intId);
        if (order != null) {

            Order newOrder;
            try {
                newOrder = BeanUtil.cloneAndPopulate(order, properties);
                newOrder.setId(0);
            } catch (ReflectiveOperationException ex) {
                throw new OrderServiceException(ex);
            }

            internalModifyOrder(newOrder);
        } else {
            throw new OrderServiceException("Unknown order id: " + intId, null);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistOrder(Order order) {

        if (this.commonConfig.isSimulation()) {
            return;
        }

        // save order to the DB by using the corresponding OrderDao
        this.orderPersistService.persistOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        // send the order into the AlgoTrader Server engine to be correlated with fills
        this.serverEngine.sendEvent(SubmittedOrder.Factory.newInstance(Status.OPEN, 0, order.getQuantity(), order));

        // also send the order to the strategy that placed the order
        if (!order.getStrategy().isServer()) {
            this.engineManager.sendEvent(order.getStrategy().getName(), order);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateOrderStatus(final OrderStatus orderStatus) {

        Validate.notNull(orderStatus, "Order status is null");

        // ignore OrderStatus with no Order (stemming from emptyOpenOrderWindow)
        if (orderStatus.getOrder() == null) {
            return;
        }

        if (orderStatus.getDateTime() == null) {
            orderStatus.setDateTime(new Date());
        }

        if (orderStatus.getExtDateTime() == null) {
            orderStatus.setExtDateTime(new Date());
        }

        // send the fill to the strategy that placed the corresponding order
        if (orderStatus.getOrder() != null && !orderStatus.getOrder().getStrategy().isServer()) {
            this.engineManager.sendEvent(orderStatus.getOrder().getStrategy().getName(), orderStatus);
        }

        if (!this.commonConfig.isSimulation()) {
            logger.debug("propagated orderStatus: " + orderStatus);

            // only store OrderStatus for non AlgoOrders
            if (!(orderStatus.getOrder() instanceof AlgoOrder)) {

                this.orderPersistService.persistOrderStatus(orderStatus);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateOrderCompletion(final OrderCompletion orderCompletion) {

        Validate.notNull(orderCompletion, "Order completion is null");

        // send the fill to the strategy that placed the corresponding order
        if (orderCompletion.getOrder() != null && !orderCompletion.getOrder().getStrategy().isServer()) {
            this.engineManager.sendEvent(orderCompletion.getOrder().getStrategy().getName(), orderCompletion);
        }

        if (!this.commonConfig.isSimulation()) {
            logger.debug("propagated orderCompletion: " + orderCompletion);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOrderId(final Order order, final String intId, final String extId) {

        if (Objects.equals(intId, order.getIntId()) || Objects.equals(extId, order.getExtId())) {

            if (intId != null) {

                order.setIntId(intId);
            }
            if (extId != null) {

                order.setExtId(extId);
            }
            this.orderPersistService.persistOrder(order);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void suggestOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        notificationLogger.info("order " + order);

    }

    @Override
    public String getNextOrderId(final Account account) {

        return getExternalOrderService(account).getNextOrderId(account);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public Map<Order, OrderStatus> loadPendingOrders() {

        List<OrderStatus> pendingOrderStati = this.orderStatusDao.findPending();
        List<Integer> unacknowledgedOrderIds = this.orderDao.findUnacknowledgedOrderIds();

        if (pendingOrderStati.isEmpty() && unacknowledgedOrderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Integer> pendingOrderIds = new ArrayList<>(unacknowledgedOrderIds.size() + pendingOrderStati.size());
        Map<Integer, OrderStatus> orderStatusMap = new HashMap<>(pendingOrderStati.size());
        for (OrderStatus pendingOrderStatus: pendingOrderStati) {

            int orderId = pendingOrderStatus.getOrder().getId();
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

    @Override
    public void init() {

        final Map<Order, OrderStatus> pendingOrderMap = loadPendingOrders();
        if (logger.isInfoEnabled() && !pendingOrderMap.isEmpty()) {

            List<Order> orderList  = new ArrayList<Order>(pendingOrderMap.keySet());
            Collections.sort(orderList);

            logger.info(orderList.size() + " order(s) are pending");
            for (int i = 0; i < orderList.size(); i++) {
                Order order = orderList.get(i);
                logger.info((i + 1) + ": " + order);
            }
        }

        for (Map.Entry<Order, OrderStatus> entry: pendingOrderMap.entrySet()) {

            Order order = entry.getKey();
            OrderStatus orderStatus = entry.getValue();
            if (orderStatus != null) {
                this.serverEngine.sendEvent(SubmittedOrder.Factory.newInstance(orderStatus.getStatus(), orderStatus.getFilledQuantity(), orderStatus.getRemainingQuantity(), order));
            } else {
                this.serverEngine.sendEvent(SubmittedOrder.Factory.newInstance(Status.OPEN, 0, order.getQuantity(), order));
            }
        }
    }

    private void sendAlgoOrder(AlgoOrder order) {

        order.setIntId(AlgoIdGenerator.getInstance().getNextOrderId());

        logger.info("send algo order: " + order);

        // progapate the order to all corresponding esper engines
        propagateOrder(order);

        this.serverEngine.sendEvent(order);
    }

    private void internalCancelOrder(Order order) {

        if (order instanceof AlgoOrder) {
            cancelAlgoOrder((AlgoOrder) order);
        } else {

            Account account = order.getAccount();
            Validate.notNull(account, "missing account for order: " + order);
            getExternalOrderService(account).cancelOrder((SimpleOrder) order);
        }
    }

    private void cancelAlgoOrder(AlgoOrder order) {

        // cancel existing child orders
        for (Order childOrder : this.orderDao.findOpenOrdersByParentIntId(order.getIntId())) {

            Account account = order.getAccount();
            Validate.notNull(account, "missing account for order: " + order);
            getExternalOrderService(account).cancelOrder((SimpleOrder) childOrder);
        }

        // get the current OrderStatusVO
        OrderStatusVO orderStatusVO = this.orderStatusDao.findOrderStatusByIntId(order.getIntId());

        // assemble a new OrderStatus Entity
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.CANCELED);
        orderStatus.setFilledQuantity(orderStatusVO.getFilledQuantity());
        orderStatus.setRemainingQuantity(orderStatusVO.getRemainingQuantity());
        orderStatus.setOrder(order);

        // send the orderStatus
        this.serverEngine.sendEvent(orderStatus);

        logger.info("cancelled algo order: " + order);
    }

    private void internalModifyOrder(Order order) {

        if (order instanceof AlgoOrder) {
            throw new UnsupportedOperationException("modification of AlgoOrders are not permitted");
        } else {

            Account account = order.getAccount();
            Validate.notNull(account, "missing account for order: " + order);
            getExternalOrderService(account).modifyOrder((SimpleOrder) order);
        }
    }


    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {

        if (this.initialized.compareAndSet(false, true)) {

            ApplicationContext applicationContext = event.getApplicationContext();
            this.externalOrderServiceMap.clear();
            Map<String, ExternalOrderService> map = applicationContext.getBeansOfType(ExternalOrderService.class);
            for (Map.Entry<String, ExternalOrderService> entry: map.entrySet()) {

                ExternalOrderService externalOrderService = entry.getValue();
                this.externalOrderServiceMap.put(externalOrderService.getOrderServiceType(), externalOrderService);
            }
        }
    }

    private ExternalOrderService getExternalOrderService(Account account) {

        Validate.notNull(account, "Account is null");

        OrderServiceType orderServiceType;

        if (this.commonConfig.isSimulation()) {
            orderServiceType = OrderServiceType.SIMULATION;
        } else {
            orderServiceType = account.getOrderServiceType();
        }

        ExternalOrderService externalOrderService = this.externalOrderServiceMap.get(orderServiceType);
        if (externalOrderService == null) {
            throw new OrderServiceException("No ExternalOrderService found for service type " + orderServiceType);
        }
        return externalOrderService;
    }

    private static final class AlgoIdGenerator {

        private static AlgoIdGenerator instance;

        private int orderId = 0;

        public static synchronized AlgoIdGenerator getInstance() {

            if (instance == null) {
                instance = new AlgoIdGenerator();
            }
            return instance;
        }

        public synchronized String getNextOrderId() {
            return "a" + String.valueOf(this.orderId++);
        }
    }
}
