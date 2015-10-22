/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service;

import java.util.Collection;
import java.util.Collections;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

import ch.algotrader.vo.client.AnnotationVO;
import ch.algotrader.vo.client.BarVO;
import ch.algotrader.vo.client.ChartDefinitionVO;
import ch.algotrader.vo.client.IndicatorVO;
import ch.algotrader.vo.client.MarkerVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class ChartProvidingServiceImpl implements ChartProvidingService {

    private final ChartDefinitionVO diagramDefinition;

    public ChartProvidingServiceImpl(ChartDefinitionVO diagramDefinition) {
        this.diagramDefinition = diagramDefinition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Return {@link ChartDefinitionVO ChartDefinitions} defined in the Spring Config File. This method can be overwritten to modify/amend ChartDefinitions (e.g. if the Security an Indicator is based on changes over time).")
    public ChartDefinitionVO getChartDefinition() {

        return this.diagramDefinition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Returns the {@link BarVO Bar Data}")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "startDateTime", description = "startDateTime") })
    public Collection<BarVO> getBars(final long startDateTime) {

        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Returns the {@link IndicatorVO Indicator Data}")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "startDateTime", description = "startDateTime") })
    public Collection<IndicatorVO> getIndicators(final long startDateTime) {

        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Returns the {@link MarkerVO Marker Data}")
    public Collection<MarkerVO> getMarkers() {

        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Returns {@link AnnotationVO Annotations}")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "startDateTime", description = "startDateTime") })
    public Collection<AnnotationVO> getAnnotations(final long startDateTime) {

        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Returns the Chart Description.")
    public String getDescription() {

        return null;
    }

}
