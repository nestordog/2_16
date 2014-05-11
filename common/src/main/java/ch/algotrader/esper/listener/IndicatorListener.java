/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.esper.listener;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

import ch.algotrader.util.MyLogger;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Prints all values as a comma-separated-list (CSV) to Log.
 * Headers will be extracted from the supplied {@code statement}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IndicatorListener implements StatementAwareUpdateListener {

    private static Logger logger = MyLogger.getLogger(IndicatorListener.class.getName());
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String[] propertyNames;

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {

        long startTime = System.nanoTime();

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

        MetricsUtil.accountEnd("IndicatorListener", startTime);
    }
}
