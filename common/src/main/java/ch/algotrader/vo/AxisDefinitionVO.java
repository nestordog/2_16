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
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setAutoRange = false;

    /**
     * If {@code autoRange} is set to {@code true} should this Axis contain the zero value.
     */
    private boolean autoRangeIncludesZero;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setAutoRangeIncludesZero = false;

    /**
     * the upper bound, this Axis should display
     */
    private double upperBound;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setUpperBound = false;

    /**
     * the lower bound, this Axis should display
     */
    private double lowerBound;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setLowerBound = false;

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
        this.setAutoRange = true;
        this.autoRangeIncludesZero = autoRangeIncludesZeroIn;
        this.setAutoRangeIncludesZero = true;
        this.upperBound = upperBoundIn;
        this.setUpperBound = true;
        this.lowerBound = lowerBoundIn;
        this.setLowerBound = true;
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
        this.setAutoRange = true;
        this.autoRangeIncludesZero = autoRangeIncludesZeroIn;
        this.setAutoRangeIncludesZero = true;
        this.upperBound = upperBoundIn;
        this.setUpperBound = true;
        this.lowerBound = lowerBoundIn;
        this.setLowerBound = true;
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
        this.setAutoRange = true;
        this.autoRangeIncludesZero = otherBean.isAutoRangeIncludesZero();
        this.setAutoRangeIncludesZero = true;
        this.upperBound = otherBean.getUpperBound();
        this.setUpperBound = true;
        this.lowerBound = otherBean.getLowerBound();
        this.setLowerBound = true;
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
        this.setAutoRange = true;
    }

    /**
     * Return true if the primitive attribute autoRange is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetAutoRange() {

        return this.setAutoRange;
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
        this.setAutoRangeIncludesZero = true;
    }

    /**
     * Return true if the primitive attribute autoRangeIncludesZero is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetAutoRangeIncludesZero() {

        return this.setAutoRangeIncludesZero;
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
        this.setUpperBound = true;
    }

    /**
     * Return true if the primitive attribute upperBound is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetUpperBound() {

        return this.setUpperBound;
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
        this.setLowerBound = true;
    }

    /**
     * Return true if the primitive attribute lowerBound is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetLowerBound() {

        return this.setLowerBound;
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
        builder.append(", setAutoRange=");
        builder.append(this.setAutoRange);
        builder.append(", autoRangeIncludesZero=");
        builder.append(this.autoRangeIncludesZero);
        builder.append(", setAutoRangeIncludesZero=");
        builder.append(this.setAutoRangeIncludesZero);
        builder.append(", upperBound=");
        builder.append(this.upperBound);
        builder.append(", setUpperBound=");
        builder.append(this.setUpperBound);
        builder.append(", lowerBound=");
        builder.append(this.lowerBound);
        builder.append(", setLowerBound=");
        builder.append(this.setLowerBound);
        builder.append(", numberFormat=");
        builder.append(this.numberFormat);
        builder.append(", datasetDefinitions=");
        builder.append(this.datasetDefinitions);
        builder.append("]");

        return builder.toString();
    }

}
