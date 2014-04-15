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
package ch.algotrader.adapter.ib;

import java.io.Serializable;

/**
 * POJO representing an IB string event.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickString implements Serializable {

    private static final long serialVersionUID = -5497096398709248042L;

    protected String tickerId;
    protected int field;
    protected String value;

    /**
     * Constructor with all properties
     * @param tickerIdIn int
     * @param fieldIn int
     * @param valueIn String
     */
    public TickString(final String tickerIdIn, final int fieldIn, final String valueIn) {
        this.tickerId = tickerIdIn;
        this.field = fieldIn;
        this.value = valueIn;
    }

    /**
     * Get the tickerId Attribute
     * @return tickerId int
     */
    public String getTickerId() {
        return this.tickerId;
    }

    /**
     *
     * @param value int
     */
    public void setTickerId(final String value) {
        this.tickerId = value;
    }

    /**
     * Get the field Attribute
     * @return field int
     */
    public int getField() {
        return this.field;
    }

    /**
     *
     * @param value int
     */
    public void setField(final int value) {
        this.field = value;
    }

    /**
     * Get the value Attribute
     * @return value String
     */
    public String getValue() {
        return this.value;
    }

    /**
     *
     * @param value String
     */
    public void setValue(final String value) {
        this.value = value;
    }
}
