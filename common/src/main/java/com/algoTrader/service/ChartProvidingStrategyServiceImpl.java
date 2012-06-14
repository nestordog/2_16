package com.algoTrader.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.ChartDefinitionVO;
import com.algoTrader.vo.IndicatorVO;
import com.algoTrader.vo.MarkerVO;

public class ChartProvidingStrategyServiceImpl extends ChartProvidingStrategyServiceBase {

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
    protected Set<IndicatorVO> handleGetIndicators(Date startDate) throws Exception {

        return new HashSet<IndicatorVO>();
    }

    @Override
    protected Set<BarVO> handleGetBars(Date startDate) throws Exception {

        return new HashSet<BarVO>();
    }

    @Override
    protected Set<MarkerVO> handleGetMarkers() throws Exception {

        return new HashSet<MarkerVO>();
    }
}
