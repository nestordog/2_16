package com.algoTrader.client.chart;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import com.sun.tools.jconsole.JConsolePlugin;

public class ChartPlugin extends JConsolePlugin {

    private Map<String, ChartTab> chartTabs = null;
    private boolean initialized = false;

    public ChartPlugin() {
    }

    public Map<String, ChartTab> getChartTabs() {

        return this.chartTabs;
    }

    public boolean isInitialized() {

        return this.initialized;
    }

    public void setInitialized(boolean b) {

        this.initialized = b;
    }

    public MBeanServerConnection getMBeanServerConnection() {

        return getContext().getMBeanServerConnection();
    }

    @Override
    public synchronized Map<String, JPanel> getTabs() {

        Map<String, JPanel> tabs = new LinkedHashMap<String, JPanel>();
        this.chartTabs = new LinkedHashMap<String, ChartTab>();

        Set<ObjectInstance> beans = null;
        try {
            beans = getContext().getMBeanServerConnection().queryMBeans(null, null);
        } catch (IOException e) {
            e.printStackTrace();
            return tabs;
        }

        for (ObjectInstance instance : beans) {

            String instanceName = instance.getObjectName().toString();
            if (instanceName.endsWith("ChartService")) {

                String className = instance.getClassName();
                String chartName = StringUtils.substringAfterLast(className, ".");
                chartName = StringUtils.substringBefore(chartName, "Impl");

                ChartTab chartTab = new ChartTab(this);
                tabs.put(chartName, chartTab);
                this.chartTabs.put(instanceName, chartTab);
            }
        }

        return tabs;
    }

    @Override
    public SwingWorker<Map<String, ChartData>, Object> newSwingWorker() {
        return new ChartWorker(this);
    }
}
