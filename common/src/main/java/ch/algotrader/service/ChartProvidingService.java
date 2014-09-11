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
public interface ChartProvidingService {

    /**
     * Return {@link ChartDefinitionVO ChartDefinitions} defined in the Spring Config File. This
     * method can be overwritten to modify/amend ChartDefinitions (e.g. if the Security an Indicator
     * is based on changes over time).
     */
    public ChartDefinitionVO getChartDefinition();

    /**
     * Returns the {@link BarVO Bar Data}
     */
    public Collection<BarVO> getBars(long startDateTime);

    /**
     * Returns the {@link IndicatorVO Indicator Data}
     */
    public Collection<IndicatorVO> getIndicators(long startDateTime);

    /**
     * Returns the {@link MarkerVO Marker Data}
     */
    public Collection<MarkerVO> getMarkers();

    /**
     * Returns {@link AnnotationVO Annotations}
     */
    public Collection<AnnotationVO> getAnnotations(long startDateTime);

    /**
     * Returns the Chart Description.
     */
    public String getDescription();

}
