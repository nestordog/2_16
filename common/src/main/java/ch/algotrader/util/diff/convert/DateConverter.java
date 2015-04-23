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
package ch.algotrader.util.diff.convert;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Objects;

/**
 * Converter for date values using a {@link DateTimeFormatter} to convert from a string.
 */
public class DateConverter extends AbstractValueConverter<Date> implements ValueConverter<Date> {

    private final DateTimeFormatter dateFormat;

    public DateConverter(String dateFormatPattern) {
        this(DateTimeFormatter.ofPattern(dateFormatPattern));
    }

    public DateConverter(DateTimeFormatter dateFormat) {
        super(Date.class);
        this.dateFormat = Objects.requireNonNull(dateFormat, "dateFormat cannot be null");
    }

    @Override
    public Date convert(String column, String value) {
        try {
            Instant instant = dateFormat.parse(value, Instant::from);
            return new Date(instant.toEpochMilli());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("[" + column + "] cannot parse date value: " + value, e);
        }
    }
}
