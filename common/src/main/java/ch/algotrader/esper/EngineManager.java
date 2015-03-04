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

import java.util.Collection;
import java.util.Date;

/**
 * Management interface for multiple {@link ch.algotrader.esper.Engine}s.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface EngineManager {

    /**
     * Returns all avaialbe engines.
     * @return
     */
    Collection<Engine> getEngines();

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
     * Prints all statement metrics.
     */
    void logStatementMetrics();

    /**
     * Resets all statement metrics.
     */
    void resetStatementMetrics();

}
