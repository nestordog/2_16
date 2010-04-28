package com.algoTrader.util.csv;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.RoundUtil;

public class CsvTickInterpolator {

    private static int factor = 8;

    private static long startHour = 8;
    private static long offsetHour = 1;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy");
    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yy_k:mm:ss");

    public static void main(String[] args) throws SuperCSVException, IOException, ParseException {

        CsvTickReader csvReader = new CsvTickReader(args[0]);
        CsvTickWriter csvWriter = new CsvTickWriter(args[1]);

        Tick oldTick = csvReader.readTick();

        Tick newTick;
        while ((newTick = csvReader.readTick()) != null) {

            for (int i=0; i <= factor; i++) {

                double lastOffset = (newTick.getLast().doubleValue() - oldTick.getLast().doubleValue()) / (double)factor;
                long hour = startHour + i * offsetHour;
                String dateTime = dateFormat.format(newTick.getDateTime()) + "_" +  + hour + ":01:00";

                Tick tick = new TickImpl();

                tick.setDateTime(dateTimeFormat.parse(dateTime));
                tick.setLast(RoundUtil.getBigDecimal(oldTick.getLast().doubleValue() + i * lastOffset));

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
            oldTick = newTick;
        }

        csvWriter.close();
    }
}
