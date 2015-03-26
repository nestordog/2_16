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
package ch.algotrader.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Contains Chart Data
 */
public class ChartDataVO implements Serializable {

    private static final long serialVersionUID = -4144908391313155527L;

    /**
     * The description to be displayed at the Top of the Chart.
     */
    private String description;

    /**
     * A ValueObject representing a {@link ch.algotrader.entity.marketData.Bar Bar}.
     */
    private Collection<BarVO> bars;

    /**
     * Contains a Indicator Data Point
     */
    private Collection<IndicatorVO> indicators;

    /**
     * Contains a Marker Data Point
     */
    private Collection<MarkerVO> markers;

    /**
     * Contains Annotation Data
     */
    private Collection<AnnotationVO> annotations;

    /**
     * Defines a Chart.
     */
    private ChartDefinitionVO chartDefinition;

    /**
     * Constructor taking only required properties
     */
    public ChartDataVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param descriptionIn String
     * @param barsIn Collection<BarVO>
     * @param indicatorsIn Collection<IndicatorVO>
     * @param markersIn Collection<MarkerVO>
     * @param annotationsIn Collection<AnnotationVO>
     * @param chartDefinitionIn ChartDefinitionVO
     */
    public ChartDataVO(final String descriptionIn, final Collection<BarVO> barsIn, final Collection<IndicatorVO> indicatorsIn, final Collection<MarkerVO> markersIn,
            final Collection<AnnotationVO> annotationsIn, final ChartDefinitionVO chartDefinitionIn) {

        this.description = descriptionIn;
        this.bars = barsIn;
        this.indicators = indicatorsIn;
        this.markers = markersIn;
        this.annotations = annotationsIn;
        this.chartDefinition = chartDefinitionIn;
    }

    /**
     * Copies constructor from other ChartDataVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public ChartDataVO(final ChartDataVO otherBean) {

        this.description = otherBean.getDescription();
        this.bars = otherBean.getBars();
        this.indicators = otherBean.getIndicators();
        this.markers = otherBean.getMarkers();
        this.annotations = otherBean.getAnnotations();
        this.chartDefinition = otherBean.getChartDefinition();
    }

    /**
     * The description to be displayed at the Top of the Chart.
     * @return description String
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * The description to be displayed at the Top of the Chart.
     * @param value String
     */
    public void setDescription(final String value) {

        this.description = value;
    }

    /**
     * A ValueObject representing a {@link ch.algotrader.entity.marketData.Bar Bar}.
     * Get the bars Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.bars Collection<BarVO>
     */
    public Collection<BarVO> getBars() {

        if (this.bars == null) {

            this.bars = new ArrayList<>();
        }
        return this.bars;
    }

    /**
     * Sets the bars
     * @param value Collection<BarVO>
     */
    public void setBars(Collection<BarVO> value) {

        this.bars = value;
    }

    /**
     * Contains a Indicator Data Point
     * Get the indicators Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.indicators Collection<IndicatorVO>
     */
    public Collection<IndicatorVO> getIndicators() {

        if (this.indicators == null) {

            this.indicators = new ArrayList<>();
        }
        return this.indicators;
    }

    /**
     * Sets the indicators
     * @param value Collection<IndicatorVO>
     */
    public void setIndicators(Collection<IndicatorVO> value) {

        this.indicators = value;
    }

    /**
     * Contains a Marker Data Point
     * Get the markers Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.markers Collection<MarkerVO>
     */
    public Collection<MarkerVO> getMarkers() {

        if (this.markers == null) {

            this.markers = new ArrayList<>();
        }
        return this.markers;
    }

    /**
     * Sets the markers
     * @param value Collection<MarkerVO>
     */
    public void setMarkers(Collection<MarkerVO> value) {

        this.markers = value;
    }

    /**
     * Contains Annotation Data
     * Get the annotations Association
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.annotations Collection<AnnotationVO>
     */
    public Collection<AnnotationVO> getAnnotations() {

        if (this.annotations == null) {

            this.annotations = new ArrayList<>();
        }
        return this.annotations;
    }

    /**
     * Sets the annotations
     * @param value Collection<AnnotationVO>
     */
    public void setAnnotations(Collection<AnnotationVO> value) {

        this.annotations = value;
    }

    public ChartDefinitionVO getChartDefinition() {

        return this.chartDefinition;
    }

    /**
     * Sets the chartDefinition
     * @param value ChartDefinitionVO
     */
    public void setChartDefinition(ChartDefinitionVO value) {

        this.chartDefinition = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ChartDataVO [description=");
        builder.append(this.description);
        builder.append(", bars=");
        builder.append(this.bars);
        builder.append(", indicators=");
        builder.append(this.indicators);
        builder.append(", markers=");
        builder.append(this.markers);
        builder.append(", annotations=");
        builder.append(this.annotations);
        builder.append(", chartDefinition=");
        builder.append(this.chartDefinition);
        builder.append("]");

        return builder.toString();
    }

}
