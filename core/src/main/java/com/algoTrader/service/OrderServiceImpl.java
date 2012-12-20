package com.algoTrader.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.trade.AlgoOrder;
import com.algoTrader.entity.trade.Fill;
import com.algoTrader.entity.trade.FillImpl;
import com.algoTrader.entity.trade.LimitOrderI;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.entity.trade.OrderStatus;
import com.algoTrader.entity.trade.OrderValidationException;
import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.enumeration.Side;
import com.algoTrader.enumeration.Status;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.BeanUtils;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.vo.OrderStatusVO;

public abstract class OrderServiceImpl extends OrderServiceBase {

    private static final long serialVersionUID = -135731394908062298L;

    private static Logger logger = MyLogger.getLogger(OrderServiceImpl.class.getName());
    private static Logger notificationLogger = MyLogger.getLogger("com.algoTrader.service.NOTIFICATION");

    private @Value("#{T(com.algoTrader.enumeration.MarketChannel).fromString('${misc.defaultMarketChannel}')}") MarketChannel defaultMarketChannel;
    private @Value("${simulation}") boolean simulation;

    private Map<MarketChannel, ExternalOrderService> externalOrderServices = new HashMap<MarketChannel, ExternalOrderService>();

    @Override
    protected void handleInit() {

        for (ExternalOrderService externalOrderService : ServiceLocator.instance().getServices(ExternalOrderService.class)) {

            MarketChannel marketChannel = externalOrderService.getMarketChannel();
            this.externalOrderServices.put(marketChannel, externalOrderService);
        }

        if (!this.externalOrderServices.containsKey(this.defaultMarketChannel)) {
            throw new IllegalStateException("defaultMarketChannel was not found: " + this.defaultMarketChannel);
        }
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
        if (!this.simulation && order instanceof SimpleOrder) {
            getExternalOrderService(order).validateOrder((SimpleOrder) order);
        }

        // TODO add internal validations (i.e. limit, amount, etc.)
    }

    @Override
    protected void handleSendOrder(Order order) throws Exception {

        // reload the strategy and security to get potential changes
        Strategy strategy = getStrategyDao().load(order.getStrategy().getId());
        Security security = getSecurityDao().load(order.getSecurity().getId());

        // also update the strategy and security of the order
        order.setStrategy(strategy);
        order.setSecurity(security);

        // make sure there is no order for the same security / strategy
        if (getOrderDao().findOpenOrderCountByStrategySecurityAndAlgoOrder(strategy.getName(), security.getId(), order.isAlgoOrder()) > 0) {
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

    @Override
    protected Collection<MarketChannel> handleGetMarketChannels() throws Exception {

        return this.externalOrderServices.keySet();
    }

    @Override
    protected MarketChannel handleGetDefaultMarketChannel() throws Exception {

        return this.defaultMarketChannel;
    }

    @Override
    protected void handleSetDefaultMarketChannel(MarketChannel marketChannel) throws Exception {

        if (this.externalOrderServices.containsKey(marketChannel)) {

            this.defaultMarketChannel = marketChannel;
            logger.info("SetDefaultMarketChannel to : " + marketChannel);
        } else {
            throw new OrderServiceException("marketChannel not active: " + marketChannel);
        }
    }

    /**
     * if a marketChannel is defined, return the corresponding orderService otherwise
     * assign the defaultMarketChannel and return it
     */
    private ExternalOrderService getExternalOrderService(Order order) {

        // add the marketChannel if none was defined
        if (order.getMarketChannel() == null) {
            order.setMarketChannel(this.defaultMarketChannel);
        }

        ExternalOrderService externalOrderService = this.externalOrderServices.get(order.getMarketChannel());
        if (externalOrderService != null) {
            return externalOrderService;
        } else {
            throw new IllegalStateException("externalOrderService does not exist " + order.getMarketChannel());
        }
    }
}
