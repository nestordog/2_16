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

import ch.algotrader.entity.marketData.MarketDataEventVO;

/**
 * Platform wide communication interface capable of submitting events to multiple
 * {@link ch.algotrader.esper.Engine}s and {@link ch.algotrader.event.EventListener}s
 * both inside and outside of the current JRE.
 * <p>
 * <table border="1" style="border-collapse: collapse;" >
 *  <tr>
 *   <td></td>
 *   <td colspan=3>Local</td>
 *   <td>Remote</td>
 *  </tr>
 *  <tr>
 *   <td></td>
 *   <td>Server Engine</td>
 *   <td>Strategy Engine</td>
 *   <td>Event Listeners</td>
 *   <td>VM's</td>
 *  </tr>
 *  <tr>
 *   <td>broadcastLocalEventListeners</td>
 *   <td></td>
 *   <td></td>
 *   <td>x</td>
 *   <td></td>
 *  </tr>
 *  <tr>
 *   <td>broadcastLocalStrategies</td>
 *   <td></td>
 *   <td>x</td>
 *   <td>x</td>
 *   <td></td>
 *  </tr>
 *  <tr>
 *   <td>broadcastLocal</td>
 *   <td>x</td>
 *   <td>x</td>
 *   <td>x</td>
 *   <td></td>
 *  </tr>
 *  <tr>
 *   <td>broadcastRemote</td>
 *   <td></td>
 *   <td></td>
 *   <td></td>
 *   <td>x</td>
 *  </tr>
 *  <tr>
 *   <td>broadcastEventListeners</td>
 *   <td></td>
 *   <td></td>
 *   <td>x</td>
 *   <td>x</td>
 *  </tr>
 *  <tr>
 *   <td>broadcastAllStrategies</td>
 *   <td></td>
 *   <td>x</td>
 *   <td>x</td>
 *   <td>x</td>
 *  </tr>
 *  <tr>
 *   <td>broadcast</td>
 *   <td>x</td>
 *   <td>x</td>
 *   <td>x</td>
 *   <td>x</td>
 *  </tr>
 * </table>
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
     * broadcasts an event to all local event listeners.
     */
    void broadcastLocalEventListeners(Object event);

    /**
     * broadcasts an event to all local Strategy Esper Engines and event listeners.
     */
    void broadcastLocalStrategies(Object event);

    /**
     * broadcasts an event to all local Esper Engines (including SERVER if local) and event listeners.
     */
    void broadcastLocal(Object event);

    /**
     * broadcasts an event to all remote VM's.
     */
    void broadcastRemote(Object event);

    /**
     * broadcasts an event to all local event listeners as well as remote VM's.
     */
    void broadcastEventListeners(Object event);

    /**
     * broadcasts an event to all local Strategy Esper Engines and event listeners as well as remote VM's.
     */
    void broadcastAllStrategies(Object event);

    /**
     * broadcasts an event to all local Esper Engines (including SERVER if local) and event listeners as well as remote VM's.
     */
    void broadcast(Object event);

    /**
     * Registers market data subscription subscription
     */
    void registerMarketDataSubscription(String strategyName, long securityId);

    /**
     * Un-registers market data subscription subscription
     */
    void unregisterMarketDataSubscription(String strategyName, long securityId);

    /**
     * Sends a MarketDataEvent into the corresponding Esper Engine.
     * In Live-Trading the {@code marketDataTemplate} will be used.
     */
    void sendMarketDataEvent(MarketDataEventVO marketDataEvent);

}
