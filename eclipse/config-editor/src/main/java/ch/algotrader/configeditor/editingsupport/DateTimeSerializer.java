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
package ch.algotrader.configeditor.editingsupport;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.configeditor.IPropertySerializer;

/**
 * Serializer for date and time values.
 *
 * @author <a href="mailto:ahihlovskiy@algotrader.ch">Andrey Hihlovskiy</a>
 *
 * @version $Revision$ $Date$
 */
public class DateTimeSerializer implements IPropertySerializer {

    private final DateFormat USFormat;
    private final DateFormat EUFormat;

    public DateTimeSerializer() {
        USFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        EUFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    }

    DateTimeSerializer(String USFormat, String EUFormat) {
        this.USFormat = new SimpleDateFormat(USFormat);
        this.EUFormat = new SimpleDateFormat(EUFormat);
    }

    @Override
    public Object deserialize(String propValue) {
        try {
            if (propValue.split("-").length > 1)
                return (Date) USFormat.parse(propValue);
            return (Date) EUFormat.parse(propValue);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String serialize(Object propObject) {
        return USFormat.format((Date) propObject);
    }
}
