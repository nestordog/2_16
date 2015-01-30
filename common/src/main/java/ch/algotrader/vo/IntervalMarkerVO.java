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


/**
 * Contains a IntervalMarker Data Point
 */
public class IntervalMarkerVO extends MarkerVO {

    private static final long serialVersionUID = 4553163156975195462L;

    /**
     * The lower value to display.
     */
    private double startValue;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setStartValue = false;

    /**
     * The upper value to display.
     */
    private double endValue;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setEndValue = false;

    /**
     * Default Constructor
     */
    public IntervalMarkerVO() {

        super();
    }

    /**
     * Constructor with all properties
     * @param nameIn String
     * @param startValueIn double
     * @param endValueIn double
     */
    public IntervalMarkerVO(final String nameIn, final double startValueIn, final double endValueIn) {

        super(nameIn);

        this.startValue = startValueIn;
        this.setStartValue = true;
        this.endValue = endValueIn;
        this.setEndValue = true;
    }

    /**
     * Copies constructor from other IntervalMarkerVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public IntervalMarkerVO(final IntervalMarkerVO otherBean) {

        super(otherBean);

        this.startValue = otherBean.getStartValue();
        this.setStartValue = true;
        this.endValue = otherBean.getEndValue();
        this.setEndValue = true;
    }

    /**
     * The lower value to display.
     * @return startValue double
     */
    public double getStartValue() {

        return this.startValue;
    }

    /**
     * The lower value to display.
     * @param value double
     */
    public void setStartValue(final double value) {

        this.startValue = value;
        this.setStartValue = true;
    }

    /**
     * Return true if the primitive attribute startValue is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetStartValue() {

        return this.setStartValue;
    }

    /**
     * The upper value to display.
     * @return endValue double
     */
    public double getEndValue() {

        return this.endValue;
    }

    /**
     * The upper value to display.
     * @param value double
     */
    public void setEndValue(final double value) {

        this.endValue = value;
        this.setEndValue = true;
    }

    /**
     * Return true if the primitive attribute endValue is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetEndValue() {

        return this.setEndValue;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("IntervalMarkerVO [startValue=");
        builder.append(this.startValue);
        builder.append(", setStartValue=");
        builder.append(this.setStartValue);
        builder.append(", endValue=");
        builder.append(this.endValue);
        builder.append(", setEndValue=");
        builder.append(this.setEndValue);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
