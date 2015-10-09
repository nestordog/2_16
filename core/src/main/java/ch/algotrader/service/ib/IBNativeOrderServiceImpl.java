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

import ch.algotrader.adapter.ib.IBExecution;
import ch.algotrader.adapter.ib.IBExecutions;
import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBOrderMessageFactory;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.SimpleOrderType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.ExternalOrderService;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.ServiceException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeOrderServiceImpl implements ExternalOrderService {

    private static final Logger LOGGER = LogManager.getLogger(IBNativeOrderServiceImpl.class);

    private static boolean firstOrder = true;

    private final IBSession iBSession;
    private final IBIdGenerator iBIdGenerator;
    private final OrderRegistry orderRegistry;
    private final IBExecutions iBExecutions;
    private final IBOrderMessageFactory iBOrderMessageFactory;
    private final OrderPersistenceService orderPersistenceService;
    private final CommonConfig commonConfig;

    public IBNativeOrderServiceImpl(final IBSession iBSession,
            final IBIdGenerator iBIdGenerator,
            final OrderRegistry orderRegistry,
            final IBExecutions iBExecutions,
            final IBOrderMessageFactory iBOrderMessageFactory,
            final OrderPersistenceService orderPersistenceService,
            final CommonConfig commonConfig) {

        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(iBIdGenerator, "IBIdGenerator is null");
        Validate.notNull(orderRegistry, "OpenOrderRegistry is null");
        Validate.notNull(iBExecutions, "IBExecutions is null");
        Validate.notNull(iBOrderMessageFactory, "IBOrderMessageFactory is null");
        Validate.notNull(orderPersistenceService, "OrderPersistenceService is null");
        Validate.notNull(commonConfig, "CommonConfig is null");

        this.iBSession = iBSession;
        this.iBIdGenerator = iBIdGenerator;
        this.orderRegistry = orderRegistry;
        this.iBExecutions = iBExecutions;
        this.iBOrderMessageFactory = iBOrderMessageFactory;
        this.orderPersistenceService = orderPersistenceService;
        this.commonConfig = commonConfig;
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

        String intId = order.getIntId();
        if (intId == null) {

            intId = this.iBIdGenerator.getNextOrderId();
            order.setIntId(intId);
        }

        this.orderRegistry.add(order);
        IBExecution execution = this.iBExecutions.addNew(intId);

        synchronized (execution) {
            execution.setStatus(Status.OPEN);
        }

        if (!this.commonConfig.isSimulation()) {
            this.orderPersistenceService.persistOrder(order);
        }

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

        sendOrModifyOrder(order);

    }

    @Override
    public void modifyOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        String intId = order.getIntId();
        this.orderRegistry.remove(intId);
        this.orderRegistry.add(order);

        IBExecution execution = this.iBExecutions.addNew(intId);
        synchronized (execution) {
            execution.setStatus(Status.OPEN);
        }

        if (!this.commonConfig.isSimulation()) {
            this.orderPersistenceService.persistOrder(order);
        }

        sendOrModifyOrder(order);

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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOrderServiceType() {
        return OrderServiceType.IB_NATIVE.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TIF getDefaultTIF(final SimpleOrderType type) {
        return TIF.DAY;
    }

}
