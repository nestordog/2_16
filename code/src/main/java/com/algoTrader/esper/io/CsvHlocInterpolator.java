package com.algoTrader.esper.io;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.marketData.TickImpl;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.HlocVO;

public class CsvHlocInterpolator {

    private static String dataSet = ServiceLocator.instance().getConfiguration().getDataSet();

    private static double recordsPerInput = 17.0;
    private static double recordsPerHour = 2.0;
    private static double offsetHour = 9.0;

    private static boolean random = false;
    private static boolean swapHighLow = false;
    private static boolean spreadEven = true;
    private static boolean enforceHighLow = true;

    public static void main(String[] args) throws SuperCSVException, IOException {

        (new File("results/tickdata/" + dataSet + "/" + args[1] + ".csv")).delete();

        CsvHlocReader csvReader = new CsvHlocReader(args[0]);
        CsvTickWriter csvWriter = new CsvTickWriter(args[1]);

        HlocVO hloc;
        while ((hloc = csvReader.readHloc()) != null) {

            double open = hloc.getOpen().doubleValue();
            double close = hloc.getClose().doubleValue();
            double high = hloc.getHigh().doubleValue();
            double low = hloc.getLow().doubleValue();

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

                Tick tick = new TickImpl();
                tick.setDateTime(new Date(hloc.getDateTime().getTime() + (int) ((currentHour / recordsPerHour + offsetHour) * 60 * 60 * 1000)));
                tick.setLast(RoundUtil.getBigDecimal(value));
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
        }

        csvWriter.close();
    }
}
