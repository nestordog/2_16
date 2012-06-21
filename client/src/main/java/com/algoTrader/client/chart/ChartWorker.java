package com.algoTrader.client.chart;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.swing.SwingWorker;

import com.algoTrader.service.ChartProvidingStrategyService;

public class ChartWorker extends SwingWorker<Map<String, ChartData>, Object> {

    private ChartPlugin chartPlugin;

    public ChartWorker(ChartPlugin chartPlugin) {
        this.chartPlugin = chartPlugin;
    }

    @Override
    protected void done() {

        Map<String, ChartData> result;
        try {
            result = get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // process all ChartData
        for (Map.Entry<String, ChartData> entry : result.entrySet()) {

            // get the chartTab by its name
            ChartTab chartTab = this.chartPlugin.getChartTabs().get(entry.getKey());

            // init the selectionPanel if a chart definition was returned
            if (entry.getValue().getChartDefinition() != null) {

                chartTab.init(entry.getValue().getChartDefinition());

                this.chartPlugin.setInitialized(true);
            }

            // update the timeSeriesChart
            chartTab.updateData(entry.getValue());
        }
    }

    @Override
    public Map<String, ChartData> doInBackground() {

        Map<String, ChartData> chartDataMap = new HashMap<String, ChartData>();
        for (Map.Entry<String, ChartTab> entry : this.chartPlugin.getChartTabs().entrySet()) {

            ChartData chartData = new ChartData();

            ObjectName mbeanName = null;

            // see if the service is available
            try {
                mbeanName = new ObjectName(entry.getKey());
                this.chartPlugin.getMBeanServerConnection().getObjectInstance(mbeanName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            // get the managementService
            ChartProvidingStrategyService chartProvidingService = JMX.newMBeanProxy(this.chartPlugin.getMBeanServerConnection(), mbeanName, ChartProvidingStrategyService.class);

            // retrieve the charts if necessary
            long startDateTime = entry.getValue().getMaxDate();
            if (!this.chartPlugin.isInitialized()) {

                // return the charts, so the SelectionPanel can be initialized
                chartData.setChartDefinition(chartProvidingService.getChartDefinition());

                // if charts are not initialized yet, load all data
                startDateTime = 0;
            }

            // retrieve bars for the selected chart and securityIds
            chartData.setBars(chartProvidingService.getBars(startDateTime));

            // retrieve indicators for the selected chart and securityIds
            chartData.setIndicators(chartProvidingService.getIndicators(startDateTime));

            // retrieve markers for the selected chart and securityIds
            chartData.setMarkers(chartProvidingService.getMarkers());

            chartDataMap.put(entry.getKey(), chartData);
        }

        return chartDataMap;
    }
}
