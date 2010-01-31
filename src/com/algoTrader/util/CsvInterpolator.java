package com.algoTrader.util;

import java.io.IOException;
import java.util.Date;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;

public class CsvInterpolator {

    private static int factor = 6;

    public static void main(String[] args) throws SuperCSVException, IOException {

        CsvReader csvReader = new CsvReader(args[0]);
        CsvWriter csvWriter = new CsvWriter(args[1]);

        Tick oldTick = csvReader.readTick();

        Tick newTick;
        while ((newTick = csvReader.readTick()) != null) {

            csvWriter.writeTick(oldTick);
            for (int i=1; i < factor; i++) {

                long dateTimeOffset = (newTick.getDateTime().getTime() - oldTick.getDateTime().getTime()) / (long)factor;
                double lastOffset = (newTick.getLast().doubleValue() - oldTick.getLast().doubleValue()) / (double)factor;
                long lastDateTimeOffset = (newTick.getLastDateTime().getTime() - oldTick.getLastDateTime().getTime()) / (long)factor;
                int volOffset = (newTick.getVol() - oldTick.getVol()) / factor;
                int volBidOffset = (newTick.getVolBid() - oldTick.getVolBid()) / factor;
                int volAskOffset = (newTick.getVolAsk() - oldTick.getVolAsk()) / factor;
                double bidOffset = (newTick.getBid().doubleValue() - oldTick.getBid().doubleValue()) / (double)factor;
                double askOffset = (newTick.getAsk().doubleValue() - oldTick.getAsk().doubleValue()) / (double)factor;
                int openIntrestOffset = (newTick.getOpenIntrest() - oldTick.getOpenIntrest()) / factor;

                Tick tick = new TickImpl();
                tick.setDateTime(new Date(oldTick.getDateTime().getTime() + i * dateTimeOffset));
                tick.setLast(RoundUtil.getBigDecimal(oldTick.getLast().doubleValue() + i * lastOffset));
                tick.setLastDateTime(new Date(oldTick.getLastDateTime().getTime() + i * lastDateTimeOffset));
                tick.setVol(oldTick.getVol() + i * volOffset);
                tick.setVolBid(oldTick.getVolBid() + i * volBidOffset);
                tick.setVolAsk(oldTick.getVolAsk() + i * volAskOffset);
                tick.setBid(RoundUtil.getBigDecimal(oldTick.getBid().doubleValue() + i * bidOffset));
                tick.setAsk(RoundUtil.getBigDecimal(oldTick.getAsk().doubleValue() + i * askOffset));
                tick.setOpenIntrest(oldTick.getOpenIntrest() + i * openIntrestOffset);
                tick.setSettlement(RoundUtil.getBigDecimal(oldTick.getSettlement().doubleValue()));
                csvWriter.writeTick(tick);
            }
            oldTick = newTick;
        }
        csvWriter.writeTick(oldTick);

        csvWriter.close();
    }
}
