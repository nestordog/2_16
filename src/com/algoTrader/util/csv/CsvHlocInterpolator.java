package com.algoTrader.util.csv;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.HlocVO;

public class CsvHlocInterpolator {

    private static String dataSet = PropertiesUtil.getProperty("simulation.dataSet");
    private static double recordsPerDay = 17.0;
     private static double recordsPerHour = 2.0;
     private static double startHour = 9.0;

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

            int lowHour = (int)(Math.random() * (recordsPerDay - 2.0));
            int highHour = (int)(Math.random() * (recordsPerDay - 3.0));

            if (highHour >= lowHour) highHour++;

            if ((open < close) && (lowHour > highHour) ||
                (open > close) && (lowHour < highHour)) {
                int tempHour = lowHour;
                lowHour = highHour;
                highHour = tempHour;
            }

            map.put(0, open);
            map.put((int)(recordsPerDay), close);
            map.put(highHour, high);
            map.put(lowHour, low);

            for (int currentHour = 0; currentHour < recordsPerDay; currentHour++) {

                int prevHour = map.floorKey(currentHour);
                double prevValue = map.get(prevHour);

                double value = 0.0;
                if (currentHour == prevHour) {
                    value = prevValue;
                } else {

                    int nextHour = map.ceilingKey(currentHour);
                    double nextValue = map.get(nextHour);

                    double factor = (double)(currentHour - prevHour) / (double)(nextHour - prevHour);
                    value = (nextValue - prevValue) * factor + prevValue;
                }

                Tick tick = new TickImpl();
                tick.setDateTime(new Date(hloc.getDateTime().getTime() + (int)((currentHour / recordsPerHour + startHour) * 60 * 60 * 1000)));
                tick.setLast(RoundUtil.getBigDecimal(value));
                tick.setLastDateTime(tick.getDateTime());

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
