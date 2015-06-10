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
package ch.algotrader.service.fix;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.service.ExternalOrderServiceImpl;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.OrderService;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.StringField;
import quickfix.field.MsgType;

/**
 * Generic FIX order service
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@InitializationPriority(InitializingServiceType.BROKER_INTERFACE)
public abstract class FixOrderServiceImpl extends ExternalOrderServiceImpl implements FixOrderService, InitializingServiceI {

    private static final long serialVersionUID = -1571841567775158540L;

    private static final Logger LOGGER = LogManager.getLogger(FixOrderServiceImpl.class);

    private final FixAdapter fixAdapter;

    private final OrderService orderService;

    public FixOrderServiceImpl(final FixAdapter fixAdapter,
            final OrderService orderService) {

        Validate.notNull(fixAdapter, "FixAdapter is null");
        Validate.notNull(orderService, "OrderService is null");

        this.fixAdapter = fixAdapter;
        this.orderService = orderService;
    }

    protected FixAdapter getFixAdapter() {

        return this.fixAdapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {

        this.fixAdapter.createSession(getOrderServiceType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOrder(final Order order, final Message message, final boolean propagate) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(message, "Message is null");

        // persist the order into the database
        this.orderService.persistOrder(order);

        // propagate the order to all corresponding Esper engines
        if (propagate) {
            this.orderService.propagateOrder(order);
        }

        // send the message to the Fix Adapter
        this.fixAdapter.sendMessage(message, order.getAccount());

        StringField msgType;
        try {
            msgType = message.getHeader().getField(new MsgType());
        } catch (FieldNotFound ex) {
            throw new FixOrderServiceException(ex);
        }

        if (msgType.getValue().equals(MsgType.ORDER_SINGLE)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("sent order: {}", order);
            }
        } else if (msgType.getValue().equals(MsgType.ORDER_CANCEL_REPLACE_REQUEST)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("sent order modification: {}", order);
            }
        } else if (msgType.getValue().equals(MsgType.ORDER_CANCEL_REQUEST)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("sent order cancellation: {}", order);
            }
        } else {
            throw new IllegalArgumentException("unsupported messagetype: " + msgType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextOrderId(final Account account) {
        return getFixAdapter().getNextOrderId(account);
    }

}
