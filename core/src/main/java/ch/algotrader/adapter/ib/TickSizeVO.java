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
 * POJO representing an IB size event.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickSizeVO implements Serializable {

    private static final long serialVersionUID = 7514087845300720397L;

    private final String tickerId;
    private final int field;
    private final int size;

    /**
     * Constructor with all properties
     * @param tickerId int
     * @param field int
     * @param size int
     */
    public TickSizeVO(final String tickerId, final int field, final int size) {
        this.tickerId = tickerId;
        this.field = field;
        this.size = size;
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
     * Get the size Attribute
     * @return size int
     */
    public int getSize() {
        return this.size;
    }

    @Override
    public String toString() {
        return "TickSizeVO{" +
                "tickerId='" + tickerId + '\'' +
                ", field=" + field +
                ", size=" + size +
                '}';
    }

}
