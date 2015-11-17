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

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

class InternalDateDeserializer extends JsonDeserializer<Date> {

    static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 4)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2, 2, SignStyle.NEVER)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 2, 2, SignStyle.NEVER)
                .toFormatter(Locale.ROOT);
    static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
                .append(DATE_FORMATTER)
                .appendLiteral(' ')
                .appendValue(HOUR_OF_DAY, 2, 2, SignStyle.NEVER)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2, 2, SignStyle.NEVER)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2, 2, SignStyle.NEVER)
                .toFormatter(Locale.ROOT);

    @Override
    public Date deserialize(
            final JsonParser jsonParser,
            final DeserializationContext context) throws IOException, JsonProcessingException {
        final JsonToken t = jsonParser.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return new Date(jsonParser.getLongValue());
        } else if (t == JsonToken.VALUE_STRING) {
            String text = jsonParser.getText().trim();
            if (text.length() == 0) {
                return null;
            }
            if ("null".equals(text)) {
                return null;
            }
            try {
                final LocalDateTime localDateTime = parse(text);
                final Instant instant = localDateTime.atZone(ZoneOffset.systemDefault()).toInstant();
                return new Date(instant.toEpochMilli());
            } catch (DateTimeParseException ex) {
                throw new JsonParseException("Unparseable date: '" + text + "'", jsonParser.getCurrentLocation(), ex);
            }
        } else {
            throw context.mappingException(Date.class, t);
        }
    }

    static LocalDateTime parse(final CharSequence text) {
        try {
            return DATE_TIME_FORMATTER.parse(text, LocalDateTime::from);
        } catch (DateTimeException ex) {
            return DATE_FORMATTER.parse(text, LocalDate::from).atStartOfDay();
        }
    }

}
