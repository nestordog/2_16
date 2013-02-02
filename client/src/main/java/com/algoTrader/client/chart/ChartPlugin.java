package com.algoTrader.client.chart;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.algoTrader.vo.ChartDataVO;
import com.sun.tools.jconsole.JConsolePlugin;

public class ChartPlugin extends JConsolePlugin {

    private Map<ObjectName, ChartTab> chartTabs = null;
    private boolean initialized = false;

    public ChartPlugin() {
    }

    public Map<ObjectName, ChartTab> getChartTabs() {

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
        this.chartTabs = new LinkedHashMap<ObjectName, ChartTab>();

        Set<ObjectInstance> beans = null;
        try {
            ObjectName objectName = new ObjectName("com.algoTrader.*:name=*,type=chart");
            beans = getContext().getMBeanServerConnection().queryMBeans(objectName, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (ObjectInstance instance : beans) {

            String chartName = instance.getObjectName().getKeyProperty("name");
            ChartTab chartTab = new ChartTab(this);
            tabs.put(chartName, chartTab);
            this.chartTabs.put(instance.getObjectName(), chartTab);
        }

        return tabs;
    }

    @Override
    public SwingWorker<Map<ObjectName, ChartDataVO>, Object> newSwingWorker() {
        return new ChartWorker(this);
    }
}
