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
package ch.algotrader.service.ib;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ib.client.Contract;

import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBOrderMessageFactory;
import ch.algotrader.adapter.ib.IBOrderStatus;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OpenOrderRegistry;
import ch.algotrader.service.ServiceException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeOrderServiceImpl implements IBNativeOrderService {

    private static final Logger LOGGER = LogManager.getLogger(IBNativeOrderServiceImpl.class);

    private static boolean firstOrder = true;

    private final IBSession iBSession;
    private final IBIdGenerator iBIdGenerator;
    private final OpenOrderRegistry openOrderRegistry;
    private final IBOrderMessageFactory iBOrderMessageFactory;
    private final Engine serverEngine;

    public IBNativeOrderServiceImpl(final IBSession iBSession,
            final IBIdGenerator iBIdGenerator,
            final OpenOrderRegistry openOrderRegistry,
            final IBOrderMessageFactory iBOrderMessageFactory,
            final Engine serverEngine) {

        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(iBIdGenerator, "IBIdGenerator is null");
        Validate.notNull(openOrderRegistry, "OpenOrderRegistry is null");
        Validate.notNull(iBOrderMessageFactory, "IBOrderMessageFactory is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.iBSession = iBSession;
        this.iBIdGenerator = iBIdGenerator;
        this.openOrderRegistry = openOrderRegistry;
        this.iBOrderMessageFactory = iBOrderMessageFactory;
        this.serverEngine = serverEngine;
    }

    @Override
    public void validateOrder(SimpleOrder order) {

        // validate quantity by allocations (if fa is enabled and no account has been specified)
        //        if (this.faEnabled && (order.getAccount() == null || "".equals(order.getAccount()))) {
        //            long quantity = getAccountService().getQuantityByAllocation(order.getStrategy().getName(), order.getQuantity());
        //            if (quantity != order.getQuantity()) {
        //                OrderQuantityValidationException ex = new OrderQuantityValidationException();
        //                ex.setMaxQuantity(quantity);
        //                throw ex;
        //            }
        //        }
    }

    @Override
    public void sendOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        // Because of an IB bug only one order can be submitted at a time when
        // first connecting to IB, so wait 100ms after the first order
        LOGGER.info("before place");

        this.openOrderRegistry.add(order);

        if (firstOrder) {

            synchronized (this) {
                internalSendOrder(order);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new ServiceException(ex);
                }
                firstOrder = false;
            }

        } else {
            internalSendOrder(order);
        }

    }

    private synchronized void internalSendOrder(SimpleOrder order) {

        String intId = order.getIntId();
        if (intId == null) {

            intId = this.iBIdGenerator.getNextOrderId();
            order.setIntId(intId);
        }

        sendOrModifyOrder(order);

    }

    @Override
    public void modifyOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        this.openOrderRegistry.add(order);

        sendOrModifyOrder(order);

        // send a 0:0 OrderStatus to validate the first SUBMITTED OrderStatus just after the modification
        IBOrderStatus orderStatus = new IBOrderStatus(Status.SUBMITTED, 0, 0, null, order);

        this.serverEngine.sendEvent(orderStatus);

    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        if (!this.iBSession.isLoggedOn()) {
            LOGGER.error("order cannot be cancelled, because IB is not logged on");
            return;
        }

        this.iBSession.cancelOrder(Integer.parseInt(order.getIntId()));

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("requested order cancellation for order: {}", order);
        }

    }

    /**
     * helper method to be used in both sendorder and modifyorder.
     */
    private void sendOrModifyOrder(SimpleOrder order) {

        if (!this.iBSession.isLoggedOn()) {
            LOGGER.error("order cannot be sent / modified, because IB is not logged on");
            return;
        }

        // get the contract
        Contract contract = IBUtil.getContract(order.getSecurity());

        // create the IB order object
        com.ib.client.Order iBOrder = this.iBOrderMessageFactory.createOrderMessage(order, contract);

        // place the order through IBSession
        this.iBSession.placeOrder(Integer.parseInt(order.getIntId()), contract, iBOrder);

        // propagate the order to all corresponding Esper engines
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("placed or modified order: {}", order);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextOrderId(final Account account) {

        return this.iBIdGenerator.getNextOrderId();
    }

    @Override
    public OrderServiceType getOrderServiceType() {
        return OrderServiceType.IB_NATIVE;
    }
}
