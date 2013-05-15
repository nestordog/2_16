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

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
            ObjectName objectName = new ObjectName("*:name=*,type=chart");
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
