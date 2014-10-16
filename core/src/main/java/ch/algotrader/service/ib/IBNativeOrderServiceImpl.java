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
import org.apache.log4j.Logger;

import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBOrderMessageFactory;
import ch.algotrader.adapter.ib.IBOrderStatus;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.ExternalOrderServiceImpl;
import ch.algotrader.service.OrderService;
import ch.algotrader.util.MyLogger;

import com.ib.client.Contract;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeOrderServiceImpl extends ExternalOrderServiceImpl implements IBNativeOrderService {

    private static Logger logger = MyLogger.getLogger(IBNativeOrderServiceImpl.class.getName());

    private static boolean firstOrder = true;

    private final IBSession iBSession;

    private final IBIdGenerator iBIdGenerator;

    private final IBOrderMessageFactory iBOrderMessageFactory;

    private final OrderService orderService;

    public IBNativeOrderServiceImpl(final IBSession iBSession,
            final IBIdGenerator iBIdGenerator,
            final IBOrderMessageFactory iBOrderMessageFactory,
            final OrderService orderService) {

        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(iBIdGenerator, "IBIdGenerator is null");
        Validate.notNull(iBOrderMessageFactory, "IBConfig is null");
        Validate.notNull(orderService, "OrderService is null");

        this.iBSession = iBSession;
        this.iBIdGenerator = iBIdGenerator;
        this.iBOrderMessageFactory = iBOrderMessageFactory;
        this.orderService = orderService;
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
        logger.info("before place");

        if (firstOrder) {

            synchronized (this) {
                internalSendOrder(order);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IBNativeOrderServiceException(ex);
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

        sendOrModifyOrder(order);

        // send a 0:0 OrderStatus to validate the first SUBMITTED OrderStatus just after the modification
        IBOrderStatus orderStatus = new IBOrderStatus(Status.SUBMITTED, 0, 0, null, order);

        EngineLocator.instance().getBaseEngine().sendEvent(orderStatus);

    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        if (!this.iBSession.getLifecycle().isLoggedOn()) {
            logger.error("order cannot be cancelled, because IB is not logged on");
            return;
        }

        this.iBSession.cancelOrder(Integer.parseInt(order.getIntId()));

        logger.info("requested order cancellation for order: " + order);

    }

    /**
     * helper method to be used in both sendorder and modifyorder.
     */
    private void sendOrModifyOrder(SimpleOrder order) {

        if (!this.iBSession.getLifecycle().isLoggedOn()) {
            logger.error("order cannot be sent / modified, because IB is not logged on");
            return;
        }

        // get the contract
        Contract contract = IBUtil.getContract(order.getSecurityInitialized());

        // create the IB order object
        com.ib.client.Order iBOrder = this.iBOrderMessageFactory.createOrderMessage(order, contract);

        // progapate the order to all corresponding esper engines
        this.orderService.propagateOrder(order);

        // place the order through IBSession
        this.iBSession.placeOrder(Integer.parseInt(order.getIntId()), contract, iBOrder);

        logger.info("placed or modified order: " + order);
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
