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

import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;
import ch.algotrader.adapter.fix.fix44.DefaultFix44OrderMessageFactory;
import ch.algotrader.adapter.fix.fix44.Fix44OrderMessageFactory;
import ch.algotrader.entity.trade.SimpleOrder;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class Fix44OrderServiceImpl extends Fix44OrderServiceBase {

    private static final long serialVersionUID = -3694423160435186473L;

    private final Fix44OrderMessageFactory messageFactory;

    protected Fix44OrderServiceImpl() {

        this.messageFactory = createMessageFactory();
    }

    // TODO: this is a work-around required due to the existing class hierarchy
    // TODO: Implementation class should be injectable through constructor
    protected Fix44OrderMessageFactory createMessageFactory() {
        return new DefaultFix44OrderMessageFactory();
    }

    @Override
    protected void handleValidateOrder(SimpleOrder order) throws Exception {
        // to be implememented
    }

    @Override
    protected void handleSendOrder(SimpleOrder order) throws Exception {

        // assign a new clOrdID
        String clOrdID = getFixAdapter().getNextOrderId(order.getAccount());

        order.setIntId(clOrdID);
        NewOrderSingle message = this.messageFactory.createNewOrderMessage(order, clOrdID);

        // broker-specific settings
        sendOrder(order, message);

        // send the message
        sendOrder(order, message);

        // propagate the order
        getOrderService().propagateOrder(order);
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order) throws Exception {

        // assign a new clOrdID
        String clOrdID = getFixAdapter().getNextOrderId(order.getAccount());

        OrderCancelReplaceRequest message = this.messageFactory.createModifyOrderMessage(order, clOrdID);

        // broker-specific settings
        modifyOrder(order, message);

        // send the message
        sendOrder(order, message);

        // propagate the order
        getOrderService().propagateOrder(order);
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order) throws Exception {

        // get origClOrdID and assign a new clOrdID
        String origClOrdID = order.getIntId();
        String clOrdID = getFixAdapter().getNextOrderIdVersion(order);

        OrderCancelRequest message = this.messageFactory.createOrderCancelMessage(order, clOrdID);

        // broker-specific settings
        cancelOrder(order, message);

        // send the message
        sendOrder(order, message);
    }
}
