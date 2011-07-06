package com.algoTrader.esper.subscriber;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;

public class IndicatorSubscriber {

    private static Logger logger = MyLogger.getLogger(IndicatorSubscriber.class.getName());

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void update(Map<?, ?> map) {

        logger.info(StringUtils.join((new TreeMap(map)).values(), ","));
    }
}
