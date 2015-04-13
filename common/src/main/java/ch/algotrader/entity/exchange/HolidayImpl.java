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

import java.util.Objects;

import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
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
        DateTimeUtil.formatLocalDate(DateTimeLegacy.toGMTDate(getDate()), buffer);
        if (getLateOpen() != null) {
            buffer.append(" lateOpen: ");
            DateTimeUtil.formatLocalTime(DateTimeLegacy.toGMTTime(getLateOpen()), buffer);
        }

        if (getEarlyClose() != null) {
            buffer.append(" earlyClose: ");
            DateTimeUtil.formatLocalTime(DateTimeLegacy.toGMTTime(getEarlyClose()), buffer);
        }

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Holiday) {
            Holiday that = (Holiday) obj;
            return Objects.equals(this.getExchange(), that.getExchange()) && Objects.equals(this.getDate(), that.getDate());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + Objects.hashCode(getExchange());
        hash = hash * 37 + Objects.hashCode(getDate());
        return hash;
    }

}
