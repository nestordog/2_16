package com.algoTrader.subscriber;

import java.util.Collection;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;

public class TrendSubscriber {

    private static Logger logger = MyLogger.getLogger(TrendSubscriber.class.getName());


    public void update(int underlayingId, boolean bullish) {

        String parentKey = bullish ? "trend.bull" : "trend.bear";

        Collection<String> keys = PropertiesUtil.getChildKeys(parentKey);

        for (String key : keys) {
            String value = PropertiesUtil.getProperty(parentKey, key);
            PropertiesUtil.setEsperOrConfigProperty(key, value);
        }

        logger.info("switched trend to " + (bullish ? "bullish" : "bearish"));
    }
}
