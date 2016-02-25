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
package ch.algotrader.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.trade.OrderValidationException;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.esper.Engine;
import ch.algotrader.util.BeanUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class SimpleOrderServiceImpl implements SimpleOrderService {

    private final CommonConfig commonConfig;

    private final Engine serverEngine;

    private final Map<String, ExternalOrderService> externalOrderServiceMap;

    public SimpleOrderServiceImpl(final CommonConfig commonConfig,
                                  final Engine serverEngine,
                                  final Map<String, ExternalOrderService> externalOrderServiceMap) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(serverEngine, "Engine is null");

        this.commonConfig = commonConfig;
        this.serverEngine = serverEngine;
        this.externalOrderServiceMap = new ConcurrentHashMap<>(externalOrderServiceMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateOrder(final SimpleOrder order) throws OrderValidationException {

        Validate.notNull(order, "Order is null");

        // validate general properties
        if (order.getSide() == null) {
            throw new OrderValidationException("Missing order side: " + order);
        }
        if (order.getQuantity() <= 0) {
            throw new OrderValidationException("Order quantity cannot be zero or negative: " + order);
        }
        if (order.getAccount() == null) {
            throw new OrderValidationException("Missing order account: " + order);
        }

        // validate order specific properties
        order.validate();

        // check that the security is tradeable
        Security security = order.getSecurity();
        if (!security.getSecurityFamily().isTradeable()) {
            throw new OrderValidationException(security + " is not tradeable: " + order);
        }

        getExternalOrderService(order.getAccount()).validateOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOrder(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        // validate the order before sending it
        try {
            validateOrder(order);
        } catch (OrderValidationException ex) {
            throw new ServiceException(ex);
        }

        Account account = order.getAccount();
        if (account == null) {
            throw new ServiceException("Order with missing account");
        }
        if (order.getDateTime() == null) {
            order.setDateTime(this.serverEngine.getCurrentTime());
        }
        if (order.getTif() == null) {
            order.setTif(TIF.DAY);
        }
        ExternalOrderService externalOrderService = getExternalOrderService(account);
        externalOrderService.sendOrder(order);

        this.serverEngine.sendEvent(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelOrder(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        Account account = order.getAccount();
        if (account == null) {
            throw new ServiceException("Order with missing account");
        }

        ExternalOrderService externalOrderService = getExternalOrderService(account);
        externalOrderService.cancelOrder(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyOrder(final SimpleOrder order) {

        Validate.notNull(order, "Order is null");

        SimpleOrder newOrder;
        if (order.getId() != 0) {
            try {
                newOrder = BeanUtil.clone(order);
                newOrder.setId(0);
            } catch (ReflectiveOperationException ex) {
                throw new ServiceException(ex);
            }
        } else {
            newOrder = order;
        }

        Account account = newOrder.getAccount();
        if (account == null) {
            throw new ServiceException("Order with missing account");
        }
        if (order.getDateTime() == null) {
            order.setDateTime(this.serverEngine.getCurrentTime());
        }
        if (order.getTif() == null) {
            order.setTif(TIF.DAY);
        }
        ExternalOrderService externalOrderService = getExternalOrderService(account);
        externalOrderService.modifyOrder(newOrder);

        this.serverEngine.sendEvent(newOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNextOrderId(final Account account) {

        return getExternalOrderService(account).getNextOrderId(account);
    }

    private ExternalOrderService getExternalOrderService(Account account) {

        Validate.notNull(account, "Account is null");

        String orderServiceType;

        if (this.commonConfig.isSimulation()) {
            orderServiceType = OrderServiceType.SIMULATION.name();
        } else {
            orderServiceType = account.getOrderServiceType();
        }

        ExternalOrderService externalOrderService = this.externalOrderServiceMap.get(orderServiceType);
        if (externalOrderService == null) {
            throw new ServiceException("No ExternalOrderService found for service type " + orderServiceType);
        }
        return externalOrderService;
    }

}
