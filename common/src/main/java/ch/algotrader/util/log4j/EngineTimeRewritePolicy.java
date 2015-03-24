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
package ch.algotrader.util.log4j;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;

import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;

/**
 * Log4J log rewrite policy that sets log date stamp to that of the Esper engine
 * when executing in simulation mode.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class EngineTimeRewritePolicy implements RewritePolicy {

    private final EngineManager engineManager;

    public EngineTimeRewritePolicy(final EngineManager engineManager) {
        this.engineManager = engineManager;
    }

    @Override
    public LogEvent rewrite(final LogEvent source) {
        if (this.engineManager == null) {
            return source;
        }

        // find the Engine with the earliest time
        long latestTime = Long.MAX_VALUE;
        for (Engine engine : this.engineManager.getEngines()) {

            if (!engine.isDestroyed()) {
                long engineTime = engine.getCurrentTimeInMillis();
                if (engineTime < latestTime) {
                    latestTime = engineTime;
                }
            }
        }
        if (latestTime < Long.MAX_VALUE) {
            return Log4jLogEvent.createEvent(
                    source.getLoggerName(),
                    source.getMarker(),
                    source.getLoggerFqcn(),
                    source.getLevel(),
                    source.getMessage(),
                    source.getThrown(),
                    source.getThrownProxy(),
                    source.getContextMap(),
                    source.getContextStack(),
                    source.getThreadName(),
                    source.getSource(),
                    latestTime);
        } else {
            return source;
        }
    }

}
