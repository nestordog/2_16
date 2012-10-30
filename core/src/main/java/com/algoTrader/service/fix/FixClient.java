package com.algoTrader.service.fix;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import org.quickfixj.jmx.JmxExporter;
import org.springframework.beans.factory.InitializingBean;

import quickfix.CompositeLogFactory;
import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
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

import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.esper.EsperManager;

public class FixClient implements InitializingBean {

    private SocketInitiator initiator = null;
    private SessionSettings settings = null;

    @Override
    public void afterPropertiesSet() throws Exception {

        InputStream inputStream = this.getClass().getResourceAsStream("/fix.cfg");
        this.settings = new SessionSettings(inputStream);
        inputStream.close();

        FixApplicationFactory applicationFactory = new FixApplicationFactory(this.settings);

        MessageStoreFactory messageStoreFactory = new FileStoreFactory(this.settings);

        //    Log4FIX log4Fix = Log4FIX.createForLiveUpdates(settings);
        //    LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(settings), new FileLogFactory(settings), log4Fix.getLogFactory() });
        //    log4Fix.show();

        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(this.settings), new FileLogFactory(this.settings) });

        MessageFactory messageFactory = new DefaultMessageFactory();

        SessionFactory sessionFactory = new FixMultiApplicationSessionFactory(applicationFactory, messageStoreFactory, logFactory, messageFactory);
        this.initiator = new SocketInitiator(sessionFactory, this.settings);

        JmxExporter exporter = new JmxExporter();
        exporter.register(this.initiator);

        this.initiator.start();
    }

    public void createSession(MarketChannel marketChannel) throws Exception {

        for (final Iterator<SessionID> i = this.settings.sectionIterator(); i.hasNext();) {
            SessionID sessionId = i.next();
            if (sessionId.getSessionQualifier().equals(marketChannel.toString())) {
                this.initiator.createDynamicSession(sessionId);
                createLogonLogoutStatement(sessionId);
                return;
            }
        }

        throw new IllegalStateException("SessionID missing in settings " + marketChannel);
    }

    private void createLogonLogoutStatement(final SessionID sessionId) throws ConfigError, FieldConvertError {

        if (this.initiator.getSettings().isSetting(sessionId, "LogonPattern") && this.initiator.getSettings().isSetting(sessionId, "LogoutPattern")) {

            // TimeZone offset
            String timeZone = this.initiator.getSettings().getString(sessionId, "TimeZone");
            int hourOffset = (TimeZone.getDefault().getOffset(System.currentTimeMillis()) - TimeZone.getTimeZone(timeZone).getOffset(System.currentTimeMillis())) / 3600000;

            // logon
            String[] logonPattern = this.initiator.getSettings().getString(sessionId, "LogonPattern").split("\\W");
            Object[] logonParams = { Integer.valueOf(logonPattern[2]), Integer.valueOf(logonPattern[1]) + hourOffset, Integer.valueOf(logonPattern[0]), Integer.valueOf(logonPattern[3]) };

            EsperManager.deployStatement(StrategyImpl.BASE, "prepared", "FIX_SESSION", sessionId.getSessionQualifier() + "_LOGON", logonParams, new Object() {
                @SuppressWarnings("unused")
                public void update() {
                    Session session = Session.lookupSession(sessionId);
                    session.logon();
                }
            });

            // logout
            String[] logoutPattern = this.initiator.getSettings().getString(sessionId, "LogoutPattern").split("\\W");
            Object[] logoutParams = { Integer.valueOf(logoutPattern[2]), Integer.valueOf(logoutPattern[1]) + hourOffset, Integer.valueOf(logoutPattern[0]), Integer.valueOf(logoutPattern[3]) };

            EsperManager.deployStatement(StrategyImpl.BASE, "prepared", "FIX_SESSION", sessionId.getSessionQualifier() + "_LOGOUT", logoutParams, new Object() {
                @SuppressWarnings("unused")
                public void update() {
                    Session session = Session.lookupSession(sessionId);
                    session.logout();
                }
            });
        }
    }

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

    public ConnectionState getConnectionState(MarketChannel marketChannel) {

        ConnectionState connectionState = getConnectionStates().get(marketChannel.getValue());
        if (connectionState != null) {
            return connectionState;
        } else {
            throw new IllegalStateException("no FIX Session available for " + marketChannel);
        }
    }

    public void sendMessage(Message message, MarketChannel marketChannel) throws SessionNotFound {

        for (SessionID sessionId : this.initiator.getSessions()) {
            if (sessionId.getSessionQualifier().equals(marketChannel.getValue())) {
                Session session = Session.lookupSession(sessionId);
                if (session.isLoggedOn()) {
                    session.send(message);
                } else {
                    throw new IllegalStateException("message cannot be sent, FIX Session is not logged on " + marketChannel);
                }
                return;
            }
        }
        throw new IllegalStateException("message cannot be sent, FIX Session does not exist " + marketChannel);
    }
}
