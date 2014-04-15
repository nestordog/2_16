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
 * POJO representing an IB price event.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class TickPrice implements Serializable {

    private static final long serialVersionUID = -4096826680075105503L;

    protected String tickerId;
    protected int field;
    protected double price;
    protected int canAutoExecute;

    /**
     * Constructor with all properties
     * @param tickerIdIn int
     * @param fieldIn int
     * @param priceIn double
     * @param canAutoExecuteIn int
     */
    public TickPrice(final String tickerIdIn, final int fieldIn, final double priceIn, final int canAutoExecuteIn) {
        this.tickerId = tickerIdIn;
        this.field = fieldIn;
        this.price = priceIn;
        this.canAutoExecute = canAutoExecuteIn;
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
     * Get the price Attribute
     * @return price double
     */
    public double getPrice() {
        return this.price;
    }

    /**
     *
     * @param value double
     */
    public void setPrice(final double value) {
        this.price = value;
    }

    /**
     * Get the canAutoExecute Attribute
     * @return canAutoExecute int
     */
    public int getCanAutoExecute() {
        return this.canAutoExecute;
    }
}
