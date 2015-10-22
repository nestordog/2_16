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

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalQuery;

/**
 * Common date/time utility methods.
 */
public class DateTimeUtil {

    // ================= Zoned ========================

    /**
     * Formats the given {@link ZonedDateTime} to its textual representation with an explicit time zone using the default format.
     * @param dateTime date / time value
     * @return textual representation of input value
     */
    public static String formatZoned(final ZonedDateTime dateTime) {
        return DateTimePatterns.ZONED_DATE_TIME.format(dateTime);
    }

    /**
     * Formats the given {@link ZonedDateTime} to its textual representation with an explicit time zone using the default format
     * and appends it to the given {@link Appendable}.
     * @param dateTime date / time value
     * @param appendable appendable
     */
    public static void formatZoned(final ZonedDateTime dateTime, final Appendable appendable) {
        DateTimePatterns.ZONED_DATE_TIME.formatTo(dateTime, appendable);
    }

    /**
     * Parses textual representation of a zoned date / time value to the specified temporal type using the default {@link java.time.format.DateTimeFormatter}.
     * @param s textual representation
     * @param query  the query defining the type to parse to, not null
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static <T> T parseZoned(final CharSequence s, final TemporalQuery<T> query) throws DateTimeException {
        return DateTimePatterns.ZONED_DATE_TIME.parse(s, query);
    }

    /**
     * Parses textual representation of a zoned date / time value to the specified temporal type using the default {@link java.time.format.DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static ZonedDateTime parseZoned(final CharSequence s) throws DateTimeException {
        return DateTimePatterns.ZONED_DATE_TIME.parse(s, ZonedDateTime::from);
    }

    // ================= GMT ========================

    /**
     * Formats the given {@link Instant} to its textual representation in GMT using the default format.
     * @param instant instant value
     * @return textual representation of input value
     */
    public static String formatAsGMT(final Instant instant) {
        return DateTimePatterns.LOCAL_DATE_TIME.format(instant.atZone(DateTimePatterns.GMT));
    }

    /**
     * Formats the given {@link Instant} to its textual representation in GMT using the default format
     * and appends it to the given {@link Appendable}.
     * @param instant instant value
     */
    public static void formatAsGMT(final Instant instant, final Appendable appendable) {
        DateTimePatterns.LOCAL_DATE_TIME.formatTo(instant.atZone(DateTimePatterns.GMT), appendable);
    }

    // ================= Local zone ========================

    /**
     * Formats the given {@link Instant} to its textual representation in the default (local) time zone
     * using the default format.
     * @param instant instant value
     * @return textual representation of input value
     */
    public static String formatLocalZone(final Instant instant) {
        return DateTimePatterns.ZONED_DATE_TIME.format(instant.atZone(ZoneId.systemDefault()));
    }

    /**
     * Formats the given {@link Instant} to its textual representation in the default (local) time zone
     * using the default format and appends it to the given {@link Appendable}.
     * @param instant instant value
     */
    public static void formatLocalZone(final Instant instant, final Appendable appendable) {
        DateTimePatterns.ZONED_DATE_TIME.formatTo(instant.atZone(ZoneId.systemDefault()), appendable);
    }

    // ================= Local date / time (no explicit zone) ========================

    /**
     * Formats the given {@link LocalDateTime} to its textual representation without an explicit time zone
     * using the default format.
     * @param localDateTime local date / time value
     * @return textual representation of input value
     */
    public static String formatLocalDateTime(final LocalDateTime localDateTime) {
        return DateTimePatterns.LOCAL_DATE_TIME.format(localDateTime);
    }

    /**
     * Formats the given {@link LocalDateTime} to its textual representation without an explicit time zone
     * using the default format and appends it to the given {@link Appendable}.
     * @param localDateTime local date / time value
     */
    public static void formatLocalDateTime(final LocalDateTime localDateTime, final Appendable appendable) {
        DateTimePatterns.LOCAL_DATE_TIME.formatTo(localDateTime, appendable);
    }

    /**
     * Parses textual representation of a zone-less date / time value to {@link LocalDateTime} value using the default {@link java.time.format.DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static LocalDateTime parseLocalDateTime(final CharSequence s) throws DateTimeException {
        return DateTimePatterns.LOCAL_DATE_TIME.parse(s, LocalDateTime::from);
    }

    /**
     * Parses textual representation of a zone-less date / time value to the specified temporal type using the default {@link java.time.format.DateTimeFormatter}.
     * @param s textual representation
     * @param query  the query defining the type to parse to, not null
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static <T> T parseLocalDateTime(final CharSequence s, final TemporalQuery<T> query) throws DateTimeException {
        return DateTimePatterns.LOCAL_DATE_TIME.parse(s, query);
    }

    // ================= Local date ========================

    /**
     * Formats the given {@link LocalDate} to its textual representation without an explicit time zone
     * using the default format.
     * @param localDate local date value
     * @return textual representation of input value
     */
    public static String formatLocalDate(final LocalDate localDate) {
        return DateTimePatterns.LOCAL_DATE.format(localDate);
    }

    /**
     * Formats the given {@link LocalDate} to its textual representation without an explicit time zone
     * using the default format and appends it to the given {@link Appendable}.
     * @param localDate local date value
     */
    public static void formatLocalDate(final LocalDate localDate, final Appendable appendable) {
        DateTimePatterns.LOCAL_DATE.formatTo(localDate, appendable);
    }

    /**
     * Parses textual representation of a zone-less date time value to {@link LocalDate} value using the default {@link java.time.format.DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static LocalDate parseLocalDate(final CharSequence s) throws DateTimeException {
        return DateTimePatterns.LOCAL_DATE.parse(s, LocalDate::from);
    }

    /**
     * Parses textual representation of a zone-less date value to the specified temporal type using the default {@link java.time.format.DateTimeFormatter}.
     * @param s textual representation
     * @param query  the query defining the type to parse to, not null
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static <T> T parseLocalDate(final CharSequence s, final TemporalQuery<T> query) throws DateTimeException {
        return DateTimePatterns.LOCAL_DATE.parse(s, query);
    }

    // ================= Local time ========================

    /**
     * Formats the given {@link LocalTime} to its textual representation without an explicit time zone
     * using the default format.
     * @param localTime local time value
     * @return textual representation of input value
     */
    public static String formatLocalTime(final LocalTime localTime) {
        return DateTimePatterns.LOCAL_TIME.format(localTime);
    }

    /**
     * Formats the given {@link LocalTime} to its textual representation without an explicit time zone
     * using the default format and appends it to the given {@link Appendable}.
     * @param localTime local time value
     */
    public static void formatLocalTime(final LocalTime localTime, final Appendable appendable) {
        DateTimePatterns.LOCAL_TIME.formatTo(localTime, appendable);
    }

    /**
     * Parses textual representation of a zone-less time time value to {@link LocalTime} value using the default {@link java.time.format.DateTimeFormatter}.
     * @param s textual representation
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static LocalTime parseLocalTime(final CharSequence s) throws DateTimeException {
        return DateTimePatterns.LOCAL_TIME.parse(s, LocalTime::from);
    }

    /**
     * Parses textual representation of a zone-less time value to the specified temporal type using the default {@link java.time.format.DateTimeFormatter}.
     * @param s textual representation
     * @param query  the query defining the type to parse to, not null
     * @return parsed value
     * @throws DateTimeException if unable to parse the requested result
     */
    public static <T> T parseLocalTime(final CharSequence s, final TemporalQuery<T> query) throws DateTimeException {
        return DateTimePatterns.LOCAL_TIME.parse(s, query);
    }

}
