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
package ch.algotrader.esper.callback;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.esper.Engine;

/**
 * Base Esper Callback Class that will be invoked on the give dateTime
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class TimerCallback extends AbstractEngineCallback {

    private static final Logger LOGGER = LogManager.getLogger(TimerCallback.class);

    /**
     * Called by the "ON_TIMER" statement. Should not be invoked directly.
     */
    public void update(String strategyName, Date dateTime, String alias) throws Exception {

        // undeploy the statement
        Engine engine = getEngine();
        if (engine != null) {
            engine.undeployStatement(alias);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} start", alias);
        }
        // call orderCompleted
        onTimer(dateTime);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} end", alias);
        }
    }

    /**
     * Will be executed by the Esper Engine on the given time.
     * Needs to be overwritten by implementing classes.
     */
    public abstract void onTimer(Date dateTime) throws Exception;
}