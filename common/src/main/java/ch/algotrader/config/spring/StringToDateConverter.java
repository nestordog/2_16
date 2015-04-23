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

package ch.algotrader.config.spring;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.convert.converter.Converter;

import ch.algotrader.util.DateTimePatterns;

public class StringToDateConverter implements Converter<String, Date> {

    private static final Pattern DATE_PATTERN =
            Pattern.compile("^\\d{4}-\\d{1,2}-\\d{1,2}$");

    private static final Pattern TIME_PATTERN =
            Pattern.compile("^\\d{1,2}:\\d{2}:\\d{2}$");

    private static final Pattern DATE_TIME_PATTERN =
            Pattern.compile("^\\d{4}\\-\\d{2}\\-\\d{2} \\d{1,2}:\\d{2}:\\d{2}$");

    @Override
    public Date convert(final String source) {

        if (source == null) {
            return null;
        }
        String s = source.trim();
        DateTimeFormatter dateFormat = null;
        Date date = null;
        Matcher matcher1 = DATE_PATTERN.matcher(s);
        try {
            if (matcher1.matches()) {
                dateFormat = DateTimeFormatter.ofPattern("yyyy-M-d");
                LocalDate localDate = dateFormat.parse(s, LocalDate::from);
                Instant instant = localDate.atStartOfDay(DateTimePatterns.GMT).toInstant();
                date = new Date(instant.toEpochMilli());
            } else {
                Matcher matcher2 = TIME_PATTERN.matcher(s);
                if (matcher2.matches()) {
                    dateFormat = DateTimeFormatter.ofPattern("H:mm:ss");
                    LocalTime localTime = dateFormat.parse(s, LocalTime::from);
                    Instant instant = localTime.atDate(LocalDate.of(1970, 1, 1)).atZone(DateTimePatterns.GMT).toInstant();
                    date = new Date(instant.toEpochMilli());
                } else {
                    Matcher matcher3 = DATE_TIME_PATTERN.matcher(s);
                    if (matcher3.matches()) {
                        dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");
                        LocalDateTime localDateTime = dateFormat.parse(s, LocalDateTime::from);
                        Instant instant = localDateTime.atZone(DateTimePatterns.GMT).toInstant();
                        date = new Date(instant.toEpochMilli());
                    }
                }
            }
            if (dateFormat == null) {
                throw new IllegalArgumentException("'" + source + "' cannot be converted to a Date");
            }
            return date;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("'" + source + "' cannot be converted to a Date");
        }
    }

}
