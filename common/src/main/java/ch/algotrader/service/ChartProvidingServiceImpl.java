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

import org.apache.commons.lang.Validate;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

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
public class ChartProvidingServiceImpl implements ChartProvidingService {

    private ChartDefinitionVO diagramDefinition;

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Setter method for passing {@link ChartDefinitionVO ChartDefinitions} defined in the Spring Config File.")
    public void setChartDefinition(final ChartDefinitionVO chartDefinition) {

        Validate.notNull(chartDefinition, "Chart definition is null");
        Validate.notNull(chartDefinition.getTimePeriod(), "Chart definition time period is null");

        try {
            this.diagramDefinition = chartDefinition;
        } catch (Exception ex) {
            throw new ChartProvidingServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Return {@link ChartDefinitionVO ChartDefinitions} defined in the Spring Config File. This method can be overwritten to modify/amend ChartDefinitions (e.g. if the Security an Indicator is based on changes over time).")
    public ChartDefinitionVO getChartDefinition() {

        try {
            return this.diagramDefinition;
        } catch (Exception ex) {
            throw new ChartProvidingServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Returns the {@link BarVO Bar Data}")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "startDateTime", description = "startDateTime") })
    public Collection<BarVO> getBars(final long startDateTime) {

        try {
            return new HashSet<BarVO>();
        } catch (Exception ex) {
            throw new ChartProvidingServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Returns the {@link IndicatorVO Indicator Data}")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "startDateTime", description = "startDateTime") })
    public Collection<IndicatorVO> getIndicators(final long startDateTime) {

        try {
            return new HashSet<IndicatorVO>();
        } catch (Exception ex) {
            throw new ChartProvidingServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Returns the {@link MarkerVO Marker Data}")
    public Collection<MarkerVO> getMarkers() {

        try {
            return new HashSet<MarkerVO>();
        } catch (Exception ex) {
            throw new ChartProvidingServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedOperation(description = "Returns {@link AnnotationVO Annotations}")
    @ManagedOperationParameters({ @ManagedOperationParameter(name = "startDateTime", description = "startDateTime") })
    public Collection<AnnotationVO> getAnnotations(final long startDateTime) {

        try {
            return new HashSet<AnnotationVO>();
        } catch (Exception ex) {
            throw new ChartProvidingServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ManagedAttribute(description = "Returns the Chart Description.")
    public String getDescription() {

        try {
            return null;
        } catch (Exception ex) {
            throw new ChartProvidingServiceException(ex.getMessage(), ex);
        }
    }

}
