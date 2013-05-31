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
package ch.algorader.util.io;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.marketData.Tick;

/**
 * SuperCSV based utility class that adjustes the {@code dateTime} field based on curent Daylight-Savings-Time Offset.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvTickDSTAdjuster {

    public static void main(String[] args) throws SuperCSVException, IOException {

        adjust(args[0], args[1]);
    }

    /**
     * Reads {@link Tick Ticks} from the File "files/tickdata/[in]". If there is currently a Daylight-Savings-Time Offset,
     * it will be added to the {@code dateTime} field. The resulting File is written to "files/tickdata/[out]".
     */
    public static void adjust(String in, String out) throws IOException {

        CsvTickReader csvReader = new CsvTickReader("files" + File.separator + "tickdata" + File.separator + in);
        CsvTickWriter csvWriter = new CsvTickWriter("files" + File.separator + "tickdata" + File.separator + out);
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
