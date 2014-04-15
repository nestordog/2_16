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
package ch.algotrader.service.fix;

import org.apache.log4j.Logger;

import quickfix.Message;
import quickfix.StringField;
import quickfix.field.MsgType;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.util.MyLogger;

/**
 * Generic FIX order service
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class FixOrderServiceImpl extends FixOrderServiceBase {

    private static final long serialVersionUID = -1571841567775158540L;

    private static Logger logger = MyLogger.getLogger(FixOrderServiceImpl.class.getName());

    @Override
    protected void handleInit() throws Exception {

        getFixAdapter().createSession(getOrderServiceType());
    }

    @Override
    protected void handleSendOrder(Order order, Message message) throws Exception {

        // send the message to the FixClient
        getFixAdapter().sendMessage(message, order.getAccount());

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
    }
}
