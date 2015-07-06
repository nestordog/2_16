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

package ch.algotrader.entity.exchange;

import java.util.TimeZone;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ExchangeImpl extends Exchange {

    private static final long serialVersionUID = 6764893180734809331L;

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();
        buffer.append(getName());
        buffer.append(",");
        buffer.append(getCode());
        return buffer.toString();
    }

    @Override
    public TimeZone getTZ() {

        return TimeZone.getTimeZone(getTimeZone());
    }
}
