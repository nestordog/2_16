package com.algoTrader.service.fix;

import org.apache.log4j.Logger;

import quickfix.Message;
import quickfix.StringField;
import quickfix.field.MsgType;

import com.algoTrader.entity.trade.Order;
import com.algoTrader.util.MyLogger;

public abstract class FixOrderServiceImpl extends FixOrderServiceBase {

    private static final long serialVersionUID = -1571841567775158540L;

    private static Logger logger = MyLogger.getLogger(FixOrderServiceImpl.class.getName());

    @Override
    protected void handleInit() throws Exception {

        getFixClient().createSession(getOrderServiceType());
    }

    @Override
    protected void handleSendAndPropagateOrder(Order order, Message message) throws Exception {

        // send the message to the FixClient
        getFixClient().sendMessage(message, order.getAccount());

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

        // propagateOrder even for cancels (where nothing actually changed) to be able to identify missing replies
        getOrderService().propagateOrder(order);

    }
}
