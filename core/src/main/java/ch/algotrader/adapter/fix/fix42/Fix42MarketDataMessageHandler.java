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
package ch.algotrader.adapter.fix.fix42;

import org.apache.log4j.Logger;

import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.MDReqID;
import quickfix.field.MDReqRejReason;
import quickfix.field.Text;
import quickfix.fix42.MarketDataRequestReject;
import ch.algotrader.util.MyLogger;

/**
 * Generic Fix42MarketDataMessageHandler. Needs to be overwritten by specific broker interfaces.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision: 6665 $ $Date: 2014-01-11 18:17:51 +0100 (Sa, 11 Jan 2014) $
 */
public class Fix42MarketDataMessageHandler {

    private static Logger logger = MyLogger.getLogger(Fix42MarketDataMessageHandler.class.getName());

    public void onMessage(MarketDataRequestReject requestReject, SessionID sessionID) throws FieldNotFound {

        MDReqID reqID = requestReject.getMDReqID();

        MDReqRejReason reason = requestReject.getMDReqRejReason();
        String reasonText;
        if (requestReject.isSetField(Text.FIELD))  {

            reasonText = requestReject.getText().getValue();
        } else {

            reasonText = "code " + reason.getValue();
        }

        logger.warn("Subscription request for '" + reqID + "' was rejected; rejection reason - " + reasonText);
    }
}
