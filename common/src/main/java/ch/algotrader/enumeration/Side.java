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
package ch.algotrader.enumeration;

/**
 * The Side of an Order ({@code BUY} or {@code SELL})
 */
public enum Side {

    BUY("B"),

    SELL("S"),

    /**
     * Represent Short Selling of a Stock. To reverse a short position use {@code BUY}
     */
    SELL_SHORT("SS");

    private static final long serialVersionUID = 7322438989658124648L;

    private final String enumValue;

    /**
     * The constructor with enumeration literal value allowing
     * super classes to access it.
     */
    private Side(String value) {

        this.enumValue = value;
    }

    /**
     * Gets the underlying value of this type safe enumeration.
     * This method is necessary to comply with DaoBase implementation.
     * @return The name of this literal.
     */
    public String getValue() {

        return this.enumValue;
    }

}
