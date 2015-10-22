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
package ch.algotrader.enumeration;

/**
 * The Time-In-Force of an Order.
 */
public enum TIF {

    /**
     * Day Order
     */
    DAY,

    /**
     * Good-till-cancel
     */
    GTC,

    /**
     * Good-till-Day
     */
    GTD,

    /**
     * Immediate-or-Cancel
     */
    IOC,

    /**
     * Fill-or-Kill
     */
    FOK,

    /**
     * At-the-Opening
     */
    ATO,

    /**
     * At-the-Close
     */
    ATC;

    private static final long serialVersionUID = -1497035503058434972L;

}
