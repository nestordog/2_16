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
     * The upper value to display.
     */
    private double endValue;

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
        this.endValue = endValueIn;
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
        this.endValue = otherBean.getEndValue();
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
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("IntervalMarkerVO [startValue=");
        builder.append(this.startValue);
        builder.append(", endValue=");
        builder.append(this.endValue);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
