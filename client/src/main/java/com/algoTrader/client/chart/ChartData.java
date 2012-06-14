package com.algoTrader.client.chart;

import java.util.Collection;

import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.ChartDefinitionVO;
import com.algoTrader.vo.IndicatorVO;
import com.algoTrader.vo.MarkerVO;

public class ChartData {

    private ChartDefinitionVO chartDefinition;
    private Collection<IndicatorVO> indicators;
    private Collection<MarkerVO> markers;
    private Collection<BarVO> bars;

    public ChartDefinitionVO getChartDefinition() {
        return this.chartDefinition;
    }

    public void setChartDefinition(ChartDefinitionVO chartDefinition) {
        this.chartDefinition = chartDefinition;
    }

    public Collection<IndicatorVO> getIndicators() {
        return this.indicators;
    }

    public void setIndicators(Collection<IndicatorVO> indicators) {
        this.indicators = indicators;
    }

    public Collection<MarkerVO> getMarkers() {
        return this.markers;
    }

    public void setMarkers(Collection<MarkerVO> markers) {
        this.markers = markers;
    }

    public Collection<BarVO> getBars() {
        return this.bars;
    }

    public void setBars(Collection<BarVO> bars) {
        this.bars = bars;
    }

}
