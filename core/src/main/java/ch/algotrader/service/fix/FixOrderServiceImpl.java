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
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.enumeration.SimpleOrderType;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.service.ExternalServiceException;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.OrderPersistenceService;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.MsgType;

/**
 * Generic FIX order service
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@InitializationPriority(InitializingServiceType.BROKER_INTERFACE)
public abstract class FixOrderServiceImpl implements FixOrderService, InitializingServiceI {

    private static final long serialVersionUID = -1571841567775158540L;

    private static final Logger LOGGER = LogManager.getLogger(FixOrderServiceImpl.class);

    private final String orderServiceType;
    private final FixAdapter fixAdapter;
    private final OrderPersistenceService orderPersistenceService;
    private final CommonConfig commonConfig;

    public FixOrderServiceImpl(
            final String orderServiceType,
            final FixAdapter fixAdapter,
            final OrderPersistenceService orderPersistenceService,
            final CommonConfig commonConfig) {

        Validate.notEmpty(orderServiceType, "OrderServiceType is empty");
        Validate.notNull(fixAdapter, "FixAdapter is null");
        Validate.notNull(orderPersistenceService, "OrderPersistenceService is null");
        Validate.notNull(commonConfig, "CommonConfig is null");

        this.orderServiceType = orderServiceType;
        this.fixAdapter = fixAdapter;
        this.orderPersistenceService = orderPersistenceService;
        this.commonConfig = commonConfig;
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
    public void sendOrder(final Order order, final Message message) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(message, "Message is null");

        String msgType;
        try {
            msgType = message.getHeader().getString(MsgType.FIELD);
        } catch (FieldNotFound ex) {
            throw new ExternalServiceException(ex);
        }

        if (!this.commonConfig.isSimulation()
                && (msgType.equals(MsgType.ORDER_SINGLE) || msgType.equals(MsgType.ORDER_CANCEL_REPLACE_REQUEST))) {
            this.orderPersistenceService.persistOrder(order);
        }

        // send the message to the Fix Adapter
        this.fixAdapter.sendMessage(message, order.getAccount());

        if (msgType.equals(MsgType.ORDER_SINGLE)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("sent order: {}", order);
            }
        } else if (msgType.equals(MsgType.ORDER_CANCEL_REPLACE_REQUEST)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("sent order modification: {}", order);
            }
        } else if (msgType.equals(MsgType.ORDER_CANCEL_REQUEST)) {
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
    public final String getNextOrderId(final Account account) {
        return getFixAdapter().getNextOrderId(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TIF getDefaultTIF(final SimpleOrderType type) {
        return TIF.DAY;
    }

    @Override
    public final String getOrderServiceType() {

        return this.orderServiceType;
    }

}
