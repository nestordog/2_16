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

package ch.algotrader.json;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;

public final class JsonConsts {

    public static final ZoneId UTC = ZoneId.of("UTC");

    public static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2, 2, SignStyle.NEVER)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 2, 2, SignStyle.NEVER)
                .toFormatter(Locale.ROOT);

    public static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
                .append(DATE_FORMATTER)
                .appendLiteral(' ')
                .appendValue(HOUR_OF_DAY, 2, 2, SignStyle.NEVER)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2, 2, SignStyle.NEVER)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2, 2, SignStyle.NEVER)
                .toFormatter(Locale.ROOT);

}
