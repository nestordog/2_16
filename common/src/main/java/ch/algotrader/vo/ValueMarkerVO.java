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
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setValue = false;

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
        this.setValue = true;
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
        this.setValue = true;
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
        this.setValue = true;
    }

    /**
     * Return true if the primitive attribute value is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetValue() {

        return this.setValue;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("ValueMarkerVO [value=");
        builder.append(this.value);
        builder.append(", setValue=");
        builder.append(this.setValue);
        builder.append(", ");
        builder.append(super.toString());
        builder.append("]");

        return builder.toString();
    }

}
