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

    /**
     * The local (zone-less) date format: {@literal yyyy-MM-dd}.
     */
    public final static DateTimeFormatter LOCAL_DATE;
    static {
        LOCAL_DATE = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .toFormatter(Locale.ROOT);
    }

    /**
     * The local (zone-less) time format: {@literal HH:mm:ss}.
     */
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

    /**
     * The local (zone-less) date time format: {@literal yyyy-MM-dd HH:mm:ss}.
     */
    public final static DateTimeFormatter LOCAL_DATE_TIME;
    static {
        LOCAL_DATE_TIME = new DateTimeFormatterBuilder()
                .append(LOCAL_DATE)
                .appendLiteral(' ')
                .append(LOCAL_TIME)
                .toFormatter(Locale.ROOT);
    }

    /**
     * The local (zone-less) date time format with milliseconds: {@literal yyyy-MM-dd HH:mm:ss.SSS}.
     */
    public final static DateTimeFormatter LOCAL_DATE_TIME_MILLIS;
    static {
        LOCAL_DATE_TIME_MILLIS = new DateTimeFormatterBuilder()
                .append(LOCAL_DATE_TIME)
                .appendLiteral('.')
                .appendValue(ChronoField.MILLI_OF_SECOND, 3)
                .toFormatter(Locale.ROOT);
    }

    /**
     * The date time format with an explicit zone: {@literal yyyy-MM-dd HH:mm:ss z}.
     */
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

    /**
     * The 2-digit year format: {@literal yy}.
     */
    public final static DateTimeFormatter YEAR_2_DIGIT;
    static {
        YEAR_2_DIGIT = new DateTimeFormatterBuilder()
                .appendPattern("yy")
                .toFormatter(Locale.ROOT);
    }

    /**
     * The 4-digit year format: {@literal yyyy}.
     */
    public final static DateTimeFormatter YEAR_4_DIGIT;
    static {
        YEAR_4_DIGIT = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .toFormatter(Locale.ROOT);
    }

    /**
     * The 4-digit year and two-digit month format: {@literal yyyymm}.
     */
    public final static DateTimeFormatter MONTH_YEAR;
    static {
        MONTH_YEAR = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .toFormatter(Locale.ROOT);
    }

    /**
     * The 1-digit month format: {@literal M}.
     */
    public final static DateTimeFormatter MONTH_1_DIGIT;
    static {
        MONTH_1_DIGIT = new DateTimeFormatterBuilder()
                .appendPattern("M")
                .toFormatter(Locale.ROOT);
    }

    /**
     * The 2-digit month format: {@literal MM}.
     */
    public final static DateTimeFormatter MONTH_2_DIGIT;
    static {
        MONTH_2_DIGIT = new DateTimeFormatterBuilder()
                .appendPattern("MM")
                .toFormatter(Locale.ROOT);
    }

    /**
     * The short month text format: {@literal MMM}.
     */
    public final static DateTimeFormatter MONTH_SHORT;
    static {
        MONTH_SHORT = new DateTimeFormatterBuilder()
                .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
                .toFormatter(Locale.ENGLISH);
    }


    /**
     * The long month text format: {@literal MMMM}.
     */
    public final static DateTimeFormatter MONTH_LONG;
    static {
        MONTH_LONG = new DateTimeFormatterBuilder()
                .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL)
                .toFormatter(Locale.ENGLISH);
    }

    /**
     * The day of month format: {@literal dd}.
     */
    public final static DateTimeFormatter DAY_OF_MONTH;
    static {
        DAY_OF_MONTH = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .toFormatter(Locale.ROOT);

    }

    /**
     * The week of month format: {@literal W}.
     */
    public final static DateTimeFormatter WEEK_OF_MONTH;
    static {
        WEEK_OF_MONTH = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.ALIGNED_WEEK_OF_MONTH)
                .toFormatter(Locale.ROOT);

    }

    public final static DateTimeFormatter FILE_TIMESTAMP;
    static {
        FILE_TIMESTAMP = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .appendValue(ChronoField.DAY_OF_MONTH, 2)
                .appendLiteral("-")
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .toFormatter(Locale.ROOT);
    }

}
