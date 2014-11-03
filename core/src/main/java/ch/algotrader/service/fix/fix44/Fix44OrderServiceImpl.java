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
package ch.algotrader.service.fix.fix44;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix44.Fix44OrderMessageFactory;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.fix.FixOrderServiceImpl;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class Fix44OrderServiceImpl extends FixOrderServiceImpl implements Fix44OrderService {

    private static final long serialVersionUID = -3694423160435186473L;

    private final Fix44OrderMessageFactory messageFactory;

    public Fix44OrderServiceImpl(final FixAdapter fixAdapter,
            final OrderService orderService,
            final Fix44OrderMessageFactory messageFactory) {

        super(fixAdapter, orderService);

        Validate.notNull(orderService, "Fix44OrderMessageFactory is null");

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

        OrderCancelReplaceRequest message = this.messageFactory.createModifyOrderMessage(order, clOrdID);

        // broker-specific settings
        prepareModifyOrder(order, message);

        // send the message
        sendOrder(order, message, true);

    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        // get origClOrdID and assign a new clOrdID
        String clOrdID = getFixAdapter().getNextOrderIdVersion(order);

        OrderCancelRequest message = this.messageFactory.createOrderCancelMessage(order, clOrdID);

        // broker-specific settings
        prepareCancelOrder(order, message);

        // send the message
        sendOrder(order, message, false);

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
