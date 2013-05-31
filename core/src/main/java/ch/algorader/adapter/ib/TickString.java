/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algorader.adapter.ib;

import java.io.Serializable;

/**
 * POJO representing an IB string event.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickString implements Serializable {

    private static final long serialVersionUID = -5497096398709248042L;

    protected int tickerId;
    protected int field;
    protected String value;

    /**
     * Constructor with all properties
     * @param tickerIdIn int
     * @param fieldIn int
     * @param valueIn String
     */
    public TickString(final int tickerIdIn, final int fieldIn, final String valueIn) {
        this.tickerId = tickerIdIn;
        this.field = fieldIn;
        this.value = valueIn;
    }

    /**
     * Get the tickerId Attribute
     * @return tickerId int
     */
    public int getTickerId() {
        return this.tickerId;
    }

    /**
     *
     * @param value int
     */
    public void setTickerId(final int value) {
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
