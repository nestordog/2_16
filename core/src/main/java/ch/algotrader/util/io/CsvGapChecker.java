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
package ch.algotrader.util.io;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.commons.lang.time.DateUtils;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimeUtil;

/**
 * SuperCSV based utility class that checks Gaps in Tick Files.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvGapChecker {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    private static final double maxGapDays = 4.0;
    private static final double maxGapMins = 30.0;
    private static final String marketOpen = "15:40:00";

    public static void main(String[] args) throws IOException, ParseException {

        check(args[0]);
    }

    /**
     * Checks the file "files/tickdata/[fileName]" for Gaps.
     * <ul>
     * <li>If there is a gap of mor than {@code maxGapDays} between Ticks of different Days a message is printed</li>
     * <li>if there is a gap of mor than {@code maxGapMins} between Ticks of the same Day a message is printed</li>
     * </ul>
     * Checking starts at {@code marketOpen}
     */
    public static void check(String fileName) throws IOException, ParseException {

        File dir = new File("files" + File.separator + "tickdata" + File.separator + fileName + File.separator);

        for (File file : dir.listFiles()) {

            CsvTickReader reader = new CsvTickReader(file);

            Tick lastTick = null;
            Tick newTick = null;
            while ((newTick = reader.readTick()) != null) {

                // check gaps
                if (lastTick != null) {

                    // check day gap
                    double daysDiff = (double) (newTick.getDateTime().getTime() - lastTick.getDateTime().getTime()) / 86400000;
                    if (daysDiff > maxGapDays) {
                        System.out.println(file.getName() + " "
                                + DateTimeUtil.formatLocalDateTime(DateTimeLegacy.toLocalDateTime(newTick.getDateTime()))
                                + " gap of " + decimalFormat.format(daysDiff) + " days");
                    }

                    if (DateUtils.isSameDay(newTick.getDateTime(), lastTick.getDateTime())) {

                        // check min gap on same day
                        double minsDiff = (double) (newTick.getDateTime().getTime() - lastTick.getDateTime().getTime()) / 60000;
                        if (minsDiff > maxGapMins) {
                            System.out.println(file.getName() + " "
                                    + DateTimeUtil.formatLocalDateTime(DateTimeLegacy.toLocalDateTime(newTick.getDateTime()))
                                    + " gap of " + decimalFormat.format(minsDiff) + " mins");
                        }

                    } else {

                        long marketOpenMills = DateTimeLegacy.parseAsLocalTime(marketOpen).getTime();
                        long millisSinceOpen = newTick.getDateTime().getTime();

                        // check market open on new day
                        if (millisSinceOpen > marketOpenMills) {
                            System.out.println(file.getName() + " "
                                    + DateTimeUtil.formatLocalDateTime(DateTimeLegacy.toLocalDateTime(newTick.getDateTime())) + " late market open");
                        }
                    }
                }

                lastTick = newTick;
            }
        }
    }
}
