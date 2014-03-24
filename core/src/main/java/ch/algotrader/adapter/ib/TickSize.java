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
package ch.algotrader.adapter.ib;

import java.io.Serializable;

/**
 * POJO representing an IB size event.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickSize implements Serializable {

    private static final long serialVersionUID = 7514087845300720397L;

    protected String tickerId;
    protected int field;
    protected int size;

    /**
     * Constructor with all properties
     * @param tickerIdIn int
     * @param fieldIn int
     * @param sizeIn int
     */
    public TickSize(final String tickerIdIn, final int fieldIn, final int sizeIn) {
        this.tickerId = tickerIdIn;
        this.field = fieldIn;
        this.size = sizeIn;
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
     * Get the size Attribute
     * @return size int
     */
    public int getSize() {
        return this.size;
    }

    /**
     *
     * @param value int
     */
    public void setSize(final int value) {
        this.size = value;
    }
}
