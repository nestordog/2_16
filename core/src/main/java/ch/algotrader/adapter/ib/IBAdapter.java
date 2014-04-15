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

import java.util.Map;

import ch.algotrader.enumeration.ConnectionState;

/**
 * Adapter for IBSessions (IB Connections).
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface IBAdapter {

    /**
     * (re)connects all IBSessions
     */
    void connect();

    /**
     * disconnects all IBSessions
     */
    void disconnect();

    /**
     * Returns a Map with {@code sessionId} and {@code connectionState} of all IBSessions
     */
    Map<Integer, ConnectionState> getConnectionStates();

}
