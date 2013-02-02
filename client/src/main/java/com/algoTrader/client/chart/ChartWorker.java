package com.algoTrader.client.chart;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;

import javax.management.JMX;
import javax.management.ObjectName;
import javax.swing.SwingWorker;

import com.algoTrader.client.WarningProducer;
import com.algoTrader.service.ChartProvidingService;
import com.algoTrader.service.ManagementService;
import com.algoTrader.vo.ChartDataVO;

public class ChartWorker extends SwingWorker<Map<ObjectName, ChartDataVO>, Object> {

    private ChartPlugin chartPlugin;

    public ChartWorker(ChartPlugin chartPlugin) {
        this.chartPlugin = chartPlugin;
    }

    @Override
    protected void done() {

        try {
            Map<ObjectName, ChartDataVO> result = get();

            // process all ChartDataVO
            for (Map.Entry<ObjectName, ChartDataVO> entry : result.entrySet()) {

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
        } catch (CancellationException e) {
            System.out.println("SwingWorker was cancelled");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<ObjectName, ChartDataVO> doInBackground() {

        // todo vitality check
        ObjectName objectName = null;
        try {

            // call the checkIsAlive test
            objectName = new ObjectName("com.algoTrader.service:name=ManagementService");
            ManagementService managementService = JMX.newMBeanProxy(this.chartPlugin.getMBeanServerConnection(), objectName, ManagementService.class);
            managementService.checkIsAlive();

            Map<ObjectName, ChartDataVO> chartDataMap = new HashMap<ObjectName, ChartDataVO>();
            for (Map.Entry<ObjectName, ChartTab> entry : this.chartPlugin.getChartTabs().entrySet()) {

                // get the managementService
                ChartProvidingService chartProvidingService = JMX.newMBeanProxy(this.chartPlugin.getMBeanServerConnection(), entry.getKey(), ChartProvidingService.class);

                // retrieve the charts if necessary
                ChartDataVO chartData = new ChartDataVO();
                long startDateTime = entry.getValue().getMaxDate();
                if (!this.chartPlugin.isInitialized()) {

                    // return the charts, so the SelectionPanel can be initialized
                    chartData.setChartDefinition(chartProvidingService.getChartDefinition());

                    // if charts are not initialized yet, load all data
                    startDateTime = 0;
                }

                // retrieve bars
                chartData.setBars(chartProvidingService.getBars(startDateTime));

                // retrieve indicators
                chartData.setIndicators(chartProvidingService.getIndicators(startDateTime));

                // retrieve markers
                chartData.setMarkers(chartProvidingService.getMarkers());

                // retrieve annotations
                chartData.setAnnotations(chartProvidingService.getAnnotations(startDateTime));

                // retrieve description
                chartData.setDescription(chartProvidingService.getDescription());

                chartDataMap.put(entry.getKey(), chartData);
            }

            return chartDataMap;

        } catch (Exception e) {
            WarningProducer.produceWarning(e);
            throw new RuntimeException(e);
        }
    }
}
