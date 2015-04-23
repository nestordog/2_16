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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Common date/time utility methods.
 */
public class DateTimeLegacy {

    // ================= Zoned parsers ====================

    /**
     * Parses textual representation of a zoned date / time value to {@link Date} using the given {@link DateTimeFormatter}.
     * @param s textual representation
     * @param formatter formatter
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsZonedDateTime(final CharSequence s, final DateTimeFormatter formatter) throws DateTimeException {
        Instant instant = formatter.parse(s, ZonedDateTime::from).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a zoned date / time value to {@link Date} using the default {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsZonedDateTime(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimeUtil.parseZoned(s).toInstant();
        return new Date(instant.toEpochMilli());
    }

    // ================= GMT parsers ====================

    /**
     * Parses textual representation of a date / time value in GMT to {@link Date} using the given {@link DateTimeFormatter}.
     * @param s textual representation
     * @param formatter formatter
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsDateTimeGMT(final CharSequence s, final DateTimeFormatter formatter) throws DateTimeException {
        Instant instant = formatter.parse(s, LocalDateTime::from).atZone(DateTimePatterns.GMT).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a date / time value in GMT to {@link Date} using the default {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsDateTimeGMT(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimeUtil.parseLocalDateTime(s).atZone(DateTimePatterns.GMT).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a date / time value with milliseconds in GMT to {@link Date} using the default {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsDateTimeMilliGMT(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimePatterns.LOCAL_DATE_TIME_MILLIS.parse(s, LocalDateTime::from).atZone(DateTimePatterns.GMT).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a date value in GMT to {@link Date} using the given {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsDateGMT(final CharSequence s, final DateTimeFormatter formatter) throws DateTimeException {
        Instant instant = formatter.parse(s, LocalDate::from).atStartOfDay(DateTimePatterns.GMT).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a date value in GMT to {@link Date} using the default {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsDateGMT(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimeUtil.parseLocalDate(s).atStartOfDay(DateTimePatterns.GMT).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a time value in GMT to {@link Date} using the given {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsTimeGMT(final CharSequence s, final DateTimeFormatter formatter) throws DateTimeException {
        Instant instant = formatter.parse(s, LocalTime::from)
                .atDate(LocalDate.of(1970, Month.JANUARY, 1)).atZone(DateTimePatterns.GMT).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a time value in GMT to {@link Date} using the default {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsTimeGMT(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimeUtil.parseLocalTime(s)
                .atDate(LocalDate.of(1970, Month.JANUARY, 1)).atZone(DateTimePatterns.GMT).toInstant();
        return new Date(instant.toEpochMilli());
    }

    // ================= Local parsers ====================

    /**
     * Parses textual representation of a date / time value in the default (local) time zone to {@link Date} using the given {@link DateTimeFormatter}.
     * @param s textual representation
     * @param formatter formatter
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsLocalDateTime(final CharSequence s, final DateTimeFormatter formatter) throws DateTimeException {
        Instant instant = formatter.parse(s, LocalDateTime::from).atZone(ZoneId.systemDefault()).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a date / time value in the default (local) time zone to {@link Date} using the default {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsLocalDateTime(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimeUtil.parseLocalDateTime(s, LocalDateTime::from).atZone(ZoneId.systemDefault()).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a date value in the default (local) time zone to {@link Date} using the given {@link DateTimeFormatter}.
     * @param s textual representation
     * @param formatter formatter
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsLocalDate(final CharSequence s, final DateTimeFormatter formatter) throws DateTimeException {
        Instant instant = formatter.parse(s, LocalDate::from).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a date value in the default (local) time zone to {@link Date} using the default {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsLocalDate(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimeUtil.parseLocalDate(s).atStartOfDay(ZoneId.systemDefault()).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a time value in the default (local) time zone to {@link Date} using the given {@link DateTimeFormatter}.
     * @param s textual representation
     * @param formatter formatter
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsLocalTime(final CharSequence s, final DateTimeFormatter formatter) throws DateTimeException {
        Instant instant = formatter.parse(s, LocalTime::from)
                .atDate(LocalDate.of(1970, Month.JANUARY, 1)).atZone(ZoneId.systemDefault()).toInstant();
        return new Date(instant.toEpochMilli());
    }

    /**
     * Parses textual representation of a time value in the default (local) time zone to {@link Date} using the given {@link DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static Date parseAsLocalTime(final CharSequence s) throws DateTimeException {
        Instant instant = DateTimeUtil.parseLocalTime(s)
                .atDate(LocalDate.of(1970, Month.JANUARY, 1)).atZone(ZoneId.systemDefault()).toInstant();
        return new Date(instant.toEpochMilli());
    }

    // ================= Converters ====================

    /**
     * Converts the given {@link Date} value to {@link LocalDateTime} in GMT.
     * @param date legacy date / time value
     * @return local date / time value
     */
    public static LocalDateTime toGMTDateTime(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DateTimePatterns.GMT).toLocalDateTime();
    }

    /**
     * Converts the given {@link Date} value to {@link LocalDate} in GMT.
     * @param date legacy date / time value
     * @return local date value
     */
    public static LocalDate toGMTDate(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DateTimePatterns.GMT).toLocalDate();
    }

    /**
     * Converts the given {@link Date} value to {@link LocalTime} in GMT.
     * @param date legacy date / time value
     * @return local time value
     */
    public static LocalTime toGMTTime(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(DateTimePatterns.GMT).toLocalDateTime().toLocalTime();
    }

    /**
     * Converts the given {@link Date} value to {@link LocalDateTime} in the default (local) time zone.
     * @param date legacy date / time value
     * @return local date / time value
     */
    public static LocalDateTime toLocalDateTime(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Converts the given {@link Date} value to {@link LocalDate} in the default (local) time zone.
     * @param date legacy date / time value
     * @return local date value
     */
    public static LocalDate toLocalDate(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Converts the given {@link Date} value to {@link LocalTime} in the default (local) time zone.
     * @param date legacy date / time value
     * @return local time value
     */
    public static LocalTime toLocalTime(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toLocalTime();
    }

}
