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
package ch.algotrader.util.io;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import org.supercsv.exception.SuperCSVException;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickImpl;
import ch.algotrader.util.ConfigurationUtil;
import ch.algotrader.util.RoundUtil;

/**
 * SuperCSV based utility class, that reads a Tick-File and interpolates additional Ticks based on it.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvLinerarInterpolator {

    private static String dataSet = ConfigurationUtil.getString("dataSet");

    private static double recordsPerInput = 17.0;
    private static double recordsPerHour = 2.0;
    private static double offsetHour = 9.0;

    public static void main(String[] args) throws SuperCSVException, IOException, ParseException {

        interpolate(args[0], args[1]);
    }

    /**
     * Reads a Tick-file from "files/tickdata/[dataSet]/[in]" and generates {@code recordsPerInput} Ticks per original Tick.
     * The values of the interpolated Ticks are a linear interpolation between the previous and the following Tick.
     * The resulting File is written to "files/tickdata/[dataSet]/[out]".
     */
    public static void interpolate(String in, String out) throws IOException {

        (new File("files" + File.separator + "tickdata" + File.separator + dataSet + File.separator + in + ".csv")).delete();

        CsvTickReader csvReader = new CsvTickReader(in);
        CsvTickWriter csvWriter = new CsvTickWriter(out);

        Tick oldTick = csvReader.readTick();

        Tick newTick;
        while ((newTick = csvReader.readTick()) != null) {

            for (int currentHour = 0; currentHour < recordsPerInput; currentHour++) {

                double lastOffset = (newTick.getLast().doubleValue() - oldTick.getLast().doubleValue()) / (recordsPerInput - 1.0);

                Tick tick = new TickImpl();

                tick.setDateTime(new Date(newTick.getDateTime().getTime() + (int) ((currentHour / recordsPerHour + offsetHour) * 60 * 60 * 1000)));
                tick.setLast(RoundUtil.getBigDecimal(oldTick.getLast().doubleValue() + currentHour * lastOffset));
                tick.setLastDateTime(null);
                tick.setVol(0);
                tick.setVolBid(0);
                tick.setVolAsk(0);
                tick.setBid(new BigDecimal(0));
                tick.setAsk(new BigDecimal(0));

                csvWriter.write(tick);
            }
            oldTick = newTick;
        }

        csvWriter.close();
    }
}
