/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util;

import java.util.ArrayList;
import java.util.Collection;

import ch.algotrader.vo.client.AxisDefinitionVO;
import ch.algotrader.vo.client.ChartDefinitionVO;
import ch.algotrader.vo.client.DatasetDefinitionVO;
import ch.algotrader.vo.client.SeriesDefinitionVO;

/**
 * Provides Lookup Methods for Chart Definitions.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ClientUtil {

    /**
     * Returns all {@link SeriesDefinitionVO SeriesDefinitionVOs} of the specified {@code type} for the {@code chartDefinition}
     */
    @SuppressWarnings("unchecked")
    public static <E extends SeriesDefinitionVO> Collection<E> getSeriesDefinitions(Class<E> type, ChartDefinitionVO chartDefinition) {

        Collection<E> barDefinitions = new ArrayList<>();
        for (AxisDefinitionVO axisDefinition : chartDefinition.getAxisDefinitions()) {
            for (DatasetDefinitionVO datasetDefinition : axisDefinition.getDatasetDefinitions()) {
                for (SeriesDefinitionVO seriesDefinition : datasetDefinition.getSeriesDefinitions()) {
                    if (seriesDefinition.getClass().isAssignableFrom(type)) {
                        barDefinitions.add((E) seriesDefinition);
                    }
                }
            }
        }

        return barDefinitions;
    }
}
