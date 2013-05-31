/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.AlgoOrder;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.entity.trade.FillImpl;
import ch.algotrader.entity.trade.LimitOrderI;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderStatus;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Side;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.EsperManager;
import ch.algotrader.util.BeanUtil;
import ch.algotrader.util.DateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.collection.CollectionUtil;
import ch.algotrader.vo.OrderStatusVO;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class OrderServiceImpl extends OrderServiceBase {

    private static Logger logger = MyLogger.getLogger(OrderServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("ch.algotrader.service.NOTIFICATION");

    private @Value("${simulation}") boolean simulation;

    @Override
    protected void handleValidateOrder(Order order) throws Exception {

        // validate order specific properties
        order.validate();

        // check that the security is tradeable
        if (!order.getSecurity().getSecurityFamily().isTradeable()) {
            throw new OrderValidationException(order.getSecurity() + " is not tradeable");
        }

        // external validation of the order
        if (!this.simulation && order instanceof SimpleOrder) {
            getExternalOrderService(order).validateOrder((SimpleOrder) order);
        }

        // TODO add internal validations (i.e. limit, amount, etc.)
    }

    @Override
    protected void handleSendOrder(Order order) throws Exception {

        // reload the strategy and security to get potential changes
        order.setStrategy(getStrategyDao().load(order.getStrategy().getId()));
        order.setSecurity(getSecurityDao().load(order.getSecurity().getId()));

        // reload the if necessary to get potential changes
        if (order.getAccount() != null) {
            order.setAccount(getAccountDao().load(order.getAccount().getId()));
        }

        // validate the order before sending it
        validateOrder(order);

        // set the dateTime property
        order.setDateTime(DateUtil.getCurrentEPTime());

        if (this.simulation) {
            sendSimulatedOrder(order);
        } else if (order instanceof AlgoOrder) {
            sendAlgoOrder((AlgoOrder) order);
        } else {
            getExternalOrderService(order).sendOrder((SimpleOrder) order);
        }
    }

    @Override
    protected void handleSendOrders(Collection<Order> orders) throws Exception {

        for (Order order : orders) {
            sendOrder(order);
        }
    }

    private void sendSimulatedOrder(Order order) {

        if (order.getQuantity() < 0) {
            throw new IllegalArgumentException("quantity has to be positive");
        }

        Security security = order.getSecurity();

        // create one fill per order
        Fill fill = new FillImpl();
        fill.setDateTime(DateUtil.getCurrentEPTime());
        fill.setExtDateTime(DateUtil.getCurrentEPTime());
        fill.setSide(order.getSide());
        fill.setQuantity(order.getQuantity());

        BigDecimal price = new BigDecimal(0);
        if (order instanceof LimitOrderI) {

            // limitorders are executed at their limit price
            price = ((LimitOrderI) order).getLimit();

        } else {

            // all other orders are executed the the market
            price = security.getCurrentMarketDataEvent().getMarketValue(Side.BUY.equals(order.getSide()) ? Direction.SHORT : Direction.LONG);
        }
        fill.setPrice(price);

        fill.setOrd(order);

        // propagate the fill
        getTransactionService().propagateFill(fill);

        // create the transaction
        getTransactionService().createTransaction(fill);

        // create and OrderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.EXECUTED);
        orderStatus.setFilledQuantity(order.getQuantity());
        orderStatus.setRemainingQuantity(0);
        orderStatus.setOrd(order);

        // send the orderStatus to base
        EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);

        // propagate the OrderStatus to the strategy
        propagateOrderStatus(orderStatus);
    }

    private void sendAlgoOrder(AlgoOrder order) {

        order.setIntId(AlgoIdGenerator.getInstance().getNextOrderId());

        logger.info("send algo order: " + order);

        // progapate the order to all corresponding esper engines
        propagateOrder(order);
    }

    @Override
    protected void handleCancelAllOrders() throws Exception {

        for (Order order : getOrderDao().findAllOpenOrders()) {
            cancelOrder(order);
        }
    }

    @Override
    protected void handleCancelOrder(String intId) throws Exception {

        Order order = getOrderDao().findOpenOrderByIntId(intId);
        if (order != null) {
            internalCancelOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + intId);
        }
    }

    @Override
    protected void handleCancelOrder(Order order) throws Exception {

        // check if order exists
        if (getOrderDao().findOpenOrderByIntId(order.getIntId()) != null) {
            internalCancelOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + order.getIntId());
        }
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
        for (Order childOrder : getOrderDao().findOpenOrdersByParentIntId(order.getIntId())) {
            getExternalOrderService(order).cancelOrder((SimpleOrder) childOrder);
        }

        // get the current OrderStatusVO
        OrderStatusVO orderStatusVO = getOrderStatusDao().findOrderStatusByIntId(order.getIntId());

        // assemble a new OrderStatus Entity
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.CANCELED);
        orderStatus.setFilledQuantity(orderStatusVO.getFilledQuantity());
        orderStatus.setRemainingQuantity(orderStatusVO.getRemainingQuantity());
        orderStatus.setOrd(order);

        // send the orderStatus
        EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);

        logger.info("cancelled algo order: " + order);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void handleModifyOrder(String intId, Map properties) throws Exception {

        Order order = getOrderDao().findOpenOrderByIntId(intId);
        if (order != null) {

            // populte the properties
            BeanUtil.populate(order, properties);

            internalModifyOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + intId);
        }
    }

    @Override
    protected void handleModifyOrder(Order order) throws Exception {

        // check if order exists
        if (getOrderDao().findOpenOrderByIntId(order.getIntId()) != null) {
            internalModifyOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + order.getIntId());
        }
    }

    private void internalModifyOrder(Order order) throws Exception {

        if (order instanceof AlgoOrder) {
            throw new UnsupportedOperationException("modification of AlgoOrders are not permitted");
        } else {
            getExternalOrderService(order).modifyOrder((SimpleOrder) order);
        }
    }

    @Override
    protected void handleSuggestOrder(Order order) throws Exception {

        notificationLogger.info("order " + order);
    }

    @Override
    protected void handlePropagateOrder(Order order) throws Exception {

        // send the order into the base engine to be correlated with fills
        EsperManager.sendEvent(StrategyImpl.BASE, order);

        // also send the order to the strategy that placed the order
        if (!order.getStrategy().isBase()) {
            EsperManager.sendEvent(order.getStrategy().getName(), order);
        }
    }

    @Override
    protected void handlePropagateOrderStatus(OrderStatus orderStatus) throws Exception {

        // send the fill to the strategy that placed the corresponding order
        if (orderStatus.getOrd() != null && !orderStatus.getOrd().getStrategy().isBase()) {
            EsperManager.sendEvent(orderStatus.getOrd().getStrategy().getName(), orderStatus);
        }

        if (!this.simulation) {
            logger.debug("propagated orderStatus: " + orderStatus);
        }
    }

    /**
     * get the externalOrderService defined by the account
     */
    @SuppressWarnings("unchecked")
    private ExternalOrderService getExternalOrderService(Order order) throws Exception {

        if (order.getAccount() == null) {
            throw new IllegalStateException("account missing for order: " + order);
        }

        OrderServiceType orderServiceType = order.getAccount().getOrderServiceType();
        Class<ExternalOrderService> orderServiceClass = (Class<ExternalOrderService>) Class.forName(orderServiceType.getValue());

        ExternalOrderService externalOrderService = CollectionUtil.getSingleElementOrNull(ServiceLocator.instance().getServices(orderServiceClass));

        if (externalOrderService == null) {
            throw new IllegalStateException("externalOrderService was not found: " + orderServiceType);
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

        public String getNextOrderId() {
            return "a" + String.valueOf(this.orderId++);
        }
    }
}
