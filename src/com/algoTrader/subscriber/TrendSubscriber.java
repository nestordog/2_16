package com.algoTrader.subscriber;

import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;

public class TrendSubscriber {

    private static Logger logger = MyLogger.getLogger(TrendSubscriber.class.getName());


    @SuppressWarnings("unchecked")
    public void update(int underlayingId, boolean bullish) {

        String parentKey = bullish ? "trend.bull" : "trend.bear";

        Configuration subset = ConfigurationUtil.getBaseConfig().subset(parentKey);
        Iterator<String> iterator = subset.getKeys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = subset.getString(key);
            ConfigurationUtil.getBaseConfig().setProperty(key, value);
            EsperService.setPropertyValue(key, value);
        }

        // only log INFO if we are in realtime
        String message = "switched trend to " + (bullish ? "bullish" : "bearish");
        if (EsperService.getInternalClock() == true) {
            logger.info(message);
        } else {
            logger.debug(message);
        }
    }
}
