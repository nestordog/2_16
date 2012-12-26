package com.algoTrader.service.fix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
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

import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.MarketChannel;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.collection.IntegerMap;

public class FixClient implements InitializingBean {

    private SocketInitiator initiator = null;
    private SessionSettings settings = null;
    private IntegerMap<SessionID> orderIds = new IntegerMap<SessionID>();

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

        for (Iterator<SessionID> i = this.settings.sectionIterator(); i.hasNext();) {
            SessionID sessionId = i.next();
            if (sessionId.getSessionQualifier().equals(marketChannel.toString())) {
                this.initiator.createDynamicSession(sessionId);
                createLogonLogoutStatement(sessionId);
                return;
            }
        }

        throw new IllegalStateException("SessionID missing in settings " + marketChannel);
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

        Session session = Session.lookupSession(getSessionID(marketChannel));
        if (session.isLoggedOn()) {
            session.send(message);
        } else {
            throw new IllegalStateException("message cannot be sent, FIX Session is not logged on " + marketChannel);
        }
    }

    public int getNextOrderId(MarketChannel marketChannel) {

        SessionID sessionId = getSessionID(marketChannel);
        if (!this.orderIds.containsKey(sessionId)) {
            initOrderId(sessionId);
        }
        return this.orderIds.increment(sessionId, 1);
    }

    private SessionID getSessionID(MarketChannel marketChannel) {

        for (SessionID sessionId : this.initiator.getSessions()) {
            if (sessionId.getSessionQualifier().equals(marketChannel.getValue())) {
                return sessionId;
            }
        }
        throw new IllegalStateException("FIX Session does not exist " + marketChannel);
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

    private void initOrderId(SessionID sessionId) {

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
                if (fileHandler.read(bytes) == -1) {
                    fileHandler.close();
                    this.orderIds.put(sessionId, 1); // no last orderId
                } else {
                    if (Arrays.equals(bytes, clOrdId)) {
                        break;
                    }
                }
            }

            for (; pointer != fileLength; pointer++) {
                int readByte = fileHandler.readByte();
                if (readByte == 0x1) {
                    break;
                }
                sb.append((char) readByte);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileHandler != null) {
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        this.orderIds.put(sessionId, Integer.valueOf(sb.toString()));
    }
}
