package com.algoTrader.util;

import java.util.Collection;
import java.util.HashSet;

import com.algoTrader.vo.AxisDefinitionVO;
import com.algoTrader.vo.ChartDefinitionVO;
import com.algoTrader.vo.DatasetDefinitionVO;
import com.algoTrader.vo.SeriesDefinitionVO;

public class ClientUtil {

    @SuppressWarnings("unchecked")
    public static <E extends SeriesDefinitionVO> Collection<E> getSeriesDefinitions(Class<E> type, ChartDefinitionVO chartDefinition) {

        AxisDefinitionVO axisDefinition = chartDefinition.getAxisDefinitions().iterator().next();
        DatasetDefinitionVO datasetDefinition = axisDefinition.getDatasetDefinitions().iterator().next();

        Collection<E> barDefinitions = new HashSet<E>();
        for (SeriesDefinitionVO seriesDefinition : datasetDefinition.getSeriesDefinitions()) {

            if (seriesDefinition.getClass().isAssignableFrom(type)) {
                barDefinitions.add((E) seriesDefinition);
            }
        }

        return barDefinitions;
    }
}
