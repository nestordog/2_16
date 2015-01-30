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
import java.util.Date;

/**
 * A ValueObject representing the performance during a particular period (Month or Year).
 */
public class PeriodPerformanceVO implements Serializable {

    private static final long serialVersionUID = 6716927755479943083L;

    /**
     * The date representing the Month or the Year of this Period.
     */
    private Date date;

    /**
     * The performance during this Period.
     */
    private double value;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setValue = false;

    /**
     * Default Constructor
     */
    public PeriodPerformanceVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param dateIn Date
     * @param valueIn double
     */
    public PeriodPerformanceVO(final Date dateIn, final double valueIn) {

        this.date = dateIn;
        this.value = valueIn;
        this.setValue = true;
    }

    /**
     * Copies constructor from other PeriodPerformanceVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public PeriodPerformanceVO(final PeriodPerformanceVO otherBean) {

        this.date = otherBean.getDate();
        this.value = otherBean.getValue();
        this.setValue = true;
    }

    /**
     * The date representing the Month or the Year of this Period.
     * Get the date Attribute
     * @return date Date
     */
    public Date getDate() {

        return this.date;
    }

    /**
     * The date representing the Month or the Year of this Period.
     * @param value Date
     */
    public void setDate(final Date value) {

        this.date = value;
    }

    /**
     * The performance during this Period.
     * Get the value Attribute
     * @return value double
     */
    public double getValue() {

        return this.value;
    }

    /**
     * The performance during this Period.
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
        builder.append("PeriodPerformanceVO [date=");
        builder.append(this.date);
        builder.append(", value=");
        builder.append(this.value);
        builder.append(", setValue=");
        builder.append(this.setValue);
        builder.append("]");

        return builder.toString();
    }

}
