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
package ch.algotrader.enumeration;

/**
 * A Duration in milliseconds. Typical Duration values (e.g. 1Min, 15Min, 1Hour and 1Day) are
 * provided
 */
public enum Duration {

    //@formatter:off
    MSEC_1(1l),

    SEC_1(1000l),
    SEC_5(5000l),

    MIN_1(60000l),
    MIN_2(120000l),
    MIN_3(180000l),
    MIN_5(300000l),
    MIN_10(600000l),
    MIN_15(900000l),
    MIN_30(1800000l),

    HOUR_1(3600000l),
    HOUR_2(7200000l),

    DAY_1(86400000l),
    DAY_2(172800000l),

    WEEK_1(604800000l),
    WEEK_2(1209600000l),

    MONTH_1(2592000000l),
    MONTH_2(5184000000l),
    MONTH_3(7776000000l),
    MONTH_4(10368000000l),
    MONTH_5(12960000000l),
    MONTH_6(15552000000l),
    MONTH_7(18144000000l),
    MONTH_8(20736000000l),
    MONTH_9(23328000000l),
    MONTH_10(25920000000l),
    MONTH_11(28512000000l),
    MONTH_18(46656000000l),

    YEAR_1(31536000000l),
    YEAR_2(63072000000l);
    //@formatter:on

    private static final long serialVersionUID = -7888367833582543038L;

    private final long enumValue;

    /**
     * The constructor with enumeration literal value allowing
     * super classes to access it.
     */
    private Duration(long value) {

        this.enumValue = value;
    }

    /**
     * Gets the underlying value of this type safe enumeration.
     * This method is necessary to comply with DaoBase implementation.
     * @return The name of this literal.
     */
    public long getValue() {

        return this.enumValue;
    }

}
