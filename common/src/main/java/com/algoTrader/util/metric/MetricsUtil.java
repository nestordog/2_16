/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.util.metric;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.MyLogger;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class MetricsUtil {

    private static final boolean metricsEnabled = ServiceLocator.instance().getConfiguration().getBoolean("misc.metricsEnabled");
    private static final boolean simulation = ServiceLocator.instance().getConfiguration().getSimulation();

    private static Logger logger = MyLogger.getLogger(MetricsUtil.class.getName());

    private static Map<String, Metric> metrics = new HashMap<String, Metric>();
    private static long startMillis = System.nanoTime();

    public static void execute(String metricName, Probe probe) {

        if (metricsEnabled) {

            long startTime = System.nanoTime();

            probe.run();

            long endTime = System.nanoTime();

            account(metricName, startTime, endTime);

        } else {
            probe.run();
        }
    }

    public static void account(String metricName, long startMillis, long endMillis) {

        if (metricsEnabled) {
            account(metricName, endMillis - startMillis);
        }
    }

    public static void accountEnd(String metricName, long startMillis) {

        if (metricsEnabled) {
            account(metricName, System.nanoTime() - startMillis);
        }
    }

    public static void account(String metricName, long millis) {

        if (metricsEnabled) {
            getMetric(metricName).addTime(millis);
        }
    }

    public static void logMetrics() {

        if (metricsEnabled) {

            if (simulation) {
                logger.info("TotalDuration: " + getDuration() + " millis");
            }

            for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
                Metric metric = entry.getValue();
                logger.info(metric.getName() + ": " + metric.getTime() + " millis " + metric.getExecutions() + " executions");
            }
        }
    }

    public static void resetMetrics() {

        metrics = new HashMap<String, Metric>();
        startMillis = System.nanoTime();
    }

    public static long getDuration() {

        return System.nanoTime() - startMillis;
    }

    private static Metric getMetric(String metricName) {

        Metric metric = metrics.get(metricName);
        if (metric == null) {
            metric = new Metric(metricName);
            metrics.put(metricName, metric);
        }
        return metric;
    }
}
