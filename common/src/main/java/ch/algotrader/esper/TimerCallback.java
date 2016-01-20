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
package ch.algotrader.esper;

import java.util.Date;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base Esper Callback Class that will be invoked on the give dateTime
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
final class TimerCallback {

    private static final Logger LOGGER = LogManager.getLogger(TimerCallback.class);

    private final Engine engine;
    private final String alias;
    private final Consumer<Date> consumer;

    TimerCallback(final Engine engine, final String alias, final Consumer<Date> consumer) {

        this.engine = engine;
        this.alias = alias;
        this.consumer = consumer;
    }

    /**
     * Called by the "ON_TIMER" statement. Should not be invoked directly.
     */
    public void update(String strategyName, Date dateTime, String alias) throws Exception {

        // undeploy the statement
        if (this.engine != null && this.alias != null) {
            this.engine.undeployStatement(this.alias);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} start", alias);
        }

        if (this.consumer != null) {
            this.consumer.accept(dateTime);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("{} end", alias);
        }
    }

}