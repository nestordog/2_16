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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalQuery;

/**
 * Common date/time utility methods.
 */
public class DateTimeUtil {

    // ================= Zoned ========================

    public static String formatZoned(final ZonedDateTime dateTime) {
        return DateTimePatterns.ZONED_DATE_TIME.format(dateTime);
    }

    public static void formatZoned(final ZonedDateTime dateTime, final Appendable appendable) {
        DateTimePatterns.ZONED_DATE_TIME.formatTo(dateTime, appendable);
    }

    public static <T> T parseZoned(final CharSequence text, final TemporalQuery<T> query) throws DateTimeException {
        return DateTimePatterns.ZONED_DATE_TIME.parse(text, query);
    }

    public static ZonedDateTime parseZoned(final CharSequence text) throws DateTimeException {
        return DateTimePatterns.ZONED_DATE_TIME.parse(text, ZonedDateTime::from);
    }

    // ================= GMT ========================

    public static String formatAsGMT(final Instant instant) {
        return DateTimePatterns.LOCAL_DATE_TIME.format(instant.atZone(DateTimePatterns.GMT));
    }

    public static void formatAsGMT(final Instant instant, final Appendable appendable) {
        DateTimePatterns.LOCAL_DATE_TIME.formatTo(instant.atZone(DateTimePatterns.GMT), appendable);
    }

    // ================= Local zone ========================

    public static String formatLocalZone(final Instant instant) {
        return DateTimePatterns.ZONED_DATE_TIME.format(instant.atZone(ZoneId.systemDefault()));
    }

    public static void formatLocalZone(final Instant instant, final Appendable appendable) {
        DateTimePatterns.ZONED_DATE_TIME.formatTo(instant.atZone(ZoneId.systemDefault()), appendable);
    }

    // ================= Local date / time (no explicit zone) ========================

    public static String formatLocalDateTime(final LocalDateTime localDateTime) {
        return DateTimePatterns.LOCAL_DATE_TIME.format(localDateTime);
    }

    public static void formatLocalDateTime(final LocalDateTime localDateTime, final Appendable appendable) {
        DateTimePatterns.LOCAL_DATE_TIME.formatTo(localDateTime, appendable);
    }

    public static LocalDateTime parseLocalDateTime(final CharSequence s) throws DateTimeException {
        return DateTimePatterns.LOCAL_DATE_TIME.parse(s, LocalDateTime::from);
    }

    public static <T> T parseLocalDateTime(final CharSequence s, final TemporalQuery<T> query) throws DateTimeException {
        return DateTimePatterns.LOCAL_DATE_TIME.parse(s, query);
    }

    // ================= Local date ========================

    public static String formatLocalDate(final LocalDate localDate) {
        return DateTimePatterns.LOCAL_DATE.format(localDate);
    }

    public static void formatLocalDate(final LocalDate localDate, final Appendable appendable) {
        DateTimePatterns.LOCAL_DATE.formatTo(localDate, appendable);
    }

    public static LocalDate parseLocalDate(final CharSequence text) throws DateTimeException {
        return DateTimePatterns.LOCAL_DATE.parse(text, LocalDate::from);
    }

    public static <T> T parseLocalDate(final CharSequence text, final TemporalQuery<T> query) throws DateTimeException {
        return DateTimePatterns.LOCAL_DATE.parse(text, query);
    }

    // ================= Local time ========================

    public static String formatLocalTime(final LocalTime localTime) {
        return DateTimePatterns.LOCAL_TIME.format(localTime);
    }

    public static void formatLocalTime(final LocalTime localTime, final Appendable appendable) {
        DateTimePatterns.LOCAL_TIME.formatTo(localTime, appendable);
    }

    public static LocalTime parseLocalTime(final CharSequence text) throws DateTimeException {
        return DateTimePatterns.LOCAL_TIME.parse(text, LocalTime::from);
    }

    public static <T> T parseLocalTime(final CharSequence text, final TemporalQuery<T> query) throws DateTimeException {
        return DateTimePatterns.LOCAL_TIME.parse(text, query);
    }

}
