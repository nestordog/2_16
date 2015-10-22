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
package ch.algotrader.vo.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Defines a Chart Axis
 */
public class AxisDefinitionVO implements Serializable {

    private static final long serialVersionUID = 4135231334559917803L;

    /**
     * The Axis display Label
     */
    private String label;

    /**
     * If {@code true} this Axis Scale will be auto formated.
     */
    private boolean autoRange;

    /**
     * If {@code autoRange} is set to {@code true} should this Axis contain the zero value.
     */
    private boolean autoRangeIncludesZero;

    /**
     * the upper bound, this Axis should display
     */
    private double upperBound;

    /**
     * the lower bound, this Axis should display
     */
    private double lowerBound;

    /**
     * The {@code java.text.NumberFormat} the Axis Labels should be formated with (e.g. for the
     * number of digits to display)
     */
    private String numberFormat;

    /**
     * Defines a Chart Dataset
     */
    private Collection<DatasetDefinitionVO> datasetDefinitions;

    /**
     * Default Constructor
     */
    public AxisDefinitionVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor taking only required properties
     * @param autoRangeIn boolean If {@code true} this Axis Scale will be auto formated.
     * @param autoRangeIncludesZeroIn boolean If {@code autoRange} is set to {@code true} should this Axis contain the zero value.
     * @param upperBoundIn double the upper bound, this Axis should display
     * @param lowerBoundIn double the lower bound, this Axis should display
     */
    public AxisDefinitionVO(final boolean autoRangeIn, final boolean autoRangeIncludesZeroIn, final double upperBoundIn, final double lowerBoundIn) {

        this.autoRange = autoRangeIn;
        this.autoRangeIncludesZero = autoRangeIncludesZeroIn;
        this.upperBound = upperBoundIn;
        this.lowerBound = lowerBoundIn;
    }

    /**
     * Constructor with all properties
     * @param labelIn String
     * @param autoRangeIn boolean
     * @param autoRangeIncludesZeroIn boolean
     * @param upperBoundIn double
     * @param lowerBoundIn double
     * @param numberFormatIn String
     * @param datasetDefinitionsIn Collection<DatasetDefinitionVO>
     */
    public AxisDefinitionVO(final String labelIn, final boolean autoRangeIn, final boolean autoRangeIncludesZeroIn, final double upperBoundIn, final double lowerBoundIn, final String numberFormatIn,
            final Collection<DatasetDefinitionVO> datasetDefinitionsIn) {

        this.label = labelIn;
        this.autoRange = autoRangeIn;
        this.autoRangeIncludesZero = autoRangeIncludesZeroIn;
        this.upperBound = upperBoundIn;
        this.lowerBound = lowerBoundIn;
        this.numberFormat = numberFormatIn;
        this.datasetDefinitions = datasetDefinitionsIn;
    }

    /**
     * Copies constructor from other AxisDefinitionVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public AxisDefinitionVO(final AxisDefinitionVO otherBean) {

        this.label = otherBean.getLabel();
        this.autoRange = otherBean.isAutoRange();
        this.autoRangeIncludesZero = otherBean.isAutoRangeIncludesZero();
        this.upperBound = otherBean.getUpperBound();
        this.lowerBound = otherBean.getLowerBound();
        this.numberFormat = otherBean.getNumberFormat();
        this.datasetDefinitions = otherBean.getDatasetDefinitions();
    }

    /**
     * The Axis display Label
     * @return label String
     */
    public String getLabel() {

        return this.label;
    }

    /**
     * The Axis display Label
     * @param value String
     */
    public void setLabel(final String value) {

        this.label = value;
    }

    /**
     * If {@code true} this Axis Scale will be auto formated.
     * @return autoRange boolean
     */
    public boolean isAutoRange() {

        return this.autoRange;
    }

    /**
     * If {@code true} this Axis Scale will be auto formated.
     * Duplicates isBoolean method, for use as Jaxb2 compatible object
     * @return autoRange boolean
     */
    @Deprecated
    public boolean getAutoRange() {

        return this.autoRange;
    }

    /**
     * If {@code true} this Axis Scale will be auto formated.
     * @param value boolean
     */
    public void setAutoRange(final boolean value) {

        this.autoRange = value;
    }

    /**
     * If {@code autoRange} is set to {@code true} should this Axis contain the zero value.
     * @return autoRangeIncludesZero boolean
     */
    public boolean isAutoRangeIncludesZero() {

        return this.autoRangeIncludesZero;
    }

    /**
     * If {@code autoRange} is set to {@code true} should this Axis contain the zero value.
     * Duplicates isBoolean method, for use as Jaxb2 compatible object
     * @return autoRangeIncludesZero boolean
     */
    @Deprecated
    public boolean getAutoRangeIncludesZero() {

        return this.autoRangeIncludesZero;
    }

    /**
     * If {@code autoRange} is set to {@code true} should this Axis contain the zero value.
     * @param value boolean
     */
    public void setAutoRangeIncludesZero(final boolean value) {

        this.autoRangeIncludesZero = value;
    }

    /**
     * the upper bound, this Axis should display
     * @return upperBound double
     */
    public double getUpperBound() {

        return this.upperBound;
    }

    /**
     * the upper bound, this Axis should display
     * @param value double
     */
    public void setUpperBound(final double value) {

        this.upperBound = value;
    }

    /**
     * the lower bound, this Axis should display
     * @return lowerBound double
     */
    public double getLowerBound() {

        return this.lowerBound;
    }

    /**
     * the lower bound, this Axis should display
     * @param value double
     */
    public void setLowerBound(final double value) {

        this.lowerBound = value;
    }

    /**
     * The {@code java.text.NumberFormat} the Axis Labels should be formated with (e.g. for the
     * number of digits to display)
     * @return numberFormat String
     */
    public String getNumberFormat() {

        return this.numberFormat;
    }

    /**
     * The {@code java.text.NumberFormat} the Axis Labels should be formated with (e.g. for the
     * number of digits to display)
     * @param value String
     */
    public void setNumberFormat(final String value) {

        this.numberFormat = value;
    }

    /**
     * Defines a Chart Dataset
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the object.
     * @return this.datasetDefinitions Collection<DatasetDefinitionVO>
     */
    public Collection<DatasetDefinitionVO> getDatasetDefinitions() {

        if (this.datasetDefinitions == null) {
            this.datasetDefinitions = new ArrayList<>();
        }
        return this.datasetDefinitions;
    }

    /**
     * Sets the datasetDefinitions
     * @param value Collection<DatasetDefinitionVO>
     */
    public void setDatasetDefinitions(Collection<DatasetDefinitionVO> value) {

        this.datasetDefinitions = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("AxisDefinitionVO [label=");
        builder.append(this.label);
        builder.append(", autoRange=");
        builder.append(this.autoRange);
        builder.append(", autoRangeIncludesZero=");
        builder.append(this.autoRangeIncludesZero);
        builder.append(", upperBound=");
        builder.append(this.upperBound);
        builder.append(", lowerBound=");
        builder.append(this.lowerBound);
        builder.append(", numberFormat=");
        builder.append(this.numberFormat);
        builder.append(", datasetDefinitions=");
        builder.append(this.datasetDefinitions);
        builder.append("]");

        return builder.toString();
    }

}
