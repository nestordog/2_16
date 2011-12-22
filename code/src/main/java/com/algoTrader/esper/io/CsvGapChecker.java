package com.algoTrader.esper.io;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.lang.time.DateUtils;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.marketData.Tick;

public class CsvGapChecker {

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss");
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.00");

    private static double maxGapDays = 4.0;
    private static double maxGapMins = 30.0;
    private static String marketOpen = "15:40:00";

    public static void main(String[] args) throws SuperCSVException, IOException, ParseException {

        File dir = new File("results/tickdata/" + args[0] + "/");

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
                        System.out.println(file.getName() + " " + dateFormat.format(newTick.getDateTime()) + " gap of " + decimalFormat.format(daysDiff) + " days");
                    }

                    if (DateUtils.isSameDay(newTick.getDateTime(), lastTick.getDateTime())) {

                        // check min gap on same day
                        double minsDiff = (double) (newTick.getDateTime().getTime() - lastTick.getDateTime().getTime()) / 60000;
                        if (minsDiff > maxGapMins) {
                            System.out.println(file.getName() + " " + dateFormat.format(newTick.getDateTime()) + " gap of " + decimalFormat.format(minsDiff) + " mins");
                        }

                    } else {

                        long marketOpenMills = timeFormat.parse(marketOpen).getTime();
                        long millisSinceOpen = timeFormat.parse(timeFormat.format(newTick.getDateTime())).getTime();

                        // check market open on new day
                        if (millisSinceOpen > marketOpenMills) {
                            System.out.println(file.getName() + " " + dateFormat.format(newTick.getDateTime()) + " late market open");
                        }
                    }
                }

                lastTick = newTick;
            }
        }
    }
}
