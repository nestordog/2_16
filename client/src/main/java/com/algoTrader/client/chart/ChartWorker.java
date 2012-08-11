package com.algoTrader.client.chart;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.swing.SwingWorker;

import com.algoTrader.service.ChartProvidingService;

public class ChartWorker extends SwingWorker<Map<ObjectName, ChartData>, Object> {

    private ChartPlugin chartPlugin;

    public ChartWorker(ChartPlugin chartPlugin) {
        this.chartPlugin = chartPlugin;
    }

    @Override
    protected void done() {

        Map<ObjectName, ChartData> result;
        try {
            result = get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // process all ChartData
        for (Map.Entry<ObjectName, ChartData> entry : result.entrySet()) {

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
    public Map<ObjectName, ChartData> doInBackground() {

        Map<ObjectName, ChartData> chartDataMap = new HashMap<ObjectName, ChartData>();
        for (Map.Entry<ObjectName, ChartTab> entry : this.chartPlugin.getChartTabs().entrySet()) {

            ChartData chartData = new ChartData();

            // see if the service is available
            try {
                this.chartPlugin.getMBeanServerConnection().getObjectInstance(entry.getKey());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            // get the managementService
            ChartProvidingService chartProvidingService = JMX.newMBeanProxy(this.chartPlugin.getMBeanServerConnection(), entry.getKey(), ChartProvidingService.class);

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
