package com.algoTrader.esper.io;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.marketData.Tick;

public class CsvTickDSTAdjuster {

    public static void main(String[] args) throws SuperCSVException, IOException  {

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
