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
package ch.algotrader.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ch.algotrader.service.ChartProvidingServiceBase;
import ch.algotrader.vo.AnnotationVO;
import ch.algotrader.vo.BarVO;
import ch.algotrader.vo.ChartDefinitionVO;
import ch.algotrader.vo.IndicatorVO;
import ch.algotrader.vo.MarkerVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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

    @Override
    protected Collection<AnnotationVO> handleGetAnnotations(long startDateTime) throws Exception {

        return new HashSet<AnnotationVO>();
    }

    @Override
    protected String handleGetDescription() throws Exception {

        return null;
    }
}
