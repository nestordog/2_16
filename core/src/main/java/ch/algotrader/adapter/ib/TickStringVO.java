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
public class TickStringVO implements Serializable {

    private static final long serialVersionUID = -5497096398709248042L;

    private final String tickerId;
    private final int field;
    private final String value;

    /**
     * Constructor with all properties
     * @param tickerId int
     * @param field int
     * @param value String
     */
    public TickStringVO(final String tickerId, final int field, final String value) {
        this.tickerId = tickerId;
        this.field = field;
        this.value = value;
    }

    /**
     * Get the tickerId Attribute
     * @return tickerId int
     */
    public String getTickerId() {
        return this.tickerId;
    }

    /**
     * Get the field Attribute
     * @return field int
     */
    public int getField() {
        return this.field;
    }

    /**
     * Get the value Attribute
     * @return value String
     */
    public String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "TickString{" +
                "tickerId='" + tickerId + '\'' +
                ", field=" + field +
                ", value='" + value + '\'' +
                '}';
    }

}
