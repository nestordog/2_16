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

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

class InternalDateDeserializer extends JsonDeserializer<Date> {

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
                final Instant instant = localDateTime.atZone(JsonConsts.UTC).toInstant();
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
            return JsonConsts.DATE_TIME_FORMATTER.parse(text, LocalDateTime::from);
        } catch (DateTimeException ex) {
            return JsonConsts.DATE_FORMATTER.parse(text, LocalDate::from).atStartOfDay();
        }
    }

}
