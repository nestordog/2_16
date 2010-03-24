package com.algoTrader.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;

public class CsvInterpolator {

    private static int factor = 8;
    private static long startHour = 9 * 60 * 60 * 1000;
    private static long offsetHour = 8 * 60 * 60 * 1000 / factor;

    public static void main(String[] args) throws SuperCSVException, IOException {

        CsvReader csvReader = new CsvReader(args[0]);
        TickCsvWriter csvWriter = new TickCsvWriter(args[1]);

        Tick oldTick = csvReader.readTick();

        Tick newTick;
        while ((newTick = csvReader.readTick()) != null) {

            for (int i=0; i < factor; i++) {

                double lastOffset = (newTick.getLast().doubleValue() - oldTick.getLast().doubleValue()) / (double)factor;

                Tick tick = new TickImpl();
                tick.setDateTime(new Date(oldTick.getDateTime().getTime() + startHour + i * offsetHour));
                tick.setLast(RoundUtil.getBigDecimal(oldTick.getLast().doubleValue() + i * lastOffset));
                tick.setLastDateTime(oldTick.getLastDateTime());

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
