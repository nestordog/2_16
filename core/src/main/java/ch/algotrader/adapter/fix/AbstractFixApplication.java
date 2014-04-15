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

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import ch.algotrader.util.MyLogger;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.Message.Header;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.StringField;
import quickfix.UnsupportedMessageType;
import quickfix.field.MsgType;
import quickfix.field.PossDupFlag;

/**
 * Implementation of {@link quickfix.Application}
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractFixApplication implements Application {

    private static final Logger logger = MyLogger.getLogger(AbstractFixApplication.class.getName());

    private final SessionID sessionID;
    private final MessageCracker incomingMessageCracker;
    private final MessageCracker outgoingMessageCracker;

    public AbstractFixApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler) {
        super();

        Validate.notNull(sessionID, "Session ID may not be null");
        Validate.notNull(incomingMessageHandler, "Incoming message handler may not be null");

        this.sessionID = sessionID;
        this.incomingMessageCracker = new InternalMessageCracker(true, incomingMessageHandler);
        this.outgoingMessageCracker = outgoingMessageHandler != null ? new InternalMessageCracker(false, outgoingMessageHandler) : new InternalMessageCracker(false, new Object());
    }

    public AbstractFixApplication(SessionID sessionID, Object incomingMessageHandler) {
        this(sessionID, incomingMessageHandler, null);
    }

    private void validateSessionID(SessionID actual) {
        if (!this.sessionID.equals(actual)) {
            throw new IllegalStateException("Unexpected session id: " + actual);
        }
    }

    protected SessionID getSessionID() {
        return sessionID;
    }

    protected Session getSession() {
        return Session.lookupSession(sessionID);
    }

    public void onCreate() {
    }

    public void onLogon() {
    }

    public void onLogout()  {
    }

    @Override
    public final void onCreate(SessionID sessionID) {

        validateSessionID(sessionID);
        onCreate();
    }

    @Override
    public final void onLogon(SessionID sessionID) {

        validateSessionID(sessionID);
        if (logger.isInfoEnabled()) {
            logger.info("logon: " + sessionID);
        }
        onLogon();
    }

    @Override
    public final void onLogout(SessionID sessionID)  {

        validateSessionID(sessionID);
        if (logger.isInfoEnabled()) {
            logger.info("logout: " + sessionID);
        }
        onLogout();
    }

    protected boolean interceptIncoming(Message message) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        return false;
    }

    protected boolean interceptOutgoing(Message message) throws FieldNotFound, DoNotSend {
        // prevents a NewOrder to be sent, if PossDupFlag is set
        Header header = message.getHeader();
        StringField msgType = header.getField(new MsgType());
        if ((msgType.getValue().equals(MsgType.ORDER_SINGLE) || msgType.getValue().equals(MsgType.ORDER_CANCEL_REPLACE_REQUEST)) && header.isSetField(new PossDupFlag())) {
            logger.info("prevent order / order replacement to be sent: " + message);
            throw new DoNotSend();
        }
        return false;
    }

    @Override
    public final void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

        validateSessionID(sessionID);
        try {
            if (message.getHeader().getField(new MsgType()).getValue().equals(MsgType.HEARTBEAT)) {
                return;
            }
            incomingMessageCracker.crack(message, sessionID);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public final void toAdmin(Message message, SessionID sessionID) {

        validateSessionID(sessionID);
        try {

            // do not crack heartbeats
            if (message.getHeader().getField(new MsgType()).getValue().equals(MsgType.HEARTBEAT)) {
                return;
            }
            outgoingMessageCracker.crack(message, sessionID);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public final void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

        validateSessionID(sessionID);

        if (interceptIncoming(message)) {
            return;
        }
        incomingMessageCracker.crack(message, sessionID);
    }

    @Override
    public final void toApp(Message message, SessionID sessionID) throws DoNotSend {

        validateSessionID(sessionID);
        try {
            if (interceptOutgoing(message)) {
                return;
            }
            outgoingMessageCracker.crack(message, sessionID);
        } catch (DoNotSend e) {
            throw e;
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public String toString() {
        return sessionID.toString();
    }

    /**
     * Message cracker that provides a default onMessage to prevent Unsupported Message Type
     */
    static class InternalMessageCracker extends MessageCracker {

        private boolean incoming;

        public InternalMessageCracker(boolean incoming, Object messageHandler) {
            super(messageHandler);
            this.incoming = incoming;
        }

        @Override
        protected void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        }
    }
}
