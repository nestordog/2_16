package com.algoTrader.subscriber;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Characteristic;
import com.algoTrader.entity.Security;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.StochasticCsvWriter;

public class PrintStochasticSubscriber {

    private static Logger logger = MyLogger.getLogger(PrintStochasticSubscriber.class.getName());

    private Map<Security, StochasticCsvWriter> csvWriters = new HashMap<Security, StochasticCsvWriter>();

    public void update(Characteristic kFast, Characteristic kSlow, Characteristic dSlow) throws SuperCSVException, IOException {

        writeToCsv(kFast, kSlow, dSlow);

        logger.debug(kFast.getSecurity().getSymbol() + " [kFast=" + kFast.getValue() + ",kSlow=" + kSlow.getValue() + ",dSlow=" + dSlow.getValue() + "]");
    }

    private void writeToCsv(Characteristic kFast, Characteristic kSlow, Characteristic dSlow) throws SuperCSVException, IOException {

        StochasticCsvWriter csvWriter;
        if (csvWriters.containsKey(kFast.getSecurity())) {
            csvWriter = (StochasticCsvWriter)csvWriters.get(kFast.getSecurity());
        } else {
            csvWriter = new StochasticCsvWriter(kFast.getSecurity().getIsin());
            csvWriters.put(kFast.getSecurity(), csvWriter);
        }

        Map<String, Number> stochastic = new HashMap<String, Number>();
        stochastic.put("dateTime", DateUtil.getCurrentEPTime().getTime());
        stochastic.put("kFast", kFast.getValue());
        stochastic.put("kSlow", kSlow.getValue());
        stochastic.put("dSlow", dSlow.getValue());

        csvWriter.write(stochastic);
    }
}
