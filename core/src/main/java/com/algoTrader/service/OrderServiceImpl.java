package com.algoTrader.service;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.FillImpl;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.entity.trade.OrderValidationException;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.espertech.esper.collection.Pair;

public abstract class OrderServiceImpl extends OrderServiceBase {

    private static Logger logger = MyLogger.getLogger(OrderServiceImpl.class.getName());
    private static Logger mailLogger = MyLogger.getLogger(OrderServiceImpl.class.getName() + ".MAIL");

    private @Value("${simulation}") boolean simulation;

    @Override
    protected void handleValidateOrder(Order order) throws Exception {

        if (!order.getSecurity().getSecurityFamily().isTradeable()) {
            throw new OrderValidationException(order.getSecurity() + " is not tradeable");
        }

        validateExternalOrder(order);

        // TODO add internal validations (i.e. limit, amount, etc.)
    }

    @Override
    protected void handleSendOrder(Order order) throws Exception {

        // reasociate the potentially merged security & strategy
        order.setSecurity((Security) HibernateUtil.reattach(this.getSessionFactory(), order.getSecurity()));
        order.setStrategy((Strategy) HibernateUtil.reattach(this.getSessionFactory(), order.getStrategy()));

        // make sure there is no order for the same security / strategy
        if (EsperManager.executeQuery(StrategyImpl.BASE,
                "select number from OpenOrderWindow " +
                "where security.id = " + order.getSecurity().getId() +
                " and strategy.id = " + order.getStrategy().getId()).size() > 0) {
            throw new OrderServiceException("existing order for " + order.getSecurity() + " strategy " + order.getStrategy());
        }

        // validate the order before sending it
        validateOrder(order);

        if (this.simulation) {

            // process the order internally
            sendInternalOrder(order);
        } else {

            // use broker specific functionality to execute the order
            sendExternalOrder(order);
        }
    }

    private void sendInternalOrder(Order order) {

        if (order.getQuantity() < 0) {
            throw new IllegalArgumentException("quantity has to be positive");
        }

        Security security = order.getSecurity();

        // create one fill per order
        Fill fill = new FillImpl();
        fill.setDateTime(DateUtil.getCurrentEPTime());
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
        if (security.getSecurityFamily().getCommission() == null) {
            throw new IllegalStateException("commission is undefined for " + security);
        }

        fill.setParentOrder(order);

        // propagate the fill
        getTransactionService().propagateFill(fill);

        // create the transaction
        getTransactionService().createTransaction(fill);

        // create and OrderStatus
        OrderStatus orderStatus = OrderStatus.Factory.newInstance();
        orderStatus.setStatus(Status.EXECUTED);
        orderStatus.setFilledQuantity(order.getQuantity());
        orderStatus.setRemainingQuantity(0);
        orderStatus.setParentOrder(order);

        // send the orderStatus to base
        EsperManager.sendEvent(StrategyImpl.BASE, orderStatus);

        // propagate the OrderStatus to the strategy
        propagateOrderStatus(orderStatus);
    }

    @Override
    protected void handleCancelOrder(Order order) throws Exception {

        cancelExternalOrder(order);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleCancelOrder(long orderNumber) throws Exception {

        Order order = ((Pair<Order, Map<?, ?>>) EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where number = " + orderNumber)).getFirst();
        if (order != null) {
            cancelExternalOrder(order);
        } else {
            throw new IllegalArgumentException("order does not exist " + orderNumber);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleCancelAllOrders() throws Exception {

        List<Pair<Order, Map<?, ?>>> pairs = EsperManager.executeQuery(StrategyImpl.BASE, "select * from OpenOrderWindow");
        for (Pair<Order, Map<?, ?>> pair : pairs) {
            cancelExternalOrder(pair.getFirst());
        }
    }

    @Override
    protected void handleModifyOrder(Order order) throws Exception {

        modifyExternalOrder(order);
    }

    @Override
    protected void handleSuggestOrder(Order order) throws Exception {

        mailLogger.info("order " + order);
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
        if (orderStatus.getParentOrder() != null && !StrategyImpl.BASE.equals(orderStatus.getParentOrder().getStrategy().getName())) {
            EsperManager.sendEvent(orderStatus.getParentOrder().getStrategy().getName(), orderStatus);
            if (!this.simulation) {
                logger.debug("propagated orderStatus: " + orderStatus);
            }
        }
    }
}
