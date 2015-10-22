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
 * the One-Cancels-Other Type of an Order Group
 */
public enum OCOType {

    /**
     * None
     */
    NONE,

    /**
     * One Fill cancels all other Orders
     */
    CANCEL_OTHERS,

    /**
     * One Fill reduces the quantity of the other Order
     */
    REDUCE_OTHERS;

    private static final long serialVersionUID = 6111674754011576987L;

}
