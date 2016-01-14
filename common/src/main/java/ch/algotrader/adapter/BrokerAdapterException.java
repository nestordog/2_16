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
package ch.algotrader.adapter;

/**
 * Signals Broker adapter exception.
 */
public class BrokerAdapterException extends RuntimeException {

    private static final long serialVersionUID = -7833579148653485688L;

    public BrokerAdapterException(final String message) {
        super(message);
    }

    public BrokerAdapterException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
