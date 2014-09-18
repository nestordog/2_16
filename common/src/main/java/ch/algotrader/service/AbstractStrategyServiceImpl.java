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

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractStrategyServiceImpl implements StrategyService {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSimulation() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitSimulation() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getSimulationResults() {
        return new HashMap<String, Object>();
    }

}
