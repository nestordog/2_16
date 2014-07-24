/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH. The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation, disassembly or reproduction of this material is strictly forbidden unless
 * prior written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH Badenerstrasse 16 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.configeditor.editingsupport;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.configeditor.IPropertySerializer;

public class DateTimeSerializer implements IPropertySerializer {

    private final DateFormat format;

    DateTimeSerializer() {
        this.format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    }

    DateTimeSerializer(String format) {
        this.format = new SimpleDateFormat(format);
    }

    @Override
    public Object deserialize(String propValue) {
        try {
            return (Date) format.parse(propValue);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String serialize(Object propObject) {
        return format.format((Date) propObject);
    }
}
