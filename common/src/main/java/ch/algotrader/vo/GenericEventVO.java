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

/**
 * Base Class for all Generic Events
 */
public class GenericEventVO implements Serializable {

    private static final long serialVersionUID = 5548989722177986415L;

    /**
     * The time in milliseconds.
     */
    private long time;

    /**
     * boolean setter for primitive attribute, so we can tell if it's initialized
     */
    private boolean setTime = false;

    /**
     * Default Constructor
     */
    public GenericEventVO() {

        // Documented empty block - avoid compiler warning - no super constructor
    }

    /**
     * Constructor with all properties
     * @param timeIn long
     */
    public GenericEventVO(final long timeIn) {

        this.time = timeIn;
        this.setTime = true;
    }

    /**
     * Copies constructor from other GenericEventVO
     *
     * @param otherBean Cannot be <code>null</code>
     * @throws NullPointerException if the argument is <code>null</code>
     */
    public GenericEventVO(final GenericEventVO otherBean) {

        this.time = otherBean.getTime();
        this.setTime = true;
    }

    /**
     * The time in milliseconds.
     * @return time long
     */
    public long getTime() {

        return this.time;
    }

    /**
     * The time in milliseconds.
     * @param value long
     */
    public void setTime(final long value) {

        this.time = value;
        this.setTime = true;
    }

    /**
     * Return true if the primitive attribute time is set, through the setter or constructor
     * @return true if the attribute value has been set
     */
    public boolean isSetTime() {

        return this.setTime;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("GenericEventVO [time=");
        builder.append(this.time);
        builder.append(", setTime=");
        builder.append(this.setTime);
        builder.append("]");

        return builder.toString();
    }

}
