package com.algoTrader.esper.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

public class IndicatorListener implements StatementAwareUpdateListener {

    private static Logger logger = MyLogger.getLogger(IndicatorListener.class.getName());
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String[] propertyNames;

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {

        // print the headers
        if (propertyNames == null) {
            propertyNames = statement.getEventType().getPropertyNames();
            logger.info(StringUtils.join(propertyNames, ","));
        }

        // print the values
        for (EventBean bean : newEvents) {
            Object[] values = new Object[propertyNames.length];
            for (int i = 0; i < propertyNames.length; i++) {
                Object obj = bean.get(propertyNames[i]);
                if (obj instanceof Date) {
                    values[i] = format.format(obj);
                } else {
                    values[i] = obj;
                }
            }
            logger.info(StringUtils.join(values, ","));
        }
    }
}
