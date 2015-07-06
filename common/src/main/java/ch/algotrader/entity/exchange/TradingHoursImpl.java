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

import java.util.Calendar;
import ch.algotrader.enumeration.WeekDay;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TradingHoursImpl extends TradingHours {

    private static final long serialVersionUID = 6712473136790778936L;

    @Override
    public boolean isEnabled(WeekDay weekDay) {

        int weekDayValue = weekDay.getValue();
        if (getOpen().compareTo(getClose()) >= 0) {
            weekDayValue = weekDay.getValue() > 1 ? weekDay.getValue() - 1 : 7;
        }

        switch (weekDayValue) {
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

        StringBuilder buffer = new StringBuilder();

        if (this.isSunday()) {
            buffer.append(WeekDay.SUNDAY);
            buffer.append(",");
        }
        if (this.isMonday()) {
            buffer.append(WeekDay.MONDAY);
            buffer.append(",");
        }
        if (this.isTuesday()) {
            buffer.append(WeekDay.TUESDAY);
            buffer.append(",");
        }
        if (this.isWednesday()) {
            buffer.append(WeekDay.WEDNESDAY);
            buffer.append(",");
        }
        if (this.isThursday()) {
            buffer.append(WeekDay.THURSDAY);
            buffer.append(",");
        }
        if (this.isFriday()) {
            buffer.append(WeekDay.FRIDAY);
            buffer.append(",");
        }
        if (this.isSaturday()) {
            buffer.append(WeekDay.SATURDAY);
            buffer.append(",");
        }

        DateTimeUtil.formatLocalTime(DateTimeLegacy.toLocalTime(getOpen()), buffer);
        buffer.append("-");
        DateTimeUtil.formatLocalTime(DateTimeLegacy.toLocalTime(getClose()), buffer);

        return buffer.toString();
    }

}
