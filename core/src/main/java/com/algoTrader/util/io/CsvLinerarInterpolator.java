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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.util.RoundUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvLinerarInterpolator {

    private static String dataSet = ServiceLocator.instance().getConfiguration().getDataSet();

    private static double recordsPerInput = 17.0;
    private static double recordsPerHour = 2.0;
    private static double offsetHour = 9.0;

    public static void main(String[] args) throws SuperCSVException, IOException, ParseException {

        (new File("files" + File.separator + "tickdata" + File.separator + dataSet + File.separator + args[1] + ".csv")).delete();

        CsvTickReader csvReader = new CsvTickReader(args[0]);
        CsvTickWriter csvWriter = new CsvTickWriter(args[1]);

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
                tick.setOpenIntrest(0);
                tick.setSettlement(new BigDecimal(0));

                csvWriter.write(tick);
            }
            oldTick = newTick;
        }

        csvWriter.close();
    }
}
