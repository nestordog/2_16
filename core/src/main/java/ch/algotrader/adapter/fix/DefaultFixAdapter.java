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
package ch.algotrader.adapter.fix;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.BrokerAdapterException;
import ch.algotrader.adapter.OrderIdGenerator;
import ch.algotrader.entity.Account;
import ch.algotrader.service.LookupService;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SocketInitiator;

/**
 * Default Implementation of ${@link FixAdapter}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class DefaultFixAdapter implements FixAdapter {

    private static final Logger LOGGER = LogManager.getLogger(AbstractFixApplication.class);

    private final Lock lock;
    private final SocketInitiator socketInitiator;
    private final LookupService lookupService;
    private final FixEventScheduler eventScheduler;
    private final OrderIdGenerator orderIdGenerator;

    public DefaultFixAdapter(
            final SocketInitiator socketInitiator,
            final LookupService lookupService,
            final FixEventScheduler eventScheduler,
            final OrderIdGenerator orderIdGenerator) {
        Validate.notNull(socketInitiator, "SocketInitiator is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(eventScheduler, "FixEventScheduler is null");
        Validate.notNull(orderIdGenerator, "OrderIdGenerator is null");

        this.socketInitiator = socketInitiator;
        this.lookupService = lookupService;
        this.eventScheduler = eventScheduler;
        this.orderIdGenerator = orderIdGenerator;
        this.lock = new ReentrantLock();
    }

    SocketInitiator getSocketInitiator() {
        return socketInitiator;
    }

    OrderIdGenerator getOrderIdGenerator() {
        return orderIdGenerator;
    }

    private SessionID findSessionID(final String sessionQualifier) {

        for (Iterator<SessionID> it = this.socketInitiator.getSettings().sectionIterator(); it.hasNext();) {
            SessionID sessionId = it.next();
            if (sessionId.getSessionQualifier().equals(sessionQualifier)) {
                return sessionId;
            }
        }
        throw new BrokerAdapterException("FIX configuration error: session '" + sessionQualifier + "' not found in settings");
    }

    @Override
    public Session getSession(String sessionQualifier) throws BrokerAdapterException {

        Validate.notEmpty(sessionQualifier, "Session qualifier is empty");

        this.lock.lock();
        try {
            SessionID sessionId = findSessionID(sessionQualifier);
            Session session = Session.lookupSession(sessionId);
            if (session == null) {
                throw new BrokerAdapterException("FIX configuration error: session '" + sessionQualifier + "' not found in settings");
            }
            return session;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * creates an individual session
     */
    @Override
    public void createSessionForService(String orderServiceType) throws BrokerAdapterException {

        Validate.notEmpty(orderServiceType, "Order service type is empty");

        Collection<String> sessionQualifiers = this.lookupService.getActiveSessionsByOrderServiceType(orderServiceType);
        if (sessionQualifiers == null || sessionQualifiers.isEmpty()) {

            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("There are no active sessions for order service type {}", orderServiceType);
            }
            return;
        }
        for (String sessionQualifier : sessionQualifiers) {
            if (sessionQualifier != null) {
                createSession(sessionQualifier);
            }
        }
    }

    @Override
    public void openSessionForService(String orderServiceType) throws BrokerAdapterException {

        Validate.notEmpty(orderServiceType, "Order service type is empty");

        Collection<String> sessionQualifiers = this.lookupService.getActiveSessionsByOrderServiceType(orderServiceType);
        if (sessionQualifiers == null || sessionQualifiers.isEmpty()) {

            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("There are no active sessions for order service type {}", orderServiceType);
            }
            return;
        }
        for (String sessionQualifier : sessionQualifiers) {
            openSession(sessionQualifier);
        }
    }

    private Session createSessionInternal(String sessionQualifier, boolean createNew) throws BrokerAdapterException {

        this.lock.lock();
        try {
            SessionID sessionId = findSessionID(sessionQualifier);
            Session session = Session.lookupSession(sessionId);
            if (session != null) {
                if (createNew) {
                    throw new BrokerAdapterException("FIX configuration error: " +
                            "existing session with qualifier '" + sessionQualifier + "' please add 'Inactive=Y' to session config");
                }
            } else {

                try {
                    this.socketInitiator.createDynamicSession(sessionId);
                    if (this.eventScheduler != null) {

                        createLogonLogoutStatement(sessionId);
                    }
                } catch (FieldConvertError | ConfigError ex) {
                    throw new BrokerAdapterException("FIX configuration error: " + ex.getMessage(), ex);
                }
            }
            return session;
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void createSession(String sessionQualifier) throws BrokerAdapterException {

        Validate.notEmpty(sessionQualifier, "Session qualifier is empty");

        createSessionInternal(sessionQualifier, true);
    }

    @Override
    public void openSession(String sessionQualifier) throws BrokerAdapterException {

        Validate.notEmpty(sessionQualifier, "Session qualifier is empty");

        createSessionInternal(sessionQualifier, false);
    }

    @Override
    public void closeSession(String sessionQualifier) throws BrokerAdapterException {

        Validate.notEmpty(sessionQualifier, "Session qualifier is empty");

        this.lock.lock();
        try {
            SessionID sessionId = findSessionID(sessionQualifier);
            Session session = Session.lookupSession(sessionId);
            if (session != null) {
                session.close();
            }
        } catch (IOException ex) {
            throw new BrokerAdapterException(ex.getMessage(), ex);
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * sends a message to the designated session for the given account
     */
    @Override
    public void sendMessage(Message message, Account account) throws BrokerAdapterException {

        String sessionQualifier = account.getSessionQualifier();
        Validate.notNull(sessionQualifier, "no session qualifier defined for account " + account);

        Session session = Session.lookupSession(findSessionID(sessionQualifier));
        if (session.isLoggedOn()) {
            session.send(message);
        } else {
            throw new BrokerAdapterException("Message cannot be sent: session '" + sessionQualifier + "' is not logged on");
        }
    }

    /**
     * sends a message to the designated session
     */
    @Override
    public void sendMessage(Message message, String sessionQualifier) throws BrokerAdapterException {

        Validate.notEmpty(sessionQualifier, "Session qualifier is empty");

        Session session = Session.lookupSession(findSessionID(sessionQualifier));
        if (session.isLoggedOn()) {
            session.send(message);
        } else {
            throw new BrokerAdapterException("Message cannot be sent: session '" + sessionQualifier + "' is not logged on");
        }
    }

    /**
     * Gets the next {@code orderId} for the specified {@code account}
     */
    @Override
    public String getNextOrderId(Account account) {

        Validate.notNull(account, "Account is null");
        Validate.notNull(account.getSessionQualifier(), "no session qualifier defined for account " + account);

        String sessionQualifier = account.getSessionQualifier();
        return this.orderIdGenerator.getNextOrderId(sessionQualifier);
    }

    /**
     * creates an Logon/Logoff statements for fix sessions with weekly logon/logoff defined
     */
    private void createLogonLogoutStatement(final SessionID sessionId) throws ConfigError, FieldConvertError {

        if (this.socketInitiator.getSettings().isSetting(sessionId, "LogonPattern") && this.socketInitiator.getSettings().isSetting(sessionId, "LogoutPattern")) {

            // TimeZone offset
            String timeZone = this.socketInitiator.getSettings().getString(sessionId, "TimeZone");
            int hourOffset = (TimeZone.getDefault().getOffset(System.currentTimeMillis()) - TimeZone.getTimeZone(timeZone).getOffset(System.currentTimeMillis())) / 3600000;

            // logon
            String[] logonPattern = this.socketInitiator.getSettings().getString(sessionId, "LogonPattern").split("\\W");

            int logonDay = Integer.valueOf(logonPattern[0]);
            int logonHour = Integer.valueOf(logonPattern[1]) + hourOffset;
            int logonMinute = Integer.valueOf(logonPattern[2]);
            int logonSecond = Integer.valueOf(logonPattern[3]);

            if (logonHour >= 24) {
                logonHour -= 24;
                logonDay += 1;
            }

            this.eventScheduler.scheduleLogon(sessionId, new EventPattern(logonDay, logonHour, logonMinute, logonSecond));

            // logout
            String[] logoutPattern = this.socketInitiator.getSettings().getString(sessionId, "LogoutPattern").split("\\W");

            int logoutDay = Integer.valueOf(logoutPattern[0]);
            int logoutHour = Integer.valueOf(logoutPattern[1]) + hourOffset;
            int logoutMinute = Integer.valueOf(logoutPattern[2]);
            int logoutSecond = Integer.valueOf(logoutPattern[3]);

            if (logoutHour >= 24) {
                logoutHour -= 24;
                logoutDay += 1;
            }

            this.eventScheduler.scheduleLogout(sessionId, new EventPattern(logoutDay, logoutHour, logoutMinute, logoutSecond));
        }
    }

    public void setOrderId(String sessionQualifier, int orderId) {

        Validate.notEmpty(sessionQualifier, "Session qualifier is empty");

        this.orderIdGenerator.setOrderId(sessionQualifier, orderId);
    }

}
