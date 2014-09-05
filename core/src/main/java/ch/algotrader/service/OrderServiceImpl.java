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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.AccountDao;
import ch.algotrader.entity.security.SecurityDao;
import ch.algotrader.entity.strategy.StrategyDao;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.LimitOrderDao;
import ch.algotrader.entity.trade.MarketOrder;
import ch.algotrader.entity.trade.MarketOrderDao;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderCompletion;
import ch.algotrader.entity.trade.OrderDao;
import ch.algotrader.entity.trade.OrderPropertyDao;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderStatusDao;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopLimitOrderDao;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderDao;
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

    private ApplicationContext applicationContext;

    private final CommonConfig commonConfig;

    private final OrderDao orderDao;

    private final OrderStatusDao orderStatusDao;

    private final StrategyDao strategyDao;

    private final SecurityDao securityDao;

    private final AccountDao accountDao;

    private final MarketOrderDao marketOrderDao;

    private final LimitOrderDao limitOrderDao;

    private final StopOrderDao stopOrderDao;

    private final StopLimitOrderDao stopLimitOrderDao;

    private final OrderPropertyDao orderPropertyDao;

    public OrderServiceImpl(final CommonConfig commonConfig,
            final OrderDao orderDao,
            final OrderStatusDao orderStatusDao,
            final StrategyDao strategyDao,
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final MarketOrderDao marketOrderDao,
            final LimitOrderDao limitOrderDao,
            final StopOrderDao stopOrderDao,
            final StopLimitOrderDao stopLimitOrderDao,
            final OrderPropertyDao orderPropertyDao) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(orderStatusDao, "OrderStatusDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(marketOrderDao, "MarketOrderDao is null");
        Validate.notNull(limitOrderDao, "LimitOrderDao is null");
        Validate.notNull(stopOrderDao, "StopOrderDao is null");
        Validate.notNull(stopLimitOrderDao, "StopLimitOrderDao is null");
        Validate.notNull(orderPropertyDao, "OrderPropertyDao is null");

        this.commonConfig = commonConfig;
        this.orderDao = orderDao;
        this.orderStatusDao = orderStatusDao;
        this.strategyDao = strategyDao;
        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.marketOrderDao = marketOrderDao;
        this.limitOrderDao = limitOrderDao;
        this.stopOrderDao = stopOrderDao;
        this.stopLimitOrderDao = stopLimitOrderDao;
        this.orderPropertyDao = orderPropertyDao;

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateOrder(final Order order) throws OrderValidationException {

        Validate.notNull(order, "Order is null");

        try {
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
                getExternalOrderService(order).validateOrder((SimpleOrder) order);
            }

            // TODO add internal validations (i.e. limit, amount, etc.)
        } catch (OrderValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void sendOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        try {
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
            validateOrder(order);

            // set the dateTime property
            order.setDateTime(DateUtil.getCurrentEPTime());

            // in case no TIF was specified set DAY
            if (order.getTif() == null) {
                order.setTif(TIF.DAY);
            }

            if (order instanceof AlgoOrder) {
                sendAlgoOrder((AlgoOrder) order);
            } else {
                getExternalOrderService(order).sendOrder((SimpleOrder) order);
                persistOrder(order);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOrders(final Collection<Order> orders) {

        try {
            for (Order order : orders) {
                sendOrder(order);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        try {
            // check if order exists
            if (this.orderDao.findOpenOrderByIntId(order.getIntId()) != null) {
                internalCancelOrder(order);
            } else {
                throw new IllegalArgumentException("order does not exist " + order.getIntId());
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOrder(final String intId) {

        Validate.notNull(intId, "Int id is null");

        try {
            Order order = this.orderDao.findOpenOrderByIntId(intId);
            if (order != null) {
                internalCancelOrder(order);
            } else {
                throw new IllegalArgumentException("order does not exist " + intId);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelAllOrders() {

        try {
            for (Order order : this.orderDao.findAllOpenOrders()) {
                cancelOrder(order);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void modifyOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        try {
            // check if order exists
            if (this.orderDao.findOpenOrderByIntId(order.getIntId()) != null) {
                internalModifyOrder(order);
            } else {
                throw new IllegalArgumentException("order does not exist " + order.getIntId());
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void modifyOrder(final String intId, final Map properties) {

        Validate.notNull(intId, "Int id is null");
        Validate.notNull(properties, "Properties is null");

        try {
            Order order = this.orderDao.findOpenOrderByIntId(intId);
            if (order != null) {

                // populte the properties
                BeanUtil.populate(order, properties);

                internalModifyOrder(order);
            } else {
                throw new IllegalArgumentException("order does not exist " + intId);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        try {
            // send the order into the base engine to be correlated with fills
            EngineLocator.instance().getBaseEngine().sendEvent(order);

            // also send the order to the strategy that placed the order
            if (!order.getStrategy().isBase()) {
                EngineLocator.instance().sendEvent(order.getStrategy().getName(), order);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void propagateOrderStatus(final OrderStatus orderStatus) {

        Validate.notNull(orderStatus, "Order status is null");

        try {
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
            }

            // only store OrderStatus for non AlgoOrders
            if (!(orderStatus.getOrder() instanceof AlgoOrder)) {
                this.orderStatusDao.create(orderStatus);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void propagateOrderCompletion(final OrderCompletion orderCompletion) {

        Validate.notNull(orderCompletion, "Order completion is null");

        try {
            // send the fill to the strategy that placed the corresponding order
            if (orderCompletion.getOrder() != null && !orderCompletion.getOrder().getStrategy().isBase()) {
                EngineLocator.instance().sendEvent(orderCompletion.getOrder().getStrategy().getName(), orderCompletion);
            }

            if (!this.commonConfig.isSimulation()) {
                logger.debug("propagated orderCompletion: " + orderCompletion);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateOrderId(final int id, final String intId, final String extId) {

        try {
            Order order = this.orderDao.load(id);
            if (intId != null && !intId.equals(order.getIntId())) {
                order.setIntId(intId);
            }

            if (extId != null && !extId.equals(order.getExtId())) {
                order.setExtId(extId);
            }
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void suggestOrder(final Order order) {

        Validate.notNull(order, "Order is null");

        try {
            notificationLogger.info("order " + order);
        } catch (Exception ex) {
            throw new OrderServiceException(ex.getMessage(), ex);
        }
    }

    private void sendAlgoOrder(AlgoOrder order) {

        order.setIntId(AlgoIdGenerator.getInstance().getNextOrderId());

        logger.info("send algo order: " + order);

        // progapate the order to all corresponding esper engines
        propagateOrder(order);
    }

    private void internalCancelOrder(Order order) throws Exception {

        if (order instanceof AlgoOrder) {
            cancelAlgoOrder((AlgoOrder) order);
        } else {
            getExternalOrderService(order).cancelOrder((SimpleOrder) order);
        }
    }

    private void cancelAlgoOrder(AlgoOrder order) throws Exception {

        // cancel existing child orders
        for (Order childOrder : this.orderDao.findOpenOrdersByParentIntId(order.getIntId())) {
            getExternalOrderService(order).cancelOrder((SimpleOrder) childOrder);
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

    private void internalModifyOrder(Order order) throws Exception {

        if (order instanceof AlgoOrder) {
            throw new UnsupportedOperationException("modification of AlgoOrders are not permitted");
        } else {
            getExternalOrderService(order).modifyOrder((SimpleOrder) order);
            persistOrder(order);
        }
    }

    private void persistOrder(Order order) {

        // save order to the DB by using the corresponding OrderDao
        if (order instanceof MarketOrder) {
            this.marketOrderDao.create((MarketOrder) order);
        } else if (order instanceof LimitOrder) {
            this.limitOrderDao.create((LimitOrder) order);
        } else if (order instanceof StopOrder) {
            this.stopOrderDao.create((StopOrder) order);
        } else if (order instanceof StopLimitOrder) {
            this.stopLimitOrderDao.create((StopLimitOrder) order);
        }

        // save order properties
        if (order.getOrderProperties() != null && order.getOrderProperties().size() != 0) {
            this.orderPropertyDao.create(order.getOrderProperties().values());
        }
    }

    /**
     * get the externalOrderService defined by the account
     */
    @SuppressWarnings("unchecked")
    private ExternalOrderService getExternalOrderService(Order order) throws Exception {

        OrderServiceType orderServiceType;

        if (this.commonConfig.isSimulation()) {
            orderServiceType = OrderServiceType.SIMULATION;
        } else {
            Validate.notNull(order.getAccount(), "missing account for order: " + order);
            orderServiceType = order.getAccount().getOrderServiceType();
        }

        Class<ExternalOrderService> orderServiceClass = (Class<ExternalOrderService>) Class.forName(orderServiceType.getValue());

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

        public String getNextOrderId() {
            return "a" + String.valueOf(this.orderId++);
        }
    }
}
