/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.esper.listener;

import java.util.Date;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

import ch.algotrader.report.ListReporter;
import ch.algotrader.report.Report;
import ch.algotrader.util.DateTimeUtil;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * Prints all values as a comma-separated-list (CSV) to files/reports/IndicatorReport.csv
 *
 * Headers will be extracted from the supplied {@code statement}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class IndicatorListener implements StatementAwareUpdateListener {

    private String[] propertyNames;
    private ListReporter reporter;

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement, EPServiceProvider epServiceProvider) {

        long startTime = System.nanoTime();

        // print the headers
        if (this.propertyNames == null) {
            this.propertyNames = statement.getEventType().getPropertyNames();
            this.reporter = new ListReporter(Report.generateFile("IndicatorReport"), this.propertyNames);
        }

        // print the values
        for (EventBean bean : newEvents) {
            Object[] values = new Object[this.propertyNames.length];
            for (int i = 0; i < this.propertyNames.length; i++) {
                Object obj = bean.get(this.propertyNames[i]);
                if (obj instanceof Date) {
                    values[i] = DateTimeUtil.formatAsGMT(((Date) obj).toInstant());
                } else {
                    values[i] = obj;
                }
            }
            this.reporter.write(values);
        }

        MetricsUtil.accountEnd("IndicatorListener", startTime);
    }
}
