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
package ch.algotrader.enumeration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Day on the Week.
 */
public enum WeekDay {

    SUNDAY(1), MONDAY(2), TUESDAY(3), WEDNESDAY(4), THURSDAY(5), FRIDAY(6), SATURDAY(7);

    private static final long serialVersionUID = 7560000570594673743L;

    private final int enumValue;

    /**
     * The constructor with enumeration literal value allowing
     * super classes to access it.
     */
    private WeekDay(int value) {

        this.enumValue = value;
    }

    /**
     * Gets the underlying value of this type safe enumeration.
     * This method is necessary to comply with DaoBase implementation.
     * @return The name of this literal.
     */
    public int getValue() {

        return this.enumValue;
    }

    /**
     * Returns an instance of WeekDay from int <code>value</code>.
     * Required by JAXB2 enumeration implementation
     *
     * @param value the value to create the WeekDay from.
     * @return static Enumeration with corresponding value
     */
    public static WeekDay fromValue(int value) {

        WeekDay weekDay = MAP_BY_VALUE.get(value);
        if (weekDay == null) {
            throw new IllegalArgumentException("Invalid value (" + value + ')');
        }
        return weekDay;
    }

    private static final Map<Integer, WeekDay> MAP_BY_VALUE;
    static {
        HashMap<Integer, WeekDay> map = new HashMap<>();
        for (WeekDay weekDay: WeekDay.values()) {

            map.put(weekDay.getValue(), weekDay);
        }
        MAP_BY_VALUE = new ConcurrentHashMap<>(map);
    }

}
