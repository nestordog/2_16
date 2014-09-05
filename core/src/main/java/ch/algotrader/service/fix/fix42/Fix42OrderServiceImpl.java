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

import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix42.Fix42OrderMessageFactory;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageFactory;
import ch.algotrader.adapter.fix.fix42.GenericFix42SymbologyResolver;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.fix.FixOrderServiceImpl;

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
            final OrderService orderService) {

        super(fixAdapter, orderService);

        this.messageFactory = createMessageFactory();
    }

    // TODO: this is a work-around required due to the existing class hierarchy
    // TODO: Implementation class should be injectable through constructor
    protected Fix42OrderMessageFactory createMessageFactory() {
        return new GenericFix42OrderMessageFactory(new GenericFix42SymbologyResolver());
    }

    @Override
    public void validateOrder(SimpleOrder order) {
        // to be implememented
    }

    @Override
    public void sendOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        try {

            // assign a new clOrdID
            String clOrdID = getFixAdapter().getNextOrderId(order.getAccount());
            order.setIntId(clOrdID);

            NewOrderSingle newOrder = this.messageFactory.createNewOrderMessage(order, clOrdID);

            // broker-specific settings
            sendOrder(order, newOrder);

            // send the message
            sendOrder(order, newOrder, true);
        } catch (Exception ex) {
            throw new Fix42OrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void modifyOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        try {

            // assign a new clOrdID
            String clOrdID = getFixAdapter().getNextOrderIdVersion(order);

            OrderCancelReplaceRequest replaceRequest = this.messageFactory.createModifyOrderMessage(order, clOrdID);

            // broker-specific settings
            modifyOrder(order, replaceRequest);

            // send the message
            sendOrder(order, replaceRequest, true);
        } catch (Exception ex) {
            throw new Fix42OrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        try {

            // assign a new clOrdID
            String clOrdID = getFixAdapter().getNextOrderIdVersion(order);

            OrderCancelRequest cancelRequest = this.messageFactory.createOrderCancelMessage(order, clOrdID);

            // broker-specific settings
            cancelOrder(order, cancelRequest);

            // send the message
            sendOrder(order, cancelRequest, false);
        } catch (Exception ex) {
            throw new Fix42OrderServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void sendOrder(final SimpleOrder order, final NewOrderSingle newOrder);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void modifyOrder(final SimpleOrder order, final OrderCancelReplaceRequest replaceRequest);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void cancelOrder(final SimpleOrder order, final OrderCancelRequest cancelRequest);

}
