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

import org.apache.log4j.Logger;

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
 * Implementation of {@link quickfix.Application} also extending {@link MessageCracker}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FixApplication extends MessageCracker implements quickfix.Application {

    private static Logger logger = MyLogger.getLogger(FixApplication.class.getName());

    private SessionID sessionId;

    public FixApplication(Object messageHandler) {
        super(messageHandler);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        // do nothing
    }

    @Override
    public void fromApp(Message message, SessionID sessionID) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        crack(message, sessionID);
    }

    @Override
    public void onCreate(SessionID sessionID) {
        // do nothing
    }

    @Override
    public void onLogon(SessionID sessionId) {
        this.sessionId = sessionId;
        logger.info("logon: " + sessionId);
    }

    @Override
    public void onLogout(SessionID sessionId) {
        this.sessionId = null;
        logger.info("logout: " + sessionId);
    }

    /**
     * crack toAdmin messages except heartbeats
     */
    @Override
    public void toAdmin(Message message, SessionID sessionID) {

        try {
            if (!message.getHeader().getField(new MsgType()).getValue().equals(MsgType.HEARTBEAT)) {
                crack(message, sessionID);
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * prevents a NewOrder to be sent, if PossDupFlag is set
     */
    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {


        try {
            Header header = message.getHeader();
            StringField msgType = header.getField(new MsgType());
            if ((msgType.getValue().equals(MsgType.ORDER_SINGLE) || msgType.getValue().equals(MsgType.ORDER_CANCEL_REPLACE_REQUEST)) && header.isSetField(new PossDupFlag())) {
                logger.info("prevent order / order replacement to be sent: " + message);
                throw new DoNotSend();
            }
        } catch (FieldNotFound e) {
            logger.error(e);
        }
    }

    public SessionID getSessionID(String qualifier) {
        return this.sessionId;
    }

    @Override
    protected void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        logger.debug("message: " + message);
    }
}
