package com.algoTrader.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.algoTrader.client.IndicatorTab.JMXResult;
import com.sun.tools.jconsole.JConsoleContext;
import com.sun.tools.jconsole.JConsoleContext.ConnectionState;

public class IndicatorPlugin extends com.sun.tools.jconsole.JConsolePlugin implements PropertyChangeListener {

    private IndicatorTab panel = null;
    private Map<String, JPanel> customTabs = null;

    public IndicatorPlugin() {
        addContextPropertyChangeListener(this);
    }

    @Override
    public synchronized Map<String, JPanel> getTabs() {

        if (this.customTabs == null) {
            this.panel = new IndicatorTab();
            this.panel.setMBeanServerConnection(getContext().getMBeanServerConnection());

            this.customTabs = new LinkedHashMap<String, JPanel>();
            this.customTabs.put("Indicators", this.panel);
        }
        return this.customTabs;
    }

    @Override
    public SwingWorker<JMXResult, Object> newSwingWorker() {
        return this.panel.newSwingWorker();
    }

    public void propertyChange(PropertyChangeEvent ev) {
        String prop = ev.getPropertyName();

        if (prop == JConsoleContext.CONNECTION_STATE_PROPERTY) {

            ConnectionState newState = (ConnectionState) ev.getNewValue();

            if (newState == ConnectionState.CONNECTED && this.panel != null) {
                this.panel.setMBeanServerConnection(getContext().getMBeanServerConnection());
            }
        }
    }
}
