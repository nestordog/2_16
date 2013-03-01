/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.util.io;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.marketData.Tick;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvTickDSTAdjuster {

    public static void main(String[] args) throws SuperCSVException, IOException {

        CsvTickReader csvReader = new CsvTickReader(args[0]);
        CsvTickWriter csvWriter = new CsvTickWriter(args[1]);
        GregorianCalendar cal = new GregorianCalendar();

        Tick tick;
        while ((tick = csvReader.readTick()) != null) {

            cal.setTime(tick.getDateTime());
            int dstOffset = cal.get(Calendar.DST_OFFSET);

            if (dstOffset > 0) {
                cal.add(Calendar.MILLISECOND, -dstOffset);
            }

            tick.setDateTime(cal.getTime());
            tick.setLastDateTime(cal.getTime());
            csvWriter.write(tick);
        }

        csvWriter.close();
    }
}
