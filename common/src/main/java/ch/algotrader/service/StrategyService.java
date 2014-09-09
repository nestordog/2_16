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
package ch.algotrader.service;

import java.util.Map;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface StrategyService {

    /**
     * Invoke potential initialization tasks at the beginning of a Simulation Run.
     */
    public void initSimulation();

    /**
     * Returns strategy specific Simulation Results for a Simulation Run as a Map with a String Key.
     */
    public Map<String, Object> getSimulationResults();

    /**
     * Invoke potential finalization tasks at the end of a Simulation Run.
     */
    public void exitSimulation();

}
