package com.algoTrader.util.csv;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.HlocVO;

public class CsvHlocInterpolator {

    private static int factor = 8;
    private static long startHour = 9 * 60 * 60 * 1000;
    private static long offsetHour = 8 * 60 * 60 * 1000 / factor;

    public static void main(String[] args) throws SuperCSVException, IOException {

        CsvHlocReader csvReader = new CsvHlocReader(args[0]);
        CsvTickWriter csvWriter = new CsvTickWriter(args[1]);

        HlocVO hloc;
        while ((hloc = csvReader.readHloc()) != null) {

            for (int i=0; i <= factor; i++) {

                double high = hloc.getHigh().doubleValue();
                double low = hloc.getLow().doubleValue();
                double open = hloc.getOpen().doubleValue();
                double close = hloc.getClose().doubleValue();

                double last = 0;;
                if (open > close) {
                    switch(i) {
                        case (0) : last = open; break;
                        case (1) : last = (open + high) / 2.0; break;
                        case (2) : last = high; break;
                        case (3) : last = (high * 3 + low) / 4.0; break;
                        case (4) : last =  (high + low) / 2.0; break;
                        case (5) : last = (high + low * 3.0) / 4.0; break;
                        case (6) : last = low; break;
                        case (7) : last = (low + close) / 2.0; break;
                        case (8) : last = close; break;
                    }
                } else {
                    switch(i) {
                    case (0) : last = open; break;
                    case (1) : last = (open + low) / 2.0; break;
                    case (2) : last = low; break;
                    case (3) : last = (low * 3 + high) / 4.0; break;
                    case (4) : last =  (low + high) / 2.0; break;
                    case (5) : last = (low + high * 3.0) / 4.0; break;
                    case (6) : last = high; break;
                    case (7) : last = (high + close) / 2.0; break;
                    case (8) : last = close; break;
                    }
                }

                Tick tick = new TickImpl();
                tick.setDateTime(new Date(hloc.getDateTime().getTime() + startHour + i * offsetHour));
                tick.setLast(RoundUtil.getBigDecimal(last));
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
