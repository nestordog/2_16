/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.fix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.quickfixj.jmx.JmxExporter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import quickfix.CompositeLogFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.FileUtil;
import quickfix.LogFactory;
import quickfix.Message;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SLF4JLogFactory;
import quickfix.Session;
import quickfix.SessionFactory;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import ch.algotrader.ServiceLocator;
import ch.algotrader.cache.EntityHandler;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.esper.EsperManager;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.collection.IntegerMap;

/**
 * Main entry point to Fix sessions.
 * This class an its public methods are available through JMX.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@ManagedResource(objectName = "ch.algotrader.adapter.fix:name=FixSessionFactory")
public class FixSessionFactory implements InitializingBean {

    private static Logger logger = MyLogger.getLogger(EntityHandler.class.getName());

    private SocketInitiator initiator = null;
    private SessionSettings settings = null;
    private IntegerMap<String> orderIds = new IntegerMap<String>();

    /**
     * sets up of the initiator
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        InputStream inputStream = this.getClass().getResourceAsStream("/fix.cfg");
        this.settings = new SessionSettings(inputStream);
        inputStream.close();

        FixApplicationFactory applicationFactory = new FixApplicationFactory(this.settings);

        MessageStoreFactory messageStoreFactory = new FileStoreFactory(this.settings);

        //        Log4FIX log4Fix = Log4FIX.createForLiveUpdates(this.settings);
        //        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(this.settings), new FileLogFactory(this.settings), log4Fix.getLogFactory() });
        //        log4Fix.show();

        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(this.settings), new FileLogFactory(this.settings) });

        MessageFactory messageFactory = new DefaultMessageFactory();

        SessionFactory sessionFactory = new FixMultiApplicationSessionFactory(applicationFactory, messageStoreFactory, logFactory, messageFactory);
        this.initiator = new SocketInitiator(sessionFactory, this.settings);

        JmxExporter exporter = new JmxExporter();
        exporter.register(this.initiator);

        this.initiator.start();
    }

    /**
     * creates an individual session
     */
    public void createSession(OrderServiceType orderServiceType) throws Exception {

        Collection<String> sessionQualifiers = ServiceLocator.instance().getLookupService().getActiveSessionsByOrderServiceType(orderServiceType);
        for (String sessionQualifier : sessionQualifiers) {

            // need to iterate over all sessions definitions in the settings because there is no lookup method
            for (Iterator<SessionID> i = this.settings.sectionIterator(); i.hasNext();) {
                SessionID sessionId = i.next();
                if (sessionId.getSessionQualifier().equals(sessionQualifier)) {
                    Session session = Session.lookupSession(sessionId);
                    if (session != null) {
                        throw new IllegalStateException("existing session with qualifief " + sessionQualifier + " please add 'Inactive=Y' to session config");
                    } else {
                        this.initiator.createDynamicSession(sessionId);
                        createLogonLogoutStatement(sessionId);
                        return;
                    }
                }
            }

            throw new IllegalStateException("SessionID missing in settings " + sessionQualifier);
        }

    }

    /**
     * returns the state of all active sessions
     */
    @ManagedAttribute
    public Map<String, ConnectionState> getConnectionStates() {

        Map<String, ConnectionState> connectionStates = new HashMap<String, ConnectionState>();
        for (SessionID sessionId : this.initiator.getSessions()) {
            Session session = Session.lookupSession(sessionId);
            if (session.isLoggedOn()) {
                connectionStates.put(sessionId.getSessionQualifier(), ConnectionState.LOGGED_ON);
            } else {
                connectionStates.put(sessionId.getSessionQualifier(), ConnectionState.DISCONNECTED);
            }
        }
        return connectionStates;
    }

    /**
     * sends a message to the designated session
     */
    public void sendMessage(Message message, Account account) throws SessionNotFound {

        Session session = Session.lookupSession(getSessionID(account.getSessionQualifier()));
        if (session.isLoggedOn()) {
            session.send(message);
        } else {
            throw new IllegalStateException("message cannot be sent, FIX Session is not logged on " + account.getSessionQualifier());
        }
    }

    /**
     * Gets the next {@code orderId} for the specified {@code account}
     */
    public synchronized String getNextOrderId(Account account) {

        String sessionQualifier = account.getSessionQualifier();
        if (!this.orderIds.containsKey(sessionQualifier)) {
            initOrderId(sessionQualifier);
        }

        int rootOrderId = this.orderIds.increment(sessionQualifier, 1);
        return account.getSessionQualifier().toLowerCase() + rootOrderId + ".0";
    }

    /**
     * Gets the next {@code orderIdVersion} based on the specified {@code order}
     */
    public String getNextOrderIdVersion(Order order) {

        String[] segments = order.getIntId().split("\\.");

        return segments[0] + "." + (Integer.parseInt(segments[1]) + 1);
    }

    /**
     *  gets the currend orderIds for all active sessions
     */
    @ManagedAttribute
    public IntegerMap<String> getOrderIds() {

        return this.orderIds;
    }

    /**
     * sets the orderId for the defined session (will be incremented by 1 for the next order)
     */
    @ManagedOperation
    @ManagedOperationParameters({
        @ManagedOperationParameter(name = "sessionQualifier", description = ""),
        @ManagedOperationParameter(name = "orderId", description = "orderId (will be incremented by 1 for the next order)")
    })
    public void setOrderId(String sessionQualifier, int orderId) {

        this.orderIds.put(sessionQualifier, orderId);
    }

    /**
     * gets an active session by the sessionQualifier
     */
    private SessionID getSessionID(String sessionQualifier) {

        for (SessionID sessionId : this.initiator.getSessions()) {
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

        if (this.initiator.getSettings().isSetting(sessionId, "LogonPattern") && this.initiator.getSettings().isSetting(sessionId, "LogoutPattern")) {

            // TimeZone offset
            String timeZone = this.initiator.getSettings().getString(sessionId, "TimeZone");
            int hourOffset = (TimeZone.getDefault().getOffset(System.currentTimeMillis()) - TimeZone.getTimeZone(timeZone).getOffset(System.currentTimeMillis())) / 3600000;

            // logon
            String[] logonPattern = this.initiator.getSettings().getString(sessionId, "LogonPattern").split("\\W");

            int logonDay = Integer.valueOf(logonPattern[0]);
            int logonHour = Integer.valueOf(logonPattern[1]) + hourOffset;
            int logonMinute = Integer.valueOf(logonPattern[2]);
            int logonSecond = Integer.valueOf(logonPattern[3]);

            if (logonHour >= 24) {
                logonHour -= 24;
                logonDay += 1;
            }

            Object[] logonParams = { logonMinute, logonHour, logonDay, logonSecond };

            EsperManager.deployStatement(StrategyImpl.BASE, "prepared", "FIX_SESSION", sessionId.getSessionQualifier() + "_LOGON", logonParams, new Object() {
                @SuppressWarnings("unused")
                public void update() {
                    Session session = Session.lookupSession(sessionId);
                    session.logon();
                }
            });

            // logout
            String[] logoutPattern = this.initiator.getSettings().getString(sessionId, "LogoutPattern").split("\\W");

            int logoutDay = Integer.valueOf(logoutPattern[0]);
            int logoutHour = Integer.valueOf(logoutPattern[1]) + hourOffset;
            int logoutMinute = Integer.valueOf(logoutPattern[2]);
            int logoutSecond = Integer.valueOf(logoutPattern[3]);

            if (logoutHour >= 24) {
                logoutHour -= 24;
                logoutDay += 1;
            }

            Object[] logoutParams = { logoutMinute, logoutHour, logoutDay, logoutSecond };

            EsperManager.deployStatement(StrategyImpl.BASE, "prepared", "FIX_SESSION", sessionId.getSessionQualifier() + "_LOGOUT", logoutParams, new Object() {
                @SuppressWarnings("unused")
                public void update() {
                    Session session = Session.lookupSession(sessionId);
                    session.logout();
                }
            });
        }
    }

    /**
     * gets the last orderId from the fix message log
     */
    private synchronized void initOrderId(String sessionQualifier) {

        SessionID sessionId = getSessionID(sessionQualifier);
        File file = new File("log" + File.separator + FileUtil.sessionIdFileName(sessionId) + ".messages.log");
        RandomAccessFile fileHandler = null;
        StringBuilder sb = new StringBuilder();

        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = file.length() - 1;

            byte[] bytes = new byte[4];
            byte[] clOrdId = new byte[] { 0x1, 0x31, 0x31, 0x3D };
            long pointer;
            for (pointer = fileLength; pointer != -1; pointer--) {
                fileHandler.seek(pointer);
                fileHandler.read(bytes);
                if (Arrays.equals(bytes, clOrdId)) {
                    break;
                }
            }

            if (pointer == -1) {
                this.orderIds.put(sessionQualifier, 1); // no last orderId
                return;
            }

            for (; pointer != fileLength; pointer++) {
                int readByte = fileHandler.readByte();
                if (readByte == 0x1) {
                    break;
                }
                sb.append((char) readByte);
            }

        } catch (Exception e) {
            logger.error("problem finding last orderId", e);
        } finally {
            if (fileHandler != null) {
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    logger.error("problem finding last orderId", e);
                }
            }
        }

        // strip out the session qualifier
        String value = sb.toString().replaceAll("[a-z]", "");

        // strip out child order number
        if (value.contains(".")) {
            value = value.split("\\.")[0];
        }

        this.orderIds.put(sessionQualifier, Integer.valueOf(value));
    }
}
