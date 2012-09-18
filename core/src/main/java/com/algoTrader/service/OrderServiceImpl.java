package com.algoTrader.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.trade.AlgoOrder;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.FillImpl;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.entity.trade.OrderValidationException;
import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.OrderStatusVO;

public abstract class OrderServiceImpl extends OrderServiceBase {

    private static Logger logger = MyLogger.getLogger(OrderServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("com.algoTrader.service.NOTIFICATION");

    private @Value("${simulation}") boolean simulation;
    private @Value("${defaultBroker}") String defaultBroker;

    // injected by Spring
    private Map<String, ExternalOrderService> externalOrderServices = new HashMap<String, ExternalOrderService>();

    public Map<String, ExternalOrderService> getExternalOrderServices() {
        return this.externalOrderServices;
    }

    public void setExternalOrderServices(Map<String, ExternalOrderService> externalOrderServices) {
        this.externalOrderServices = externalOrderServices;
    }

    @Override
    protected void handleValidateOrder(Order order) throws Exception {

        // validate order specific properties
        order.validate();

        // check that the security is tradeable
        if (!order.getSecurity().getSecurityFamily().isTradeable()) {
            throw new OrderValidationException(order.getSecurity() + " is not tradeable");
        }

        // external validation of the order
        if (order instanceof SimpleOrder) {
            getExternalOrderService(order).validateOrder((SimpleOrder) order);
        }

        // TODO add internal validations (i.e. limit, amount, etc.)
    }

    @Override
    protected void handleSendOrder(Order order) throws Exception {

        // reasociate the potentially merged security & strategy
        order.setSecurity((Security) HibernateUtil.reattach(this.getSessionFactory(), order.getSecurity()));
        order.setStrategy((Strategy) HibernateUtil.reattach(this.getSessionFactory(), order.getStrategy()));

        // make sure there is no order for the same security / strategy
        if (getOrderDao().findOpenOrderCountByStrategySecurityAndAlgoOrder(order.getStrategy().getName(), order.getSecurity().getId(), order.isAlgoOrder()) > 0) {
                throw new OrderServiceException("existing " + (order instanceof AlgoOrder ? "AlgoOrder" : "SimpleOrder") + " for " + order.getSecurity() + " strategy " + order.getStrategy());
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

        // in simulation orders are executed at the market
        double price = 0.0;
        if (Side.SELL.equals(order.getSide())) {
            price = security.getLastTick().getBid().doubleValue();

        } else {
            price = security.getLastTick().getAsk().doubleValue();
        }
        fill.setPrice(RoundUtil.getBigDecimal(price));

        // set the commission
        if (security.getSecurityFamily().getExecutionCommission() == null) {
            throw new IllegalStateException("commission is undefined for " + security);
        }

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

        order.setNumber(AlgoIdGenerator.getInstance().getNextOrderId());

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
    protected void handleCancelOrder(int orderNumber) throws Exception {

        Order order = getOrderDao().findOpenOrderByNumber(orderNumber);
        if (order != null) {
            cancelOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + orderNumber);
        }
    }

    @Override
    protected void handleCancelOrder(Order order) throws Exception {

        if (order instanceof AlgoOrder) {
            cancelAlgoOrder((AlgoOrder) order);
        } else {
            getExternalOrderService(order).cancelOrder((SimpleOrder) order);
        }
    }

    private void cancelAlgoOrder(AlgoOrder order) {

        // cancel existing child orders
        for (Order childOrder : getOrderDao().findOpenOrdersByParentNumber(order.getNumber())) {
            getExternalOrderService(order).cancelOrder((SimpleOrder) childOrder);
        }

        // get the current OrderStatusVO
        OrderStatusVO orderStatusVO = getOrderStatusDao().findOrderStatusByNumber(order.getNumber());

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
    protected void handleModifyOrder(int orderNumber, Map properties) throws Exception {

        Order order = getOrderDao().findOpenOrderByNumber(orderNumber);
        if (order != null) {

            // populte the properties
            BeanUtils.populate(order, properties);

            modifyOrder(order);

        } else {
            throw new IllegalArgumentException("order does not exist " + orderNumber);
        }
    }

    @Override
    protected void handleModifyOrder(Order order) throws Exception {

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
        if (!StrategyImpl.BASE.equals(order.getStrategy().getName())) {
            EsperManager.sendEvent(order.getStrategy().getName(), order);
        }
    }

    @Override
    protected void handlePropagateOrderStatus(OrderStatus orderStatus) throws Exception {

        // send the fill to the strategy that placed the corresponding order
        if (orderStatus.getOrd() != null && !StrategyImpl.BASE.equals(orderStatus.getOrd().getStrategy().getName())) {
            EsperManager.sendEvent(orderStatus.getOrd().getStrategy().getName(), orderStatus);
        }

        if (!this.simulation) {
            logger.debug("propagated orderStatus: " + orderStatus);
        }
    }

    /**
     * if a broker is defined, return the corresponding orderService otherwise return the defaultExternalOrderService
     */
    private ExternalOrderService getExternalOrderService(Order order) {

        if (order.getBroker() != null) {
            return getExternalOrderServices().get(order.getBroker().getValue());
        } else {
            return getExternalOrderServices().get(this.defaultBroker);
        }
    }
}
