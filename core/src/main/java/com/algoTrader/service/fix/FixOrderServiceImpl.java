package com.algoTrader.service.fix;

import org.apache.log4j.Logger;

import quickfix.Message;
import quickfix.StringField;
import quickfix.field.MsgType;

import com.algoTrader.entity.trade.Order;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.util.MyLogger;

public abstract class FixOrderServiceImpl extends FixOrderServiceBase {

    private static final long serialVersionUID = -1571841567775158540L;

    private static Logger logger = MyLogger.getLogger(FixOrderServiceImpl.class.getName());

    @Override
    protected void handleInit() throws Exception {

        getFixClient().createSession(getMarketChannel());
    }

    @Override
    protected void handleSendAndPropagateMessage(Order order, Message message) throws Exception {

        if (!getFixClient().getConnectionState(getMarketChannel()).equals(ConnectionState.LOGGED_ON)) {
            throw new FixOrderServiceException("FIX Session is not logged on " + getMarketChannel());
        }

        // send the message to the FixClient
        getFixClient().sendMessage(message, getMarketChannel());

        StringField msgType = message.getHeader().getField(new MsgType());
        if (msgType.getValue().equals(MsgType.ORDER_SINGLE)) {
            logger.info("sent order: " + order);
        } else if (msgType.getValue().equals(MsgType.ORDER_CANCEL_REPLACE_REQUEST)) {
            logger.info("sent order modification: " + order);
        } else if (msgType.getValue().equals(MsgType.ORDER_CANCEL_REQUEST)) {
            logger.info("sent order cancellation: " + order);
        } else {

            throw new IllegalArgumentException("unsupported messagetype: " + msgType);
        }

        getOrderService().propagateOrder(order);
    }
}
