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

import ch.algotrader.adapter.fix.fix42.Fix42OrderMessageFactory;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageFactory;
import ch.algotrader.entity.trade.SimpleOrder;
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
public abstract class Fix42OrderServiceImpl extends Fix42OrderServiceBase {

    private static final long serialVersionUID = -3694423160435186473L;

    private final Fix42OrderMessageFactory messageFactory;

    protected Fix42OrderServiceImpl() {

        this.messageFactory = createMessageFactory();
    }

    // TODO: this is a work-around required due to the existing class hierarchy
    // TODO: Implementation class should be injectable through constructor
    protected Fix42OrderMessageFactory createMessageFactory() {
        return new GenericFix42OrderMessageFactory();
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

        NewOrderSingle newOrder = this.messageFactory.createNewOrderMessage(order, clOrdID);

        // broker-specific settings
        sendOrder(order, newOrder);

        // send the message
        sendOrder(order, newOrder, true);
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order) throws Exception {

        // assign a new clOrdID
        String clOrdID = getFixAdapter().getNextOrderIdVersion(order);

        OrderCancelReplaceRequest replaceRequest = this.messageFactory.createModifyOrderMessage(order, clOrdID);

        // broker-specific settings
        modifyOrder(order, replaceRequest);

        // send the message
        sendOrder(order, replaceRequest, true);
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order) throws Exception {

        // assign a new clOrdID
        String clOrdID = getFixAdapter().getNextOrderIdVersion(order);

        OrderCancelRequest cancelRequest = this.messageFactory.createOrderCancelMessage(order, clOrdID);

        // broker-specific settings
        cancelOrder(order, cancelRequest);

        // send the message
        sendOrder(order, cancelRequest, false);
    }
}
