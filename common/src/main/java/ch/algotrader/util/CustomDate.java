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
package ch.algotrader.util;

import java.util.Date;

/**
 * Subclass of {@link java.util.Date} that has a Constructor that accepts a String representation of milliseconds.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CustomDate extends Date {

    private static final long serialVersionUID = -1910877406220278376L;

    public CustomDate(String date) {
        super();

        if (!"".equals(date)) {
            setTime(Long.parseLong(date));
        }
    }

    public CustomDate(long date) {
        super(date);
    }
}
