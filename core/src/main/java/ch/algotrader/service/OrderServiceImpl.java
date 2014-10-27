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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.AccountDao;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletion;
import ch.algotrader.entity.trade.OrderDao;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusDao;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.spring.HibernateSession;
import ch.algotrader.vo.OrderStatusVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@HibernateSession
public class OrderServiceImpl implements OrderService, ApplicationContextAware {

    private static Logger logger = MyLogger.getLogger(OrderServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("ch.algotrader.service.NOTIFICATION");

    private volatile ApplicationContext applicationContext;

    private final CommonConfig commonConfig;

    private final OrderDao orderDao;

    private final OrderStatusDao orderStatusDao;

    private final StrategyDao strategyDao;

    private final SecurityDao securityDao;

    private final AccountDao accountDao;

    private final OrderPersistenceService orderPersistStrategy;

    public OrderServiceImpl(final CommonConfig commonConfig,
            final OrderDao orderDao,
            final OrderStatusDao orderStatusDao,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final OrderPersistenceService orderPersistStrategy) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(orderStatusDao, "OrderStatusDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(orderPersistStrategy, "OrderPersistStrategy is null");

        this.commonConfig = commonConfig;
        this.orderDao = orderDao;
        this.orderStatusDao = orderStatusDao;
        this.strategyDao = strategyDao;
        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.orderPersistStrategy = orderPersistStrategy;
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
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
        Validate.isTrue(order.getSecurity().getSecurityFamily().isTradeable(), order.getSecurity() + " is not tradeable");

        // external validation of the order
        if (order instanceof SimpleOrder) {

            Account account = order.getAccount();
            Validate.notNull(account, "missing account for order: " + order);
            getExternalOrderService(account).validateOrder((SimpleOrder) order);
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
        Validate.notNull(order.getStrategy(), "missing strategy for order " + order);
        Validate.notNull(order.getSecurity(), "missing security for order " + order);

        // reload the strategy and security to get potential changes
        order.setStrategy(this.strategyDao.load(order.getStrategy().getId()));
        order.setSecurity(this.securityDao.load(order.getSecurity().getId()));

        // reload the order if necessary to get potential changes
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
        order.setDateTime(DateUtil.getCurrentEPTime());

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
            persistOrder(order);
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

            // populate the properties
            try {
                BeanUtil.populate(order, properties);
            } catch (ReflectiveOperationException ex) {
                throw new OrderServiceException(ex);
            }

            internalModifyOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + intId);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        // send the order into the base engine to be correlated with fills
        EngineLocator.instance().getBaseEngine().sendEvent(order);

        // also send the order to the strategy that placed the order
        if (!order.getStrategy().isBase()) {
            EngineLocator.instance().sendEvent(order.getStrategy().getName(), order);
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
        if (orderStatus.getOrder() != null && !orderStatus.getOrder().getStrategy().isBase()) {
            EngineLocator.instance().sendEvent(orderStatus.getOrder().getStrategy().getName(), orderStatus);
        }

        if (!this.commonConfig.isSimulation()) {
            logger.debug("propagated orderStatus: " + orderStatus);

            // only store OrderStatus for non AlgoOrders
            if (!(orderStatus.getOrder() instanceof AlgoOrder)) {

                this.orderPersistStrategy.persistOrderStatus(orderStatus);
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
        if (orderCompletion.getOrder() != null && !orderCompletion.getOrder().getStrategy().isBase()) {
            EngineLocator.instance().sendEvent(orderCompletion.getOrder().getStrategy().getName(), orderCompletion);
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

        if (intId != null && !intId.equals(order.getIntId())) {
            order.setIntId(intId);
        }

        if (extId != null && !extId.equals(order.getExtId())) {
            order.setExtId(extId);
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

    private void sendAlgoOrder(AlgoOrder order) {

        order.setIntId(AlgoIdGenerator.getInstance().getNextOrderId());

        logger.info("send algo order: " + order);

        // progapate the order to all corresponding esper engines
        propagateOrder(order);
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
        EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);

        logger.info("cancelled algo order: " + order);
    }

    private void internalModifyOrder(Order order) {

        if (order instanceof AlgoOrder) {
            throw new UnsupportedOperationException("modification of AlgoOrders are not permitted");
        } else {

            Account account = order.getAccount();
            Validate.notNull(account, "missing account for order: " + order);
            getExternalOrderService(account).modifyOrder((SimpleOrder) order);
            persistOrder(order);
        }
    }

    private void persistOrder(Order order) {

        if (this.commonConfig.isSimulation()) {
            return;
        }

        // save order to the DB by using the corresponding OrderDao
        this.orderPersistStrategy.persistOrder(order);
    }

    /**
     * get the externalOrderService defined by the account
     */
    @SuppressWarnings("unchecked")
    private ExternalOrderService getExternalOrderService(Account account) {

        OrderServiceType orderServiceType;

        if (this.commonConfig.isSimulation()) {
            orderServiceType = OrderServiceType.SIMULATION;
        } else {
            orderServiceType = account.getOrderServiceType();
        }

        Class<ExternalOrderService> orderServiceClass;
        try {
            orderServiceClass = (Class<ExternalOrderService>) Class.forName(orderServiceType.getValue());
        } catch (ClassNotFoundException ex) {
            throw new OrderServiceException("External service class " + orderServiceType.getValue() + " not found", ex);
        }

        Map<String, ExternalOrderService> externalOrderServices = this.applicationContext.getBeansOfType(orderServiceClass);

        // select the proxy
        String name = CollectionUtils.find(externalOrderServices.keySet(), new Predicate<String>() {
            @Override
            public boolean evaluate(String name) {
                return !name.startsWith("ch.algotrader.service");
            }
        });

        ExternalOrderService externalOrderService = externalOrderServices.get(name);

        Validate.notNull(externalOrderService, "externalOrderService was not found: " + orderServiceType);

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
