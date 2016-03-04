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
package ch.algotrader.service.fix.fix42;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix42.Fix42OrderMessageFactory;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.fix.FixOrderServiceImpl;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * Generic FIX 4.2 order service
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class Fix42OrderServiceImpl extends FixOrderServiceImpl implements Fix42OrderService {

    private final OrderRegistry orderRegistry;
    private final Fix42OrderMessageFactory messageFactory;

    public Fix42OrderServiceImpl(
            final String orderServiceType,
            final FixAdapter fixAdapter,
            final Fix42OrderMessageFactory messageFactory,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        super(orderServiceType, fixAdapter, orderPersistenceService, orderDao, accountDao, commonConfig);

        Validate.notNull(orderRegistry, "OpenOrderRegistry is null");
        Validate.notNull(messageFactory, "Fix42OrderMessageFactory is null");

        this.orderRegistry = orderRegistry;
        this.messageFactory = messageFactory;
    }

    protected OrderRegistry getOrderRegistry() {

        return this.orderRegistry;
    }

    @Override
    public void validateOrder(SimpleOrder order) {
        // to be implememented
    }

    @Override
    public void sendOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        String clOrdID = order.getIntId();
        if (clOrdID == null) {

            // assign a new clOrdID
            clOrdID = getFixAdapter().getNextOrderId(order.getAccount());
            order.setIntId(clOrdID);
        }

        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order, clOrdID);

        // broker-specific settings
        prepareSendOrder(order, message);

        this.orderRegistry.add(order);

        // send the message
        sendOrder(order, message);

    }

    @Override
    public void modifyOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        // assign a new clOrdID
        String clOrdID = this.orderRegistry.getNextOrderIdRevision(order.getIntId());

        OrderCancelReplaceRequest replaceRequest = this.messageFactory.createModifyOrderMessage(order, clOrdID);

        // broker-specific settings
        prepareModifyOrder(order, replaceRequest);

        // assign a new clOrdID
        order.setIntId(clOrdID);
        order.setExtId(null);

        this.orderRegistry.add(order);

        // send the message
        sendOrder(order, replaceRequest);

    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        // assign a new clOrdID
        String clOrdID = this.orderRegistry.getNextOrderIdRevision(order.getIntId());

        OrderCancelRequest cancelRequest = this.messageFactory.createOrderCancelMessage(order, clOrdID);

        // broker-specific settings
        prepareCancelOrder(order, cancelRequest);

        // send the message
        sendOrder(order, cancelRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void prepareSendOrder(final SimpleOrder order, final NewOrderSingle newOrder);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void prepareModifyOrder(final SimpleOrder order, final OrderCancelReplaceRequest replaceRequest);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void prepareCancelOrder(final SimpleOrder order, final OrderCancelRequest cancelRequest);

}
