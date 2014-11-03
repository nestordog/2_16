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
package ch.algotrader.service.fix.fix42;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix42.Fix42OrderMessageFactory;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.fix.FixOrderServiceImpl;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * Generic FIX 4.2 order service
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class Fix42OrderServiceImpl extends FixOrderServiceImpl implements Fix42OrderService {

    private static final long serialVersionUID = -3694423160435186473L;

    private final Fix42OrderMessageFactory messageFactory;

    public Fix42OrderServiceImpl(final FixAdapter fixAdapter,
            final OrderService orderService,
            final Fix42OrderMessageFactory messageFactory) {

        super(fixAdapter, orderService);

        Validate.notNull(orderService, "Fix42OrderMessageFactory is null");

        this.messageFactory = messageFactory;
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

        // send the message
        sendOrder(order, message, true);

    }

    @Override
    public void modifyOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        // assign a new clOrdID
        String clOrdID = getFixAdapter().getNextOrderIdVersion(order);

        OrderCancelReplaceRequest replaceRequest = this.messageFactory.createModifyOrderMessage(order, clOrdID);

        // broker-specific settings
        prepareModifyOrder(order, replaceRequest);

        // send the message
        sendOrder(order, replaceRequest, true);

    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        // assign a new clOrdID
        String clOrdID = getFixAdapter().getNextOrderIdVersion(order);

        OrderCancelRequest cancelRequest = this.messageFactory.createOrderCancelMessage(order, clOrdID);

        // broker-specific settings
        prepareCancelOrder(order, cancelRequest);

        // send the message
        sendOrder(order, cancelRequest, false);

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
