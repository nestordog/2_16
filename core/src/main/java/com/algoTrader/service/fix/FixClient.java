package com.algoTrader.service.fix;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.quickfixj.jmx.JmxExporter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import quickfix.CompositeLogFactory;
import quickfix.DefaultMessageFactory;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
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

import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.MarketChannel;

public class FixClient implements InitializingBean {

    private @Value("${simulation}") boolean simulation;
    private @Value("${fix.enabled}") boolean fixEnabled;

    private Initiator initiator = null;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (this.simulation || !this.fixEnabled) {
            return;
        }

        InputStream inputStream = this.getClass().getResourceAsStream("/fix.cfg");
        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();

        FixApplicationFactory applicationFactory = new FixApplicationFactory(settings);

        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);

        //    Log4FIX log4Fix = Log4FIX.createForLiveUpdates(settings);
        //    LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(settings), new FileLogFactory(settings), log4Fix.getLogFactory() });
        //    log4Fix.show();

        LogFactory logFactory = new CompositeLogFactory(new LogFactory[] { new SLF4JLogFactory(settings), new FileLogFactory(settings) });

        MessageFactory messageFactory = new DefaultMessageFactory();

        SessionFactory sessionFactory = new FixMultiApplicationSessionFactory(applicationFactory, messageStoreFactory, logFactory, messageFactory);
        this.initiator = new SocketInitiator(sessionFactory, settings);

        JmxExporter exporter = new JmxExporter();
        exporter.register(this.initiator);

        this.initiator.start();
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

        if (this.fixEnabled) {
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
        } else {
            throw new IllegalStateException("Fix is not enabled");
        }
    }
}
