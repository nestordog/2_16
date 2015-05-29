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
 * Contains a Indicator Data Point
 */
public class IndicatorVO implements Serializable {

    private static final long serialVersionUID = -1214724634260113134L;

    /**
     * The name of the Indicator. Has to match {@link IndicatorDefinitionVO#getName}.
     */
    private String name;

    /**
     * The dateTime of this Data Point
     */
    private Date dateTime;

    /**
     * The value to display.
     */
    private double value;

    /**
     * Default Constructor
     */
    public IndicatorVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param nameIn String
     * @param dateTimeIn Date
     * @param valueIn double
     */
    public IndicatorVO(final String nameIn, final Date dateTimeIn, final double valueIn) {

        this.name = nameIn;
        this.dateTime = dateTimeIn;
        this.value = valueIn;
    }

    /**
     * Copies constructor from other IndicatorVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public IndicatorVO(final IndicatorVO otherBean) {

        this.name = otherBean.getName();
        this.dateTime = otherBean.getDateTime();
        this.value = otherBean.getValue();
    }

    /**
     * The name of the Indicator. Has to match {@link IndicatorDefinitionVO#getName}.
     * @return name String
     */
    public String getName() {

        return this.name;
    }

    /**
     * The name of the Indicator. Has to match {@link IndicatorDefinitionVO#getName}.
     * @param value String
     */
    public void setName(final String value) {

        this.name = value;
    }

    /**
     * The dateTime of this Data Point
     * @return dateTime Date
     */
    public Date getDateTime() {

        return this.dateTime;
    }

    /**
     * The dateTime of this Data Point
     * @param value Date
     */
    public void setDateTime(final Date value) {

        this.dateTime = value;
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
        builder.append("IndicatorVO [name=");
        builder.append(this.name);
        builder.append(", dateTime=");
        builder.append(this.dateTime);
        builder.append(", value=");
        builder.append(this.value);
        builder.append("]");

        return builder.toString();
    }

}
