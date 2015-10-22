/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.ib;

import java.io.Serializable;

/**
 * POJO representing an IB price event.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TickPriceVO implements Serializable {

    private static final long serialVersionUID = -4096826680075105503L;

    private final String tickerId;
    private final int field;
    private final double price;
    private final int canAutoExecute;

    /**
     * Constructor with all properties
     * @param tickerId int
     * @param field int
     * @param price double
     * @param canAutoExecute int
     */
    public TickPriceVO(final String tickerId, final int field, final double price, final int canAutoExecute) {
        this.tickerId = tickerId;
        this.field = field;
        this.price = price;
        this.canAutoExecute = canAutoExecute;
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
     * Get the price Attribute
     * @return price double
     */
    public double getPrice() {
        return this.price;
    }

    /**
     * Get the canAutoExecute Attribute
     * @return canAutoExecute int
     */
    public int getCanAutoExecute() {
        return this.canAutoExecute;
    }

    @Override
    public String toString() {
        return "TickPrice{" +
                "tickerId='" + tickerId + '\'' +
                ", field=" + field +
                ", price=" + price +
                ", canAutoExecute=" + canAutoExecute +
                '}';
    }

}
