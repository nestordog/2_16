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
import java.math.BigDecimal;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.util.ConfigurationUtil;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.BarVO;

/**
 * SuperCSV based utility class, that reads a Bar-File and interpolates Tick based on it.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CsvBarToTickInterpolator {

    private static String dataSet = ConfigurationUtil.getString("dataSet");

    private static double recordsPerInput = 17.0;
    private static double recordsPerHour = 2.0;
    private static double offsetHour = 9.0;

    private static boolean random = false;
    private static boolean swapHighLow = false;
    private static boolean spreadEven = true;
    private static boolean enforceHighLow = true;

    public static void main(String[] args) throws IOException {

        interpolate(args[0], args[1]);
    }

    /**
     * Reads a Tick-file from "files/bardata/[dataSet]/[in]" and generates {@code recordsPerInput} Ticks per Bar.
     * The resulting File is written to "files/tickdata/[dataSet]/[out]".
     */
    public static void interpolate(String in, String out) throws IOException {

        File file = new File("files" + File.separator + "bardata" + File.separator + dataSet + File.separator + in);
        if (file.exists()) {
            file.delete();
        }

        CsvBarVOReader csvReader = new CsvBarVOReader(in);
        CsvTickWriter csvWriter = new CsvTickWriter(out);

        BarVO bar;
        while ((bar = csvReader.readBarVO()) != null) {

            double open = bar.getOpen().doubleValue();
            double close = bar.getClose().doubleValue();
            double high = bar.getHigh().doubleValue();
            double low = bar.getLow().doubleValue();

            NavigableMap<Integer, Double> map = new TreeMap<Integer, Double>();

            int lowHour = 0;
            int highHour = 0;
            if (random) {
                lowHour = (int) (Math.random() * (recordsPerInput - 2.0));
                highHour = (int) (Math.random() * (recordsPerInput - 3.0));

                if (highHour >= lowHour) {
                    highHour++;
                }

                if (swapHighLow) {
                    if ((open < close) && (lowHour > highHour) || (open > close) && (lowHour < highHour)) {
                        int tempHour = lowHour;
                        lowHour = highHour;
                        highHour = tempHour;
                    }
                }
            }

            if (spreadEven) {
                double totalMovement = 0.0;
                if (open > close) {
                    totalMovement = high - open + high - low + close - low;
                } else {
                    totalMovement = open - low + high - low + high - close;
                }
                double movementPerRecord = totalMovement / (recordsPerInput - 1);
                if (open > close) {
                    highHour = (int) Math.round((high - open) / movementPerRecord);
                    lowHour = (int) Math.round((high - open + high - low) / movementPerRecord);
                } else {
                    lowHour = (int) Math.round((open - low) / movementPerRecord);
                    highHour = (int) Math.round((open - low + high - low) / movementPerRecord);
                }
            }

            if (enforceHighLow) {
                map.put(0, open);
                map.put((int) (recordsPerInput - 1), close);
                map.put(highHour, high);
                map.put(lowHour, low);
            } else {
                map.put(highHour, high);
                map.put(lowHour, low);
                map.put(0, open);
                map.put((int) (recordsPerInput - 1), close);
            }

            for (int currentHour = 0; currentHour < recordsPerInput; currentHour++) {

                int prevHour = map.floorKey(currentHour);
                double prevValue = map.get(prevHour);

                double value = 0.0;
                if (currentHour == prevHour) {
                    value = prevValue;
                } else {

                    int nextHour = map.ceilingKey(currentHour);
                    double nextValue = map.get(nextHour);

                    double factor = (double) (currentHour - prevHour) / (double) (nextHour - prevHour);
                    value = (nextValue - prevValue) * factor + prevValue;
                }

                Tick tick = Tick.Factory.newInstance();
                tick.setDateTime(new Date(bar.getDateTime().getTime() + (int) ((currentHour / recordsPerHour + offsetHour) * 60 * 60 * 1000)));
                tick.setLast(RoundUtil.getBigDecimal(value));
                tick.setLastDateTime(null);

                tick.setVol(0);
                tick.setVolBid(0);
                tick.setVolAsk(0);
                tick.setBid(new BigDecimal(0));
                tick.setAsk(new BigDecimal(0));

                csvWriter.write(tick);
            }
        }

        csvWriter.close();
    }
}
