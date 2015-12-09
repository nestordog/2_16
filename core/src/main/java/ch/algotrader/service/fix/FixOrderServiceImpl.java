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
package ch.algotrader.service.fix;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.enumeration.SimpleOrderType;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.ServiceException;
import quickfix.FieldNotFound;
import quickfix.Message;
import quickfix.field.MsgType;

/**
 * Generic FIX order service
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@InitializationPriority(InitializingServiceType.BROKER_INTERFACE)
@Transactional(propagation = Propagation.SUPPORTS)
public abstract class FixOrderServiceImpl implements FixOrderService, InitializingServiceI {

    private static final Logger LOGGER = LogManager.getLogger(FixOrderServiceImpl.class);

    private final String orderServiceType;
    private final FixAdapter fixAdapter;
    private final ExternalSessionStateHolder stateHolder;
    private final OrderPersistenceService orderPersistenceService;
    private final OrderDao orderDao;
    private final AccountDao accountDao;
    private final CommonConfig commonConfig;

    public FixOrderServiceImpl(
            final String orderServiceType,
            final FixAdapter fixAdapter,
            final ExternalSessionStateHolder stateHolder,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        Validate.notEmpty(orderServiceType, "OrderServiceType is empty");
        Validate.notNull(fixAdapter, "FixAdapter is null");
        Validate.notNull(stateHolder, "ExternalSessionStateHolder is null");
        Validate.notNull(orderPersistenceService, "OrderPersistenceService is null");
        Validate.notNull(orderDao, "OrderDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(commonConfig, "CommonConfig is null");

        this.orderServiceType = orderServiceType;
        this.fixAdapter = fixAdapter;
        this.stateHolder = stateHolder;
        this.orderPersistenceService = orderPersistenceService;
        this.orderDao = orderDao;
        this.accountDao = accountDao;
        this.commonConfig = commonConfig;
    }

    protected FixAdapter getFixAdapter() {

        return this.fixAdapter;
    }

    protected List<Account> getAllAccounts() {

        return this.accountDao.findByByOrderServiceType(this.orderServiceType);
    }

    protected Set<String> getAllSessionQualifiers() {

        List<Account> accounts = getAllAccounts();
        return accounts.stream()
                .filter(account -> !StringUtils.isEmpty(account.getSessionQualifier()))
                .map(Account::getSessionQualifier)
                .collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {

        Set<String> sessionQualifiers = getAllSessionQualifiers();

        for (String sessionQualifier: sessionQualifiers) {
            BigDecimal orderId = this.orderDao.findLastIntOrderIdBySessionQualifier(sessionQualifier);
            this.fixAdapter.setOrderId(sessionQualifier, orderId != null ? orderId.intValue() : 0);
            if (LOGGER.isDebugEnabled() && orderId != null) {
                LOGGER.debug("Current order count for session {}: {}", sessionQualifier, orderId.intValue());
            }
        }
        this.fixAdapter.createSessionForService(getOrderServiceType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendOrder(final Order order, final Message message) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(message, "Message is null");

        if (!this.stateHolder.isLoggedOn()) {
            throw new ServiceException("Fix session is not logged on");
        }

        String msgType;
        try {
            msgType = message.getHeader().getString(MsgType.FIELD);
        } catch (FieldNotFound ex) {
            throw new ServiceException(ex);
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
