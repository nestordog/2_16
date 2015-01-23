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
package ch.algotrader.esper;

import java.util.Date;

import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.vo.GenericEventVO;

/**
 * Management interface for multiple {@link ch.algotrader.esper.Engine}s.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface EngineManager {

    /**
     * Returns {@code true} if an engine with the given name is available
     * @param engineName
     * @return
     */
    boolean hasEngine(String engineName);

    /**
     * Returns engine with the given name
     * @param engineName
     * @return
     */
    Engine getEngine(String engineName);

    /**
     * Returns SERVER engine.
     * @return
     */
    Engine getServerEngine();

    /**
     * Destroys engine with the given name
     * @param engineName
     * @return
     */
    void destroyEngine(String engineName);

    /**
     * Returns current time of the local Engine.
     * If the local engine is not yet initialized or is using internal clock the current system date is returned.
     */
    Date getCurrentEPTime();

    /**
     * Sends an Event to the corresponding Esper Engine.
     * Use this method for situations where the corresponding Engine might be running localy ore remote,
     * otherwise use {@link ch.algotrader.esper.Engine#sendEvent}.
     *
     */
    void sendEvent(String engineName, Object obj);

    /**
     * Sends a MarketDataEvent into the corresponding Esper Engine.
     * In Live-Trading the {@code marketDataTemplate} will be used.
     */
    void sendMarketDataEvent(MarketDataEvent marketDataEvent);

    /**
     * Sends a MarketDataEvent into the corresponding Esper Engine.
     * In Live-Trading the {@code genericTemplate} will be used.
     */
    void sendGenericEvent(GenericEventVO event);

    /**
     * Sends an event to all local Esper Engines
     */
    void sendEventToAllEngines(Object obj);

    /**
     * Prints all statement metrics.
     */
    void logStatementMetrics();

    /**
     * Resets all statement metrics.
     */
    void resetStatementMetrics();

}
