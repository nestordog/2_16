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
package ch.algotrader.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;

/**
 * Date/time patterns and commonly used time zones.
 */
public final class DateTimePatterns {

    public static final ZoneId GMT = ZoneId.of("GMT");

    public final static DateTimeFormatter LOCAL_DATE;
    static {
        LOCAL_DATE = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .toFormatter(Locale.ROOT);
    }

    public final static DateTimeFormatter LOCAL_TIME;
    static {
        LOCAL_TIME = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter(Locale.ROOT);
    }

    public final static DateTimeFormatter ZONED_DATE_TIME;
    static {
        ZONED_DATE_TIME = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .appendLiteral(' ')
                .appendZoneText(TextStyle.SHORT)
                .toFormatter(Locale.ROOT);
    }

    public final static DateTimeFormatter LOCAL_DATE_TIME;
    static {
        LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter(Locale.ROOT);
    }

    public final static DateTimeFormatter LOCAL_DATE_TIME_MILLIS;
    static {
        LOCAL_DATE_TIME_MILLIS = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(':')
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .appendLiteral('.')
                .appendValue(ChronoField.MILLI_OF_SECOND, 3)
                .toFormatter(Locale.ROOT);
    }

    public final static DateTimeFormatter OPTION_MONTH_YEAR;
    static {
        OPTION_MONTH_YEAR = new DateTimeFormatterBuilder()
                .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
                .appendLiteral('/')
                .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
                .toFormatter(Locale.ROOT);
    }

    public final static DateTimeFormatter OPTION_DAY_MONTH_YEAR;
    static {
        OPTION_DAY_MONTH_YEAR = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendLiteral('/')
                .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
                .appendLiteral('/')
                .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
                .toFormatter(Locale.ROOT);
    }

    public final static DateTimeFormatter YEAR_4_DIGIT;
    static {
        YEAR_4_DIGIT = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .toFormatter(Locale.ROOT);

    }

    public final static DateTimeFormatter MONTH_LONG;
    static {
        MONTH_LONG = new DateTimeFormatterBuilder()
                .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
                .toFormatter(Locale.ROOT);
    }

    public final static DateTimeFormatter DAY_OF_MONTH;
    static {
        DAY_OF_MONTH = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .toFormatter(Locale.ROOT);

    }

    public final static DateTimeFormatter WEEK_OF_MONTH;
    static {
        WEEK_OF_MONTH = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.ALIGNED_WEEK_OF_MONTH)
                .toFormatter(Locale.ROOT);

    }

}
