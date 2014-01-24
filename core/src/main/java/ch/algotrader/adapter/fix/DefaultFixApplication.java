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

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.Message.Header;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.StringField;
import quickfix.UnsupportedMessageType;
import quickfix.field.MsgType;
import quickfix.field.PossDupFlag;
import ch.algotrader.util.MyLogger;

/**
 * Implementation of {@link quickfix.Application}
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultFixApplication implements Application {

    private static Logger logger = MyLogger.getLogger(DefaultFixApplication.class.getName());

    private final SessionID sessionID;
    private final MessageCracker incomingMessageCracker;
    private final MessageCracker outgoingMessageCracker;
    private final FixSessionLifecycle lifecycleHandler;

    public DefaultFixApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler, FixSessionLifecycle lifecycleHandler) {
        super();

        Validate.notNull(sessionID, "Session ID may not be null");
        Validate.notNull(incomingMessageHandler, "Incoming message handler may not be null");
        Validate.notNull(lifecycleHandler, "Lifecycle handler may not be null");

        this.sessionID = sessionID;
        this.incomingMessageCracker = new InternalMessageCracker(true, incomingMessageHandler);
        this.outgoingMessageCracker = outgoingMessageHandler != null ? new InternalMessageCracker(false, outgoingMessageHandler) : new InternalMessageCracker(false, new Object());
        this.lifecycleHandler = lifecycleHandler;
    }

    public DefaultFixApplication(SessionID sessionID, Object incomingMessageHandler, FixSessionLifecycle lifecycleHandler) {
        this(sessionID, incomingMessageHandler, null, lifecycleHandler);
    }

    private void validateSessionID(SessionID actual) {
        if (!this.sessionID.equals(actual)) {
            throw new IllegalStateException("Unexpected session id: " + actual);
        }
    }

    @Override
    public void onCreate(SessionID sessionID) {

        validateSessionID(sessionID);
        lifecycleHandler.created(sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {

        validateSessionID(sessionID);
        if (logger.isInfoEnabled()) {
            logger.info("logon: " + sessionID);
        }
        lifecycleHandler.loggedOn(sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID) {

        validateSessionID(sessionID);
        if (logger.isInfoEnabled()) {
            logger.info("logout: " + sessionID);
        }
        lifecycleHandler.loggedOff(sessionID);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

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
    public void toAdmin(Message message, SessionID sessionID) {

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
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

        validateSessionID(sessionID);

        incomingMessageCracker.crack(message, sessionID);
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {

        validateSessionID(sessionID);
        try {

            // prevents a NewOrder to be sent, if PossDupFlag is set
            Header header = message.getHeader();
            StringField msgType = header.getField(new MsgType());
            if ((msgType.getValue().equals(MsgType.ORDER_SINGLE) || msgType.getValue().equals(MsgType.ORDER_CANCEL_REPLACE_REQUEST)) && header.isSetField(new PossDupFlag())) {
                logger.info("prevent order / order replacement to be sent: " + message);
                throw new DoNotSend();
            }
            outgoingMessageCracker.crack(message, sessionID);
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
            logger.info((incoming ? "incoming: " : "outgoing: ") + sessionID + message);
        }
    }
}
