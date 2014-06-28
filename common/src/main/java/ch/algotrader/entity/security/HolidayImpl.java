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
package ch.algotrader.entity.security;

import java.text.SimpleDateFormat;

import ch.algotrader.util.ObjectUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class HolidayImpl extends Holiday {

    private static final long serialVersionUID = 8542514325219286349L;
    private static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Override
    public boolean isPartialOpen() {
        return getEarlyClose() != null || getLateOpen() != null;
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append(getExchange());
        buffer.append(" ");
        buffer.append(dayFormat.format(getDate()));

        if (getLateOpen() != null) {
            buffer.append(" lateOpen: ");
            buffer.append(hourFormat.format(getLateOpen()));
        }

        if (getEarlyClose() != null) {
            buffer.append(" earlyClose: ");
            buffer.append(hourFormat.format(getEarlyClose()));
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
            return ObjectUtil.equalsNonNull(this.getExchange(), that.getExchange()) && ObjectUtil.equalsNonNull(this.getDate(), that.getDate());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getExchange());
        hash = hash * 37 + ObjectUtil.hashCode(getDate());
        return hash;
    }

}
