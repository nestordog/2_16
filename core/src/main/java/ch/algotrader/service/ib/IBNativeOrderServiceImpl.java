/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service.ib;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ib.client.Contract;
import com.ib.client.EWrapperMsgGenerator;

import ch.algotrader.adapter.AutoIncrementIdGenerator;
import ch.algotrader.adapter.ib.IBExecution;
import ch.algotrader.adapter.ib.IBExecutions;
import ch.algotrader.adapter.ib.IBOrderMessageFactory;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.adapter.ib.IBUtil;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.SimpleOrderType;
import ch.algotrader.enumeration.Status;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.ExternalOrderService;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.ServiceException;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@InitializationPriority(InitializingServiceType.BROKER_INTERFACE)
@Transactional(propagation = Propagation.SUPPORTS)
public class IBNativeOrderServiceImpl implements ExternalOrderService, InitializingServiceI {

    private static final Logger LOGGER = LogManager.getLogger(IBNativeOrderServiceImpl.class);

    private final IBSession iBSession;
    private final AutoIncrementIdGenerator orderIdGenerator;
    private final OrderRegistry orderRegistry;
    private final IBExecutions iBExecutions;
    private final IBOrderMessageFactory iBOrderMessageFactory;
    private final OrderPersistenceService orderPersistenceService;
    private final OrderDao orderDao;
    private final CommonConfig commonConfig;
    private final Lock lock;
    private final AtomicBoolean firstTime;

    public IBNativeOrderServiceImpl(final IBSession iBSession,
            final AutoIncrementIdGenerator orderIdGenerator,
            final OrderRegistry orderRegistry,
            final IBExecutions iBExecutions,
            final IBOrderMessageFactory iBOrderMessageFactory,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final CommonConfig commonConfig) {

        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(orderIdGenerator, "AutoIncrementIdGenerator is null");
        Validate.notNull(orderRegistry, "OpenOrderRegistry is null");
        Validate.notNull(iBExecutions, "IBExecutions is null");
        Validate.notNull(iBOrderMessageFactory, "IBOrderMessageFactory is null");
        Validate.notNull(orderPersistenceService, "OrderPersistenceService is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(commonConfig, "CommonConfig is null");

        this.iBSession = iBSession;
        this.orderIdGenerator = orderIdGenerator;
        this.orderRegistry = orderRegistry;
        this.iBExecutions = iBExecutions;
        this.iBOrderMessageFactory = iBOrderMessageFactory;
        this.orderPersistenceService = orderPersistenceService;
        this.orderDao = orderDao;
        this.commonConfig = commonConfig;
        this.lock = new ReentrantLock();
        this.firstTime = new AtomicBoolean(false);
    }

    @Override
    public void init() {
        BigDecimal num = this.orderDao.findLastIntOrderIdByServiceType(OrderServiceType.IB_NATIVE.name());
        if (num != null) {
            long currentId = this.orderIdGenerator.updateIfGreater(num.intValue());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(EWrapperMsgGenerator.nextValidId((int) (currentId + 1)));
            }
        }
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

        if (!this.iBSession.isLoggedOn()) {
            throw new ServiceException("IB session is not logged on");
        }

        this.lock.lock();
        try {

            String intId = order.getIntId();
            if (intId == null) {

                intId = Integer.toString((int) this.orderIdGenerator.generateId());
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

            sendOrModifyOrder(order);

            if (this.firstTime.compareAndSet(false, true)) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new ServiceException(ex);
                }
            }
        } finally {
            this.lock.unlock();
        }

    }

    @Override
    public void modifyOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        if (!this.iBSession.isLoggedOn()) {
            throw new ServiceException("IB session is not logged on");
        }

        this.lock.lock();
        try {

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

        } finally {
            this.lock.unlock();
        }

    }

    @Override
    public void cancelOrder(SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        if (!this.iBSession.isLoggedOn()) {
            throw new ServiceException("IB session is not logged on");
        }

        this.lock.lock();
        try {

            this.iBSession.cancelOrder(Integer.parseInt(order.getIntId()));

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("requested order cancellation for order: {}", order);
            }

        } finally {
            this.lock.unlock();
        }

    }

    /**
     * helper method to be used in both sendorder and modifyorder.
     */
    private void sendOrModifyOrder(SimpleOrder order) {

        if (!this.iBSession.isLoggedOn()) {
            throw new ServiceException("IB session is not logged on");
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

        return Integer.toString((int) this.orderIdGenerator.generateId());
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
