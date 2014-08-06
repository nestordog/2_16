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
import java.util.Calendar;

import ch.algotrader.enumeration.WeekDay;
import ch.algotrader.util.ObjectUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TradingHoursImpl extends TradingHours {

    private static final long serialVersionUID = 6712473136790778936L;

    private static final SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    public boolean isEnabled(WeekDay weekDay) {

        switch (weekDay.getValue()) {
            case Calendar.SUNDAY:
                return isSunday();
            case Calendar.MONDAY:
                return isMonday();
            case Calendar.TUESDAY:
                return isTuesday();
            case Calendar.WEDNESDAY:
                return isWednesday();
            case Calendar.THURSDAY:
                return isThursday();
            case Calendar.FRIDAY:
                return isFriday();
            case Calendar.SATURDAY:
                return isSaturday();
            default:
                throw new IllegalArgumentException("unknown weekday");
        }
    }

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append(getExchange());
        buffer.append(" ");

        for (WeekDay weekDay : WeekDay.values()) {
            if (isEnabled(weekDay)) {
                buffer.append(weekDay.name());
                buffer.append(" ");
            }
        }

        buffer.append(hourFormat.format(getOpen()));
        buffer.append("-");
        buffer.append(hourFormat.format(getClose()));

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof TradingHours) {
            TradingHours that = (TradingHours) obj;
            return ObjectUtil.equalsNonNull(this.getExchange(), that.getExchange())
                    && ObjectUtil.equalsNonNull(this.getOpen(), that.getOpen())
                    && ObjectUtil.equalsNonNull(this.getClose(), that.getClose())
                    && this.isSunday() == that.isSunday()
                    && this.isMonday() == that.isMonday()
                    && this.isTuesday() == that.isTuesday()
                    && this.isWednesday() == that.isWednesday()
                    && this.isThursday() == that.isThursday()
                    && this.isFriday() == that.isFriday()
                    && this.isSaturday() == that.isSaturday();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getExchange());
        hash = hash * 37 + ObjectUtil.hashCode(getOpen());
        hash = hash * 37 + ObjectUtil.hashCode(getClose());
        hash = hash * 37 + (isSunday() ? 1231 : 1237);
        hash = hash * 37 + (isMonday() ? 1231 : 1237);
        hash = hash * 37 + (isTuesday() ? 1231 : 1237);
        hash = hash * 37 + (isWednesday() ? 1231 : 1237);
        hash = hash * 37 + (isThursday() ? 1231 : 1237);
        hash = hash * 37 + (isFriday() ? 1231 : 1237);
        hash = hash * 37 + (isSaturday() ? 1231 : 1237);

        return hash;
    }

}
