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
package ch.algotrader.util.metric;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;

import ch.algotrader.util.MyLogger;

import com.algoTrader.ServiceLocator;

/**
 * Utility class for recording and logging of performance metrics
 *
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

    /**
     * account the given metric by its {@code startMillis} and {@code endMillis}
     */
    public static void account(String metricName, long startMillis, long endMillis) {

        if (metricsEnabled) {
            account(metricName, endMillis - startMillis);
        }
    }

    /**
     * account the given metric by its {@code startMillis}. The endTime is taken from the system clock
     */
    public static void accountEnd(String metricName, long startMillis) {

        if (metricsEnabled) {
            account(metricName, System.nanoTime() - startMillis);
        }
    }

    /**
     * account the given metric by its {@code startMillis}. The endTime is taken from the system clock.
     * The name of the metric will be a combination of {@code metricName} and {@code clazz}.
     */
    public static void accountEnd(String metricName, Class<?> clazz, long startMillis) {

        if (metricsEnabled) {
            account(metricName + "." + ClassUtils.getShortClassName(clazz), System.nanoTime() - startMillis);
        }
    }

    /**
     * account the given metric by its duration in {@code millis}
     */
    public static void account(String metricName, long millis) {

        if (metricsEnabled) {
            getMetric(metricName).addTime(millis);
        }
    }

    /**
     * print-out all metric values
     */
    public static void logMetrics() {

        if (metricsEnabled) {

            if (simulation) {
                logger.info("TotalDuration: " + (System.nanoTime() - startMillis) + " millis");
            }

            for (Metric metric : metrics.values()) {
                logger.info(metric.getName() + ": " + metric.getTime() + " millis " + metric.getExecutions() + " executions");
            }
        }
    }

    /**
     * Resets all metrics and also reset the {@code startMillis}
     */
    public static void resetMetrics() {

        metrics.clear();
        startMillis = System.nanoTime();
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
