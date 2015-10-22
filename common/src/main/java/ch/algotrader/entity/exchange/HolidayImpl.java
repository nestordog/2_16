/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/

package ch.algotrader.entity.exchange;

import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class HolidayImpl extends Holiday {

    private static final long serialVersionUID = 8542514325219286349L;

    @Override
    public boolean isPartialOpen() {
        return getEarlyClose() != null || getLateOpen() != null;
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();
        DateTimeUtil.formatLocalDate(DateTimeLegacy.toLocalDate(getDate()), buffer);
        if (getLateOpen() != null) {
            buffer.append(",lateOpen: ");
            DateTimeUtil.formatLocalTime(DateTimeLegacy.toLocalTime(getLateOpen()), buffer);
        }

        if (getEarlyClose() != null) {
            buffer.append(",earlyClose: ");
            DateTimeUtil.formatLocalTime(DateTimeLegacy.toLocalTime(getEarlyClose()), buffer);
        }

        return buffer.toString();
    }

}
