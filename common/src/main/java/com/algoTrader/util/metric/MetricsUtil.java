package com.algoTrader.util.metric;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.MyLogger;

public class MetricsUtil {

    private static final boolean metricsEnabled = ServiceLocator.instance().getConfiguration().getBoolean("misc.metricsEnabled");

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

            logger.info("TotalDuration: " + getDuration() + " millis");

            for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
                Metric metric = entry.getValue();
                logger.info(metric.getName() + ": " + metric.getExecutions() + " executions " + metric.getTime() + " millis");
            }
        }
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
