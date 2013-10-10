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

import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.OrderCancelReject;
import ch.algotrader.util.MyLogger;

/**
 * Generic Fix44MessageHandler. Needs to be overwritten by specific broker interfaces.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class Fix44MessageHandler {

    private static Logger logger = MyLogger.getLogger(Fix44MessageHandler.class.getName());

    public Fix44MessageHandler(SessionSettings settings) {
        // do nothing
    }

    public void onMessage(ExecutionReport executionReport, SessionID sessionID) {

        logger.debug(executionReport);
    }

    public void onMessage(OrderCancelReject orderCancelReject, SessionID sessionID)  {

        logger.debug(orderCancelReject);
    }
}
