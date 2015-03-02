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
package ch.algotrader.event.dispatch;

import ch.algotrader.entity.marketData.MarketDataEvent;

/**
 * Platform wide communication interface capable of submitting events to multiple
 * {@link ch.algotrader.esper.Engine}s and {@link ch.algotrader.event.EventListener}s
 * both inside and outside of the current JRE.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface EventDispatcher {

    /**
     * Sends an Event to the corresponding Esper Engine.
     * Use this method for situations where the corresponding Engine might be running local or remote,
     * otherwise use {@link ch.algotrader.esper.Engine#sendEvent}.
     *
     */
    void sendEvent(String engineName, Object event);

    /**
     * Sends a MarketDataEvent into the corresponding Esper Engine.
     * In Live-Trading the {@code marketDataTemplate} will be used.
     */
    void sendMarketDataEvent(MarketDataEvent marketDataEvent);

    /**
     * broadcasts an event to all Esper Engines and event listeners both local and remote.
     */
    void broadcast(Object event);

    /**
     * broadcasts an event to all local Esper Engines and event listeners.
     */
    void broadcastLocal(Object event);

    /**
     * broadcasts an event to all remote Esper Engines and event listeners.
     */
    void broadcastRemote(Object event);

}
