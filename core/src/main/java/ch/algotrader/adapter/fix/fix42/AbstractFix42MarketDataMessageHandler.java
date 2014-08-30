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
package ch.algotrader.adapter.fix.fix42;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.algotrader.util.MyLogger;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.MDReqID;
import quickfix.field.MDReqRejReason;
import quickfix.field.Text;
import quickfix.fix42.MarketDataRequestReject;

/**
 * Base Fix/4.2 market data message handler. Needs to be overwritten by specific broker interfaces.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class AbstractFix42MarketDataMessageHandler extends AbstractFix42MessageHandler {

    private static Logger LOGGER = MyLogger.getLogger(AbstractFix42MarketDataMessageHandler.class.getName());

    public void onMessage(MarketDataRequestReject requestReject, SessionID sessionID) throws FieldNotFound {

        if (LOGGER.isEnabledFor(Level.WARN)) {

            StringBuilder buf = new StringBuilder();
            MDReqID reqID = requestReject.getMDReqID();
            buf.append("Subscription request for '").append(reqID.getValue()).append("' was rejected");

            if (requestReject.isSetField(Text.FIELD))  {

                buf.append("; reason given: ").append(requestReject.getText().getValue());
            } else if (requestReject.isSetField(MDReqRejReason.FIELD)) {

                buf.append("; code: ").append(requestReject.getMDReqRejReason().getValue());
            }

            LOGGER.warn(buf.toString());
        }
    }

}
