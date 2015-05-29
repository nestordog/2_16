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
 * Contains a ValueMarker Data Point
 */
public class ValueMarkerVO extends MarkerVO {

    private static final long serialVersionUID = -3721163178115529075L;

    /**
     * The value to display.
     */
    private double value;

    /**
     * Default Constructor
     */
    public ValueMarkerVO() {

        super();
    }

    /**
     * Constructor with all properties
     * @param nameIn String
     * @param valueIn double
     */
    public ValueMarkerVO(final String nameIn, final double valueIn) {

        super(nameIn);
        this.value = valueIn;
    }

    /**
     * Copies constructor from other ValueMarkerVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public ValueMarkerVO(final ValueMarkerVO otherBean) {

        super(otherBean);
        this.value = otherBean.getValue();
    }

    /**
     * The value to display.
     * @return value double
     */
    public double getValue() {

        return this.value;
    }

    /**
     * The value to display.
     * @param value double
     */
    public void setValue(final double value) {

        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ValueMarkerVO [value=");
        builder.append(this.value);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
