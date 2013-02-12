package com.algoTrader.service.fix;

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

import com.algoTrader.util.MyLogger;


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

    @Override
    public void toAdmin(Message message, SessionID sessionID) {

        try {
            crack(message, sessionID);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

        // do not resend NewOrders if PossDupFlag is set
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
