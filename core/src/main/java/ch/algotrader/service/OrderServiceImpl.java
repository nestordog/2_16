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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.HibernateInitializer;
import ch.algotrader.dao.exchange.ExchangeDao;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.dao.trade.OrderPreferenceDao;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.marketData.TickVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.Allocation;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderVO;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderVO;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderDetailsVO;
import ch.algotrader.entity.trade.OrderPreference;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderVO;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopLimitOrderVO;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderVO;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.SimpleOrderType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.util.BeanUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class OrderServiceImpl implements OrderService {

    private static final Logger LOGGER = LogManager.getLogger(OrderServiceImpl.class);
    private static final Logger NOTIFICATION_LOGGER = LogManager.getLogger("ch.algotrader.service.NOTIFICATION");

    private final CommonConfig commonConfig;

    private final MarketDataCache marketDataCache;

    private final OrderDao orderDao;

    private final StrategyDao strategyDao;

    private final SecurityDao securityDao;

    private final AccountDao accountDao;

    private final ExchangeDao exchangeDao;

    private final OrderPreferenceDao orderPreferenceDao;

    private final OrderRegistry orderRegistry;

    private final EventDispatcher eventDispatcher;

    private final EngineManager engineManager;

    private final Engine serverEngine;

    private final Map<String, ExternalOrderService> externalOrderServiceMap;

    public OrderServiceImpl(final CommonConfig commonConfig,
            final MarketDataCache marketDataCache,
            final OrderDao orderDao,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final ExchangeDao exchangeDao,
            final OrderPreferenceDao orderPreferenceDao,
            final OrderRegistry orderRegistry,
            final EventDispatcher eventDispatcher,
            final EngineManager engineManager,
            final Engine serverEngine,
            final Map<String, ExternalOrderService> externalOrderServiceMap) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(marketDataCache, "MarketDataCache is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(exchangeDao, "ExchangeDao is null");
        Validate.notNull(orderPreferenceDao, "OrderPreferenceDao is null");
        Validate.notNull(orderRegistry, "OpenOrderRegistry is null");
        Validate.notNull(eventDispatcher, "PlatformEventDispatcher is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.marketDataCache = marketDataCache;
        this.orderDao = orderDao;
        this.strategyDao = strategyDao;
        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.exchangeDao = exchangeDao;
        this.orderPreferenceDao = orderPreferenceDao;
        this.orderRegistry = orderRegistry;
        this.eventDispatcher = eventDispatcher;
        this.engineManager = engineManager;
        this.serverEngine = serverEngine;
        this.externalOrderServiceMap = new ConcurrentHashMap<>(externalOrderServiceMap);
    }

    @Override
    public Order createOrderByOrderPreference(final String name) {

        Validate.notEmpty(name, "Name is empty");

        OrderPreference orderPreference = this.orderPreferenceDao.findByName(name);

        Validate.notNull(orderPreference, "unknown OrderPreference" + name == null ? "" : " " + name);

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

            MarketDataEventVO marketDataEvent = this.marketDataCache.getCurrentMarketDataEvent(security.getId());
            if (marketDataEvent == null) {
                throw new OrderValidationException("no marketDataEvent available to initialize SlicingOrder");
            } else if (!(marketDataEvent instanceof TickVO)) {
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

        // validate the order before sending it
        try {
            validateOrder(order);
        } catch (OrderValidationException ex) {
            throw new ServiceException(ex);
        }

        if (order instanceof AlgoOrder) {
            sendAlgoOrder((AlgoOrder) order);
        } else if (order instanceof SimpleOrder){
            sendSimpleOrder((SimpleOrder) order);
        } else {
            throw new ServiceException("Unexpected order class: " + order.getClass());
        }
    }

    private void sendAlgoOrder(final AlgoOrder order) {

        if (order.getDateTime() == null) {
            order.setDateTime(this.engineManager.getCurrentEPTime());
        }
        order.setIntId(AlgoIdGenerator.getInstance().getNextOrderId());

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("send algo order: {}", order);
        }

        this.orderRegistry.add(order);

        this.serverEngine.sendEvent(order);
    }

    private void sendSimpleOrder(final SimpleOrder order) {

        Account account = order.getAccount();
        if (account == null) {
            throw new ServiceException("Order with missing account");
        }

        ExternalOrderService externalOrderService = getExternalOrderService(account);

        enforceTif(order, externalOrderService);
        externalOrderService.sendOrder(order);

        this.serverEngine.sendEvent(order);
    }

    private void enforceTif(final SimpleOrder order, final ExternalOrderService externalOrderService) {

        if (order.getDateTime() == null) {
            order.setDateTime(this.engineManager.getCurrentEPTime());
        }
        if (order.getTif() == null) {
            SimpleOrderType orderType;
            if (order instanceof MarketOrder) {
                orderType = SimpleOrderType.MARKET;
            } else if (order instanceof LimitOrder) {
                orderType = SimpleOrderType.LIMIT;
            } else if (order instanceof StopOrder) {
                orderType = SimpleOrderType.STOP;
            } else if (order instanceof StopLimitOrder) {
                orderType = SimpleOrderType.STOP_LIMIT;
            } else {
                throw new ServiceException("Unsupported simple order class: " + order.getClass());
            }
            order.setTif(externalOrderService.getDefaultTIF(orderType));
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

        if (order instanceof AlgoOrder) {
            cancelAlgoOrder((AlgoOrder) order);
        } else if (order instanceof SimpleOrder){
            cancelSimpleOrder((SimpleOrder) order);
        } else {
            throw new ServiceException("Unexpected order class: " + order.getClass());
        }

    }

    private void cancelAlgoOrder(AlgoOrder order) {

        String algoOrderId = order.getIntId();
        ExecutionStatusVO executionStatus = this.orderRegistry.getStatusByIntId(algoOrderId);
        if (executionStatus != null) {
            OrderStatus algoOrderStatus = OrderStatus.Factory.newInstance();
            algoOrderStatus.setIntId(algoOrderId);
            algoOrderStatus.setStatus(Status.CANCELED);
            algoOrderStatus.setFilledQuantity(executionStatus.getFilledQuantity());
            algoOrderStatus.setRemainingQuantity(executionStatus.getRemainingQuantity());
            algoOrderStatus.setOrder(order);

            this.orderRegistry.updateExecutionStatus(algoOrderId, null, algoOrderStatus.getStatus(),
                    algoOrderStatus.getFilledQuantity(), algoOrderStatus.getRemainingQuantity());

            propagateOrderStatus(algoOrderStatus);

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("cancelled algo order: {}", order);
            }
        }

        Collection<Order> openOrders = this.orderRegistry.getOpenOrdersByParentIntId(order.getIntId());
        for (Order childOrder: openOrders) {
            if (childOrder instanceof SimpleOrder) {
                Account account = order.getAccount();
                getExternalOrderService(account).cancelOrder((SimpleOrder) childOrder);
            }
        }
    }

    private void cancelSimpleOrder(final SimpleOrder order) {

        Account account = order.getAccount();
        if (account == null) {
            throw new ServiceException("Order with missing account");
        }

        ExternalOrderService externalOrderService = getExternalOrderService(account);
        externalOrderService.cancelOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOrder(final String intId) {

        Validate.notNull(intId, "Int id is null");

        Order order = this.orderRegistry.getOpenOrderByIntId(intId);
        if (order == null) {
            throw new ServiceException("Could not find open order with IntId " + intId);
        }
        cancelOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelAllOrders() {

        final List<Order> orders = this.orderRegistry.getAllOpenOrders();
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
    public void modifyOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        if (order instanceof AlgoOrder) {
            throw new ServiceException("Modification of AlgoOrders is not permitted");
        } else if (order instanceof SimpleOrder){

            SimpleOrder newOrder;
            if (order.getId() != 0) {
                try {
                    newOrder = BeanUtil.clone((SimpleOrder) order);
                    newOrder.setId(0);
                } catch (ReflectiveOperationException ex) {
                    throw new ServiceException(ex);
                }
            } else {
                newOrder = (SimpleOrder) order;
            }

            modifySimpleOrder(newOrder);
        } else {
            throw new ServiceException("Unexpected order class: " + order.getClass());
        }
    }

    private void modifySimpleOrder(final SimpleOrder order) {

        Account account = order.getAccount();
        if (account == null) {
            throw new ServiceException("Order with missing account");
        }

        ExternalOrderService externalOrderService = getExternalOrderService(account);
        enforceTif(order, externalOrderService);
        externalOrderService.modifyOrder(order);

        this.serverEngine.sendEvent(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyOrder(final String intId, final Map<String, String> properties) {

        Validate.notNull(intId, "Int id is null");
        Validate.notNull(properties, "Properties is null");

        Order order = this.orderRegistry.getOpenOrderByIntId(intId);
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

        modifyOrder(newOrder);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextOrderId(final long accountId) {

        Account account = this.accountDao.load(accountId);
        if (account == null) {
            throw new ServiceException("Unknown account id: " + accountId);
        }
        return getExternalOrderService(account).getNextOrderId(account);
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

    @Override
    public List<Order> getOpenOrdersByStrategy(final long strategyId) {

        return this.orderRegistry.getOpenOrdersByStrategy(strategyId);
    }

    @Override
    public List<Order> getOpenOrdersBySecurity(final long securityId) {

        return this.orderRegistry.getOpenOrdersBySecurity(securityId);
    }

    @Override
    public List<Order> getOpenOrdersByStrategyAndSecurity(final long strategyId, final long securityId) {

        return this.orderRegistry.getOpenOrdersByStrategyAndSecurity(strategyId, securityId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Order getOrderByIntId(String intId) {

        Order order = this.orderRegistry.getByIntId(intId);
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

        return this.orderRegistry.getOpenOrderDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrderDetailsVO> getRecentOrderDetails() {

        return this.orderRegistry.getRecentOrderDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void evictExecutedOrders() {

        this.orderRegistry.evictCompleted();
    }

    private ExternalOrderService getExternalOrderService(Account account) {

        Validate.notNull(account, "Account is null");

        String orderServiceType;

        if (this.commonConfig.isSimulation()) {
            orderServiceType = OrderServiceType.SIMULATION.name();
        } else {
            orderServiceType = account.getOrderServiceType();
        }

        ExternalOrderService externalOrderService = this.externalOrderServiceMap.get(orderServiceType);
        if (externalOrderService == null) {
            throw new ServiceException("No ExternalOrderService found for service type " + orderServiceType);
        }
        return externalOrderService;
    }

    private static final class AlgoIdGenerator {

        private static AlgoIdGenerator instance;

        private final AtomicLong orderId = new AtomicLong(0);

        public static synchronized AlgoIdGenerator getInstance() {

            if (instance == null) {
                instance = new AlgoIdGenerator();
            }
            return instance;
        }

        public String getNextOrderId() {
            return "a" + Long.toString(this.orderId.incrementAndGet());
        }
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
    public void sendOrder(final OrderVO order) {

        sendSimpleOrder(convert(order));
    }

    @Override
    public void modifyOrder(OrderVO order) {

        modifySimpleOrder(convert(order));
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
