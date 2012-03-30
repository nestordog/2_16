package com.algoTrader.service;

import java.util.List;

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
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;

public abstract class OrderServiceImpl extends OrderServiceBase {

    private static final long serialVersionUID = -197227736784463124L;

    private static Logger logger = MyLogger.getLogger(OrderServiceImpl.class.getName());

    private @Value("${simulation}") boolean simulation;

    @Override
    protected void handleValidateOrder(Order order) throws Exception {

        if (!order.getSecurity().getSecurityFamily().isTradeable()) {
            throw new OrderValidationException(order.getSecurity().getSymbol() + " is not tradeable");
        }

        validateExternalOrder(order);

        // TODO add internal validations (i.e. limit, amount, etc.)
    }

    @Override
    protected void handleSendOrder(Order order) throws Exception {

        // validate the order before sending it
        validateOrder(order);

        if (this.simulation) {

            // process the order internally
            sendInternalOrder(order);
        } else {

            Security security = order.getSecurity();
            Strategy strategy = order.getStrategy();

            // reattach the security & strategy
            security = (Security) HibernateUtil.reattach(this.getSessionFactory(), security);
            strategy = (Strategy) HibernateUtil.reattach(this.getSessionFactory(), strategy);

            // reasociate the potentially merged security & strategy
            order.setSecurity(security);
            order.setStrategy(strategy);

            // use broker specific functionality to execute the order
            sendExternalOrder(order);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void handleSendOrders(List orders) throws Exception {

        for (Order order : (List<Order>) orders) {
            sendOrder(order);
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
            throw new RuntimeException("commission is undefined for " + security.getSymbol());
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

        // propagate the OrderStatus
        propagateOrderStatus(orderStatus);
    }

    @Override
    protected void handleCancelOrder(Order order) throws Exception {

        cancelExternalOrder(order);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleCancelAllOrders() throws Exception {

        List<Order> orders = getRuleService().getAllEvents(StrategyImpl.BASE, "CREATE_WINDOW_OPEN_ORDER");
        for (Order order : orders) {
            cancelExternalOrder(order);
        }
    }

    @Override
    protected void handleModifyOrder(Order order) throws Exception {

        modifyExternalOrder(order);
    }

    @Override
    protected void handlePropagateOrder(Order order) throws Exception {

        // send the order into the base engine to be correlated with fills
        getRuleService().sendEvent(StrategyImpl.BASE, order);

        // also send the order to the strategy that placed the order
        if (!StrategyImpl.BASE.equals(order.getStrategy().getName())) {
            getRuleService().routeEvent(order.getStrategy().getName(), order);
        }
    }

    @Override
    protected void handlePropagateOrderStatus(OrderStatus orderStatus) throws Exception {

        // send the fill to the strategy that placed the corresponding order
        if (!StrategyImpl.BASE.equals(orderStatus.getParentOrder().getStrategy().getName())) {
            getRuleService().routeEvent(orderStatus.getParentOrder().getStrategy().getName(), orderStatus);
        }

        if (!this.simulation) {
            logger.debug("propagated orderStatus: " + orderStatus);
        }
    }
}
