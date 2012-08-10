package com.algoTrader.service;

import java.util.HashSet;
import java.util.Set;

import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.ChartDefinitionVO;
import com.algoTrader.vo.IndicatorVO;
import com.algoTrader.vo.MarkerVO;

public class ChartProvidingServiceImpl extends ChartProvidingServiceBase {

    private ChartDefinitionVO diagramDefinition;

    @Override
    protected void handleSetChartDefinition(ChartDefinitionVO diagramDefinition) throws Exception {

        this.diagramDefinition = diagramDefinition;
    }

    @Override
    protected ChartDefinitionVO handleGetChartDefinition() throws Exception {

        return this.diagramDefinition;
    }

    @Override
    protected Set<IndicatorVO> handleGetIndicators(long startDateTime) throws Exception {

        return new HashSet<IndicatorVO>();
    }

    @Override
    protected Set<BarVO> handleGetBars(long startDateTime) throws Exception {

        return new HashSet<BarVO>();
    }

    @Override
    protected Set<MarkerVO> handleGetMarkers() throws Exception {

        return new HashSet<MarkerVO>();
    }
}
