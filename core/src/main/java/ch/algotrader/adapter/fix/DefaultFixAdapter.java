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
package ch.algotrader.adapter.fix;

import java.util.Collection;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.ordermgmt.OrderIdGenerator;
import ch.algotrader.service.LookupService;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SocketInitiator;

/**
 * Default Implementation of ${@link FixAdapter}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultFixAdapter implements FixAdapter {

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

    /**
     * creates an individual session
     */
    @Override
    public void createSession(OrderServiceType orderServiceType) throws Exception {

        Collection<String> sessionQualifiers = this.lookupService.getActiveSessionsByOrderServiceType(orderServiceType);
        for (String sessionQualifier : sessionQualifiers) {
            createSession(sessionQualifier);
        }
    }

    @Override
    public void openSession(OrderServiceType orderServiceType) throws Exception {

        Collection<String> sessionQualifiers = this.lookupService.getActiveSessionsByOrderServiceType(orderServiceType);
        for (String sessionQualifier : sessionQualifiers) {
            openSession(sessionQualifier);
        }
    }

    private void createSessionInternal(String sessionQualifier, boolean createNew) throws Exception {

        this.lock.lock();
        try {
            // need to iterate over all sessions definitions in the settings because there is no lookup method
            for (Iterator<SessionID> i = this.socketInitiator.getSettings().sectionIterator(); i.hasNext();) {
                SessionID sessionId = i.next();
                if (sessionId.getSessionQualifier().equals(sessionQualifier)) {
                    Session session = Session.lookupSession(sessionId);
                    if (session != null) {
                        if (createNew) {
                            throw new IllegalStateException("existing session with qualifier " + sessionQualifier + " please add 'Inactive=Y' to session config");
                        }
                    } else {

                        this.socketInitiator.createDynamicSession(sessionId);
                        if (this.eventScheduler != null) {

                            createLogonLogoutStatement(sessionId);
                        }
                    }
                    return;
                }
            }

            throw new IllegalStateException("SessionID missing in settings " + sessionQualifier);

        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public void createSession(String sessionQualifier) throws Exception {

        createSessionInternal(sessionQualifier, true);
    }

    @Override
    public void openSession(String sessionQualifier) throws Exception {

        createSessionInternal(sessionQualifier, false);
    }

    /**
     * sends a message to the designated session for the given account
     */
    @Override
    public void sendMessage(Message message, Account account) throws SessionNotFound {

        Validate.notNull(account.getSessionQualifier(), "no session qualifier defined for account " + account);

        Session session = Session.lookupSession(getSessionID(account.getSessionQualifier()));
        if (session.isLoggedOn()) {
            session.send(message);
        } else {
            throw new IllegalStateException("message cannot be sent, FIX Session is not logged on " + account.getSessionQualifier());
        }
    }

    /**
     * sends a message to the designated session
     */
    @Override
    public void sendMessage(Message message, String sessionQualifier) throws SessionNotFound {

        Session session = Session.lookupSession(getSessionID(sessionQualifier));
        if (session.isLoggedOn()) {
            session.send(message);
        } else {
            throw new IllegalStateException("message cannot be sent, FIX Session is not logged on " + sessionQualifier);
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
     * Gets the next {@code orderIdVersion} based on the specified {@code order}
     */
    @Override
    public String getNextOrderIdVersion(Order order) {

        String[] segments = order.getIntId().split("\\.");

        return segments[0] + "." + (Integer.parseInt(segments[1]) + 1);
    }

    /**
     * gets an active session by the sessionQualifier
     */
    private SessionID getSessionID(String sessionQualifier) {

        for (SessionID sessionId : this.socketInitiator.getSessions()) {
            if (sessionId.getSessionQualifier().equals(sessionQualifier)) {
                return sessionId;
            }
        }
        throw new IllegalStateException("FIX Session does not exist " + sessionQualifier);
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

}
