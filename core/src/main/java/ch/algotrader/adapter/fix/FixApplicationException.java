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

import org.apache.log4j.Logger;

import ch.algotrader.entity.trade.Order;
import ch.algotrader.util.MyLogger;
import quickfix.Message;
import quickfix.StringField;
import quickfix.field.MsgType;

/**
 * Signals generic FIX application exception.
 *
 * @version $Revision$ $Date$
 */
public class FixApplicationException extends RuntimeException {

    public FixApplicationException(final String message) {
        super(message);
    }

    public FixApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
